package com.example.chattonapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.chattonapplication.databinding.ActivitySigninBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class SigninActivity extends AppCompatActivity {
    Button googleAuth;
    ActivitySigninBinding binding;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    FirebaseDatabase database;

    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN=20;

    GoogleSignInOptions gso;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        progressDialog =new ProgressDialog(SigninActivity.this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Login to your account");
        googleAuth=findViewById(R.id.btn_google);
        database=FirebaseDatabase.getInstance();


        gso= new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        mGoogleSignInClient= GoogleSignIn.getClient(this,gso);

        googleAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              googleSignIn();
            }
        });

        binding.btnSignIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(binding.etEmail.getText().toString().isEmpty()){
                    binding.etEmail.setError("Please enter your email");
                    return;
                }
                if(binding.etPassword.getText().toString().isEmpty()){
                    binding.etPassword.setError("Please enter your password");
                    return;
                }
                progressDialog.show();
                auth.signInWithEmailAndPassword(binding.etEmail.getText().toString(),binding.etPassword.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful())
                                {
                                    Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                                    intent.putExtra("gso", gso);
                                    startActivity(intent);
                                }
                                else{
                                    Toast.makeText(SigninActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        binding.tvClickSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SigninActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
        if(auth.getCurrentUser()!=null)
        {
            Intent intent = new Intent(SigninActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    private void googleSignIn() {

   Intent intent =mGoogleSignInClient.getSignInIntent();
   startActivityForResult(intent,RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if (requestCode==RC_SIGN_IN){

        Task<GoogleSignInAccount> task=GoogleSignIn.getSignedInAccountFromIntent(data);

        try{
             GoogleSignInAccount account=task.getResult(ApiException.class);
             firebaseAuth(account.getIdToken());
        }
        catch (Exception e) {
            Log.e("Exception", "An exception occurred: " + e.toString());
            Toast.makeText(this, "An exception occurred: " + e.toString(), Toast.LENGTH_SHORT).show();
        }

        }
    }

    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            // Check if the user exists in the database
                            DatabaseReference userRef = database.getReference().child("Users").child(user.getUid());
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        // User exists, directly go to MainActivity
                                        Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                                        intent.putExtra("gso", gso);
                                        startActivity(intent);
                                        finish(); // Finish this activity to prevent going back to it using back button
                                    } else {
                                        // User does not exist, prompt for password
                                        // Here you can show a dialog or navigate to a different activity to let the user enter password
                                        // After getting the password, you can save Google ID and name to the database
                                        // For now, let's assume you're navigating to SignUpActivity
                                        Intent intent = new Intent(SigninActivity.this, SignUpActivity.class);
                                        intent.putExtra("googleIdToken", idToken); // Pass Google ID token to SignUpActivity
                                        startActivity(intent);
                                        finish(); // Finish this activity to prevent going back to it using back button
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // Handle error
                                    Toast.makeText(SigninActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // Sign in failed
                            Toast.makeText(SigninActivity.this, "Sign in failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}