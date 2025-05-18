package com.example.mobilalkosinstagram;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize navigation
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            handleNavigationItemSelected(item.getItemId());
            return true;
        });

        // Set the profile item as selected
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
    }

    private void handleNavigationItemSelected(int itemId) {
        if (itemId == R.id.nav_home) {
            // Navigate to HomeActivity
            finish(); // Close this activity and return to HomeActivity
        } else if (itemId == R.id.nav_messages) {
            Toast.makeText(this, "Messages will be implemented later", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_profile) {
            // Already on profile
        } else if (itemId == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
        }
    }
}