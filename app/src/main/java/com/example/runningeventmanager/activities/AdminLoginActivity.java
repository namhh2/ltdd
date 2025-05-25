package com.example.runningeventmanager.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.runningeventmanager.R;

public class AdminLoginActivity extends AppCompatActivity {
    private static final String TAG = "AdminLoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_admin_login);
            
            // Get views
            EditText usernameEditText = findViewById(R.id.adminUsernameEditText);
            EditText passwordEditText = findViewById(R.id.adminPasswordEditText);
            Button loginButton = findViewById(R.id.adminLoginButton);
            TextView userLoginLink = findViewById(R.id.userLoginLink);
            
            // Set up login button
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Get username and password
                        String username = usernameEditText.getText().toString().trim();
                        String password = passwordEditText.getText().toString().trim();
                        
                        // Validate input
                        if (username.isEmpty() || password.isEmpty()) {
                            Toast.makeText(AdminLoginActivity.this, 
                                "Please enter both username and password", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        // Check credentials (hardcoded for simplicity)
                        if ("admin".equals(username) && "admin".equals(password)) {
                            // Create admin session
                            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.putBoolean("isAdmin", true);
                            editor.putString("username", "admin");
                            editor.putLong("userId", 1); // Use a valid ID for admin (assuming ID 1 exists in DB)
                            editor.putString("email", "admin@runningeventmanager.com");
                            editor.apply();
                            
                            // Navigate to admin dashboard
                            Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                            intent.putExtra("USER_ID", 1L);  // Pass user ID directly to the activity
                            intent.putExtra("IS_ADMIN", true);  // Pass admin status directly
                            startActivity(intent);
                            finish();
                        } else {
                            // Show error message
                            Toast.makeText(AdminLoginActivity.this, 
                                "Invalid admin credentials", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Login error: " + e.getMessage(), e);
                        Toast.makeText(AdminLoginActivity.this, 
                            "Login error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            
            // Set up user login link
            userLoginLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AdminLoginActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing admin login", Toast.LENGTH_LONG).show();
            
            // Return to main login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
} 