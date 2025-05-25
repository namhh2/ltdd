package com.example.runningeventmanager.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.runningeventmanager.R;
import com.example.runningeventmanager.dao.UserDao;
import com.example.runningeventmanager.models.User;

public class StravaConnectActivity extends AppCompatActivity {
    private static final String TAG = "StravaConnectActivity";
    
    // Replace these with your actual Strava API credentials
    private static final String CLIENT_ID = "YOUR_STRAVA_CLIENT_ID";
    private static final String CLIENT_SECRET = "YOUR_STRAVA_CLIENT_SECRET";
    private static final String REDIRECT_URI = "runningeventmanager://strava";
    private static final String AUTHORIZATION_URL = "https://www.strava.com/oauth/authorize";
    private static final String TOKEN_URL = "https://www.strava.com/oauth/token";
    
    private TextView connectionStatusTextView;
    private Button connectButton;
    private Button disconnectButton;
    private UserDao userDao;
    private long userId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strava_connect);
        
        // Set up the action bar
        getSupportActionBar().setTitle("Strava Connection");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Initialize views
        connectionStatusTextView = findViewById(R.id.connectionStatusTextView);
        connectButton = findViewById(R.id.connectButton);
        disconnectButton = findViewById(R.id.disconnectButton);
        
        // Initialize UserDao
        userDao = new UserDao(this);
        
        // Get user ID from session
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = prefs.getLong("userId", -1);
        
        if (userId == -1) {
            Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Get current user - change getUserById to getUser
        currentUser = userDao.getUser(userId);
        
        // Update UI based on connection status
        updateConnectionStatus();
        
        // Set up button click listeners
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectWithStrava();
            }
        });
        
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectFromStrava();
            }
        });
        
        // Check if this activity was launched from a redirect
        Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith(REDIRECT_URI)) {
            // Handle the OAuth callback
            handleStravaCallback(uri);
        }
    }
    
    private void updateConnectionStatus() {
        if (currentUser != null && currentUser.isConnectedWithStrava()) {
            connectionStatusTextView.setText("Status: Connected");
            connectButton.setVisibility(View.GONE);
            disconnectButton.setVisibility(View.VISIBLE);
        } else {
            connectionStatusTextView.setText("Status: Not connected");
            connectButton.setVisibility(View.VISIBLE);
            disconnectButton.setVisibility(View.GONE);
        }
    }
    
    private void connectWithStrava() {
        // In a real app, you would implement OAuth 2.0 flow with Strava
        // For now, we'll just open the Strava authorization page
        String authUrl = AUTHORIZATION_URL + 
                "?client_id=" + CLIENT_ID + 
                "&response_type=code" + 
                "&redirect_uri=" + REDIRECT_URI + 
                "&approval_prompt=force" + 
                "&scope=read,activity:read";
        
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
        startActivity(browserIntent);
        
        Toast.makeText(this, "Opening Strava authorization...", Toast.LENGTH_SHORT).show();
    }
    
    private void handleStravaCallback(Uri uri) {
        // Extract the authorization code from the URI
        String code = uri.getQueryParameter("code");
        
        if (code != null) {
            // In a real app, you would exchange this code for an access token
            // using Retrofit or another HTTP client
            
            // For this example, we'll simulate a successful connection
            simulateSuccessfulConnection();
        } else {
            // Error in the OAuth process
            String error = uri.getQueryParameter("error");
            Toast.makeText(this, "Authorization failed: " + error, Toast.LENGTH_LONG).show();
        }
    }
    
    private void simulateSuccessfulConnection() {
        // In a real app, you would save the actual Strava ID and token
        String fakeStravaId = "12345678";
        String fakeStravaToken = "fake_token_for_demo";
        
        // Update the user in the database
        int result = userDao.updateStravaConnection(userId, fakeStravaId, fakeStravaToken);
        
        if (result > 0) {
            // Refresh user data - change getUserById to getUser
            currentUser = userDao.getUser(userId);
            updateConnectionStatus();
            
            Toast.makeText(this, "Successfully connected with Strava!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save Strava connection", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void disconnectFromStrava() {
        // Remove Strava connection from user
        int result = userDao.updateStravaConnection(userId, null, null);
        
        if (result > 0) {
            // Refresh user data
            currentUser = userDao.getUser(userId);
            updateConnectionStatus();
            
            Toast.makeText(this, "Disconnected from Strava", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to disconnect from Strava", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
} 