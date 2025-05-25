package com.example.runningeventmanager.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runningeventmanager.R;
import com.example.runningeventmanager.dao.RegistrationDao;
import com.example.runningeventmanager.dao.UserDao;
import com.example.runningeventmanager.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity {
    private static final String TAG = "UserManagementActivity";
    
    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private UserDao userDao;
    private List<User> userList;
    private long adminUserId = -1;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);
        
        try {
            // Get admin credentials from intent first, then fallback to SharedPreferences
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey("USER_ID") && extras.containsKey("IS_ADMIN")) {
                adminUserId = extras.getLong("USER_ID", -1);
                isAdmin = extras.getBoolean("IS_ADMIN", false);
                
                // Store in SharedPreferences for consistency
                SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("userId", adminUserId);
                editor.putBoolean("isAdmin", isAdmin);
                editor.apply();
            } else {
                // Fallback to SharedPreferences
                SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                adminUserId = prefs.getLong("userId", -1);
                isAdmin = prefs.getBoolean("isAdmin", false);
            }
            
            Log.d(TAG, "Admin check - isAdmin: " + isAdmin + ", adminUserId: " + adminUserId);
            
            if (!isAdmin || adminUserId <= 0) { // Check for <= 0 instead of == -1
                Log.e(TAG, "Permission denied - User is not admin or user ID is invalid");
                Toast.makeText(this, "You do not have permission to access this page", Toast.LENGTH_SHORT).show();
                // Return to admin login instead of finishing
                Intent intent = new Intent(this, AdminLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            // Set up toolbar
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("User Management");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            // Initialize UserDao
            userDao = new UserDao(this);

            // Initialize RecyclerView
            usersRecyclerView = findViewById(R.id.usersRecyclerView);
            usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Load users
            loadUsers();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing UserManagementActivity: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing user management", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        try {
            userList = userDao.getAllRegularUsers();
            Log.d(TAG, "Loaded " + (userList != null ? userList.size() : 0) + " users");
            
            userAdapter = new UserAdapter(userList);
            usersRecyclerView.setAdapter(userAdapter);
            userAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "Error loading users: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading users", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private List<User> users;

        public UserAdapter(List<User> users) {
            this.users = users != null ? users : new ArrayList<>();
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.user_list_item, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = users.get(position);
            holder.bind(user);
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            private TextView userNameTextView;
            private TextView userEmailTextView;
            private TextView userCreatedAtTextView;
            private ImageButton deleteUserButton;

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                userNameTextView = itemView.findViewById(R.id.userNameTextView);
                userEmailTextView = itemView.findViewById(R.id.userEmailTextView);
                userCreatedAtTextView = itemView.findViewById(R.id.userCreatedAtTextView);
                deleteUserButton = itemView.findViewById(R.id.deleteUserButton);
            }

            public void bind(final User user) {
                userNameTextView.setText(user.getUsername());
                userEmailTextView.setText(user.getEmail());
                userCreatedAtTextView.setText("Created: " + (user.getCreatedAt() != null ? user.getCreatedAt() : "N/A"));

                deleteUserButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmDeleteUser(user);
                    }
                });
            }
        }
    }

    private void confirmDeleteUser(final User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete user " + user.getUsername() + "? This will also delete all their event registrations.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteUser(user);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(User user) {
        try {
            Log.d(TAG, "Attempting to delete user with ID: " + user.getId());
            
            // First delete all user registrations
            RegistrationDao registrationDao = new RegistrationDao(this);
            int registrationsDeleted = registrationDao.deleteRegistrationsByUser(user.getId());
            Log.d(TAG, "Deleted " + registrationsDeleted + " registrations for user ID: " + user.getId());
            
            // Then delete the user
            int rowsDeleted = userDao.deleteUser(user.getId());
            boolean userDeleted = rowsDeleted > 0;
            
            if (userDeleted) {
                Toast.makeText(this, "User and all registrations deleted successfully", Toast.LENGTH_SHORT).show();
                
                // Reload user list immediately
                loadUsers();
            } else {
                Log.e(TAG, "Failed to delete user with ID: " + user.getId());
                Toast.makeText(this, "Failed to delete user. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting user: " + e.getMessage(), e);
            Toast.makeText(this, "Error deleting user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 