package com.example.smartassistant;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new HomeFragment())
                .commit();

        bottomNavigation.setOnItemSelectedListener(item -> {

            Fragment selectedFragment;

            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_tasks) {
                selectedFragment = new TasksFragment();
            } else if (itemId == R.id.nav_notes) {
                selectedFragment = new NotesFragment();
            } else if (itemId == R.id.nav_ai) {
                selectedFragment = new AIFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else {
                selectedFragment = new HomeFragment();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, selectedFragment)
                    .commit();

            return true;
        });
    }
}
