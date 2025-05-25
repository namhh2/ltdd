package com.example.runningeventmanager.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.runningeventmanager.R;
import com.example.runningeventmanager.fragments.EventsFragment;
import com.example.runningeventmanager.fragments.NewsFragment;
import com.example.runningeventmanager.fragments.ProfileFragment;
import com.example.runningeventmanager.fragments.StatsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private long userId;
    private String username;
    private boolean isAdmin;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get user info from session
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = prefs.getLong("userId", -1);
        username = prefs.getString("username", "");
        isAdmin = prefs.getBoolean("isAdmin", false);
        
        if (userId == -1) {
            // No valid user ID, return to login
            logout();
            return;
        }
        
        // Set up the app bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Welcome, " + username);
        }

        // Initialize bottom navigation
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EventsFragment())
                    .commit();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        
        // Show/hide admin option based on user role
        menu.findItem(R.id.action_admin).setVisible(isAdmin);
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_admin && isAdmin) {
            startActivity(new Intent(this, AdminDashboardActivity.class));
            return true;
        } else if (id == R.id.action_connect_strava) {
            // TODO: Implement Strava connection
            // Comment out the line that's causing the error:
            // startActivity(new Intent(this, StravaConnectActivity.class));
            
            // Show a toast message instead
            android.widget.Toast.makeText(this, "Strava connection coming soon!", android.widget.Toast.LENGTH_SHORT).show();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    int id = item.getItemId();

                    if (id == R.id.nav_events) {
                        selectedFragment = new EventsFragment();
                    } else if (id == R.id.nav_news) {
                        selectedFragment = new NewsFragment();
                    } else if (id == R.id.nav_stats) {
                        selectedFragment = new StatsFragment();
                    } else if (id == R.id.nav_profile) {
                        selectedFragment = new ProfileFragment();
                    }

                    if (selectedFragment != null) {
                        // Pass userId to fragment
                        Bundle args = new Bundle();
                        args.putLong("userId", userId);
                        selectedFragment.setArguments(args);

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment)
                                .commit();
                    }

                    return true;
                }
            };
    
    private void logout() {
        // Clear user session
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        
        // Return to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public long getUserId() {
        return userId;
    }
} 