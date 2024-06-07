package com.example.chattonapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.chattonapplication.Models.Users;
import com.example.chattonapplication.databinding.ActivitySettingsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {
    ActivitySettingsBinding binding;
    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseDatabase database;

    ActivityResultLauncher<String> imagePickLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Initialize ActivityResultLauncher
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        handleImage(uri);
                    } else {
                        Toast.makeText(this, "Image is not uploaded", Toast.LENGTH_SHORT).show();
                    }
                });

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        binding.saveButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status = binding.etStatus.getText().toString();
                String username = binding.etUserName1.getText().toString();
                HashMap<String, Object> obj = new HashMap<>();
                obj.put("status", status);
                obj.put("userName", username);


                database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                        .updateChildren(obj);
                Toast.makeText(SettingsActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
            }
        });

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users users = snapshot.getValue(Users.class);
                        Picasso.get()
                                .load(users.getProfilepic())
                                .placeholder(R.drawable.avatar)
                                .into(binding.profileImage);

                        binding.etStatus.setText(users.getStatus());
                        binding.etUserName1.setText(users.getUserName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        binding.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch activity for result using the launcher
                imagePickLauncher.launch("image/*");
            }
        });
    }

    // Handle the image picked
    private void handleImage(Uri sFile) {
        binding.profileImage.setImageURI(sFile);
        final StorageReference reference = storage.getReference().child("profile_pictures")
                .child(FirebaseAuth.getInstance().getUid());

        reference.putFile(sFile).addOnSuccessListener(taskSnapshot -> {
            reference.getDownloadUrl().addOnSuccessListener(uri -> {
                database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                        .child("profilepic").setValue(uri.toString());
                Toast.makeText(SettingsActivity.this, "Profile Picture Updated", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
