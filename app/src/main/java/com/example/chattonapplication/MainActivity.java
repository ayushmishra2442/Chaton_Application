package com.example.chattonapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


import com.example.chattonapplication.Adapters.FragmentsAdapter;
import com.example.chattonapplication.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    ActivityMainBinding binding;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth auth;

    GoogleSignInOptions gso;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        auth=FirebaseAuth.getInstance();

        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new FragmentsAdapter(getSupportFragmentManager()));

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.settings) {
           Intent i = new Intent(MainActivity.this, SettingsActivity.class);
           startActivity(i);
        } else if (item.getItemId() == R.id.logout) {
            auth.signOut();
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // Sign-out successful
                            // Redirect the user to your sign-in activity or perform any other actions as needed
                            Intent intent = new Intent(MainActivity.this, SigninActivity.class);
                            startActivity(intent);
                            finish(); // Optional: Finish this activity to prevent going back to it using the back button
                        }
                    });

        }

        else if(item.getItemId() == R.id.groupChat){
            Intent intentt= new Intent(MainActivity.this,GroupChatActivity.class);
            startActivity(intentt);

        }

        return true;
    }
}