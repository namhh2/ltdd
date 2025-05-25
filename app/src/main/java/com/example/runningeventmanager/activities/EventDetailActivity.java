package com.example.runningeventmanager.activities;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runningeventmanager.R;
import com.example.runningeventmanager.adapters.ParticipantAdapter;
import com.example.runningeventmanager.dao.EventDao;
import com.example.runningeventmanager.dao.RegistrationDao;
import com.example.runningeventmanager.models.Event;
import com.example.runningeventmanager.models.Registration;
import com.example.runningeventmanager.database.DatabaseHelper;
import net.sqlcipher.database.SQLiteDatabase;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventDetailActivity extends AppCompatActivity implements ParticipantAdapter.OnParticipantClickListener {
    private static final String TAG = "EventDetailActivity";
    private TextView eventNameTextView;
    private TextView eventDateTextView;
    private TextView eventLocationTextView;
    private TextView eventDistanceTextView;
    private TextView eventPaceRequirementTextView;
    private TextView eventDescriptionTextView;
    private TextView registrationCountTextView;
    private Button registerButton;
    private Button leaveButton;
    private RecyclerView participantsRecyclerView;
    private TextView emptyParticipantsView;
    
    private EventDao eventDao;
    private RegistrationDao registrationDao;
    private long eventId;
    private long userId;
    private Event event;
    private ParticipantAdapter participantAdapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        
        // Initialize DAOs
        eventDao = new EventDao(this);
        registrationDao = new RegistrationDao(this);
        dbHelper = DatabaseHelper.getInstance(this);
        
        // Get event ID from intent
        eventId = getIntent().getLongExtra("EVENT_ID", -1);
        if (eventId == -1) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize views
        eventNameTextView = findViewById(R.id.eventNameTextView);
        eventDateTextView = findViewById(R.id.eventDateTextView);
        eventLocationTextView = findViewById(R.id.eventLocationTextView);
        eventDistanceTextView = findViewById(R.id.eventDistanceTextView);
        eventPaceRequirementTextView = findViewById(R.id.eventPaceRequirementTextView);
        eventDescriptionTextView = findViewById(R.id.eventDescriptionTextView);
        registrationCountTextView = findViewById(R.id.registrationCountTextView);
        registerButton = findViewById(R.id.registerButton);
        leaveButton = findViewById(R.id.leaveButton);
        participantsRecyclerView = findViewById(R.id.participantsRecyclerView);
        emptyParticipantsView = findViewById(R.id.emptyParticipantsView);
        
        // Get user ID
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = prefs.getLong("userId", -1);
        Log.d(TAG, "onCreate: eventId=" + eventId + ", userId=" + userId);
        
        // Debug buttons initial state
        Log.d(TAG, "onCreate initial button states - Register: " + 
            (registerButton.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE") + 
            ", Leave: " + (leaveButton.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE"));
            
        // Set up RecyclerView
        participantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Load event details
        setupEventDetails();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called - checking registration status");
        
        // Update registration status
        updateRegistrationStatus();
        loadParticipants();
        
        // Additional button visibility check after updating status
        try {
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            userId = prefs.getLong("userId", -1);
            
            if (userId > 0 && eventId > 0) {
                SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
                if (db != null) {
                    String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_REGISTRATIONS + 
                                 " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ? " +
                                 "AND " + DatabaseHelper.COLUMN_EVENT_ID + " = ?";
                    
                    Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(eventId)});
                    if (cursor != null && cursor.moveToFirst()) {
                        int count = cursor.getInt(0);
                        boolean isRegistered = (count > 0);
                        cursor.close();
                        
                        Log.d(TAG, "onResume registration check: " + (isRegistered ? "REGISTERED" : "NOT REGISTERED") + 
                             ", count=" + count);
                        
                        // Force button visibility based on registration status
                        if (isRegistered) {
                            registerButton.setVisibility(View.GONE);
                            leaveButton.setVisibility(View.VISIBLE);
                            
                            // Log final visibility state
                            new Handler().postDelayed(() -> {
                                Log.d(TAG, "Final leave button state: " + 
                                     (leaveButton.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE"));
                            }, 100);
                        }
                    }
                    db.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking additional visibility: " + e.getMessage());
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        // Check registration status when activity starts
        try {
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            userId = prefs.getLong("userId", -1);
            boolean isAdmin = prefs.getBoolean("isAdmin", false);
            
            Log.d(TAG, "onStart - forcing button check - userId: " + userId + ", eventId: " + eventId);
            
            if (!isAdmin && userId > 0 && eventId > 0) {
                // Check directly if user is registered
                SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
                try {
                    String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_REGISTRATIONS + 
                                " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ? " +
                                "AND " + DatabaseHelper.COLUMN_EVENT_ID + " = ?";
                    
                    Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(eventId)});
                    if (cursor != null && cursor.moveToFirst()) {
                        int count = cursor.getInt(0);
                        boolean isRegistered = (count > 0);
                        cursor.close();
                        
                        Log.d(TAG, "onStart registration check: " + isRegistered + " (count: " + count + ")");
                        
                        if (isRegistered) {
                            // User is registered: show leave button
                            leaveButton.setVisibility(View.VISIBLE);
                            registerButton.setVisibility(View.GONE);
                            Log.d(TAG, "onStart: showing LEAVE EVENT button");
                            
                            // Ensure button is clickable
                            leaveButton.setOnClickListener(v -> leaveEvent());
                        } else {
                            // User not registered: show register button
                            registerButton.setVisibility(View.VISIBLE);
                            leaveButton.setVisibility(View.GONE);
                            Log.d(TAG, "onStart: hiding leave button, showing register button");
                        }
                    }
                } finally {
                    if (db != null && db.isOpen()) db.close();
                }
                
                // Final log of button state
                new Handler().postDelayed(() -> {
                    Log.d(TAG, "Final leave button state: " + 
                        (leaveButton.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE"));
                }, 200);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onStart: " + e.getMessage(), e);
        }
    }
    
    private void setupEventDetails() {
        try {
            // Get event details from database
            SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
            if (db == null) {
                Log.e(TAG, "Failed to get readable database");
                return;
            }

            String[] projection = {
                DatabaseHelper.COLUMN_ID,
                DatabaseHelper.COLUMN_EVENT_NAME,
                DatabaseHelper.COLUMN_EVENT_DATE,
                DatabaseHelper.COLUMN_EVENT_DESCRIPTION,
                DatabaseHelper.COLUMN_PACE_REQUIREMENT,
                DatabaseHelper.COLUMN_EVENT_DISTANCE,
                DatabaseHelper.COLUMN_LOCATION,
                DatabaseHelper.COLUMN_STATUS,
                DatabaseHelper.COLUMN_MAX_PARTICIPANTS
            };

            String selection = DatabaseHelper.COLUMN_ID + " = ?";
            String[] selectionArgs = {String.valueOf(eventId)};

            Cursor cursor = db.query(
                DatabaseHelper.TABLE_EVENTS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
            );

            if (cursor.moveToFirst()) {
                // Initialize event object
                event = new Event();
                
                int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_NAME);
                int dateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_DATE);
                int descIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_DESCRIPTION);
                int paceIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PACE_REQUIREMENT);
                int distanceIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_DISTANCE);
                int locationIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATION);
                int statusIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_STATUS);
                int maxParticipantsIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_MAX_PARTICIPANTS);
                
                // Set values with null checks
                event.setId(idIndex != -1 ? cursor.getLong(idIndex) : -1);
                event.setName(nameIndex != -1 ? cursor.getString(nameIndex) : "");
                event.setDate(dateIndex != -1 ? cursor.getString(dateIndex) : "");
                event.setDescription(descIndex != -1 ? cursor.getString(descIndex) : "");
                event.setPaceRequirement(paceIndex != -1 ? cursor.getString(paceIndex) : "");
                event.setDistance(distanceIndex != -1 ? cursor.getDouble(distanceIndex) : 0);
                event.setLocation(locationIndex != -1 ? cursor.getString(locationIndex) : "");
                event.setStatus(statusIndex != -1 ? cursor.getString(statusIndex) : "UNKNOWN");
                event.setMaxParticipants(maxParticipantsIndex != -1 ? cursor.getInt(maxParticipantsIndex) : 0);

                // Set event details
                eventNameTextView.setText(event.getName());
                eventDateTextView.setText(event.getDate());
                eventDescriptionTextView.setText(event.getDescription());
                eventPaceRequirementTextView.setText(event.getPaceRequirement());
                eventDistanceTextView.setText(String.valueOf(event.getDistance()) + " km");
                eventLocationTextView.setText(event.getLocation());

                // Save the user ID for later use
                SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                userId = prefs.getLong("userId", -1);
                boolean isAdmin = prefs.getBoolean("isAdmin", false);
                
                // Check immediately if user is registered for this event
                String checkQuery = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_REGISTRATIONS + 
                                 " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ? " +
                                 "AND " + DatabaseHelper.COLUMN_EVENT_ID + " = ?";
                
                Cursor checkCursor = db.rawQuery(checkQuery, new String[]{String.valueOf(userId), String.valueOf(eventId)});
                boolean isRegistered = false;
                
                if (checkCursor != null && checkCursor.moveToFirst()) {
                    int count = checkCursor.getInt(0);
                    isRegistered = (count > 0);
                    Log.d(TAG, "User " + userId + " registration check for event " + eventId + ": " + count + " records found");
                    checkCursor.close();
                }
                
                // Update button visibility immediately based on registration status
                if (isAdmin) {
                    registerButton.setVisibility(View.GONE);
                    leaveButton.setVisibility(View.GONE);
                } else if (isRegistered) {
                    registerButton.setVisibility(View.GONE);
                    leaveButton.setVisibility(View.VISIBLE);
                    leaveButton.setText("ABANDONAR EVENTO");
                    leaveButton.setEnabled(true);
                    
                    // Set leave button click listener
                    leaveButton.setOnClickListener(v -> leaveEvent());
                    
                    Log.d(TAG, "User IS registered - Leave button set to VISIBLE");
                } else {
                    leaveButton.setVisibility(View.GONE);
                    registerButton.setVisibility(View.VISIBLE);
                    Log.d(TAG, "User NOT registered - Register button set to VISIBLE");
                    
                    // Set register button click listener
                    registerButton.setOnClickListener(v -> registerForEvent());
                }
            }
            cursor.close();
            
            // We'll still call this for any additional processing
            updateRegistrationStatus();
        } catch (Exception e) {
            Log.e(TAG, "Error setting up event details: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading event details", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadParticipants() {
        List<Registration> participants = registrationDao.getRegistrationsByEvent(eventId);
        
        if (participants != null && !participants.isEmpty()) {
            participantAdapter = new ParticipantAdapter(this, participants, this);
            participantsRecyclerView.setAdapter(participantAdapter);
            participantsRecyclerView.setVisibility(View.VISIBLE);
            emptyParticipantsView.setVisibility(View.GONE);
        } else {
            participantsRecyclerView.setVisibility(View.GONE);
            emptyParticipantsView.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateRegistrationStatus() {
        try {
            if (event == null) {
                Log.e(TAG, "Event object is null in updateRegistrationStatus");
                return;
            }
            
            // Get user ID from shared preferences
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            userId = prefs.getLong("userId", -1);
            boolean isAdmin = prefs.getBoolean("isAdmin", false);
            
            Log.d(TAG, "updateRegistrationStatus - userId: " + userId + ", eventId: " + eventId);
            
            // Check if user is registered for this event
            boolean isRegistered = registrationDao.isUserRegistered(userId, eventId);
            int registrationCount = registrationDao.getRegistrationCountForEvent(eventId);
            boolean isFull = registrationCount >= event.getMaxParticipants();
            boolean isUpcoming = "UPCOMING".equals(event.getStatus());
            
            // Update participant count
            registrationCountTextView.setText(registrationCount + " / " + event.getMaxParticipants());
            
            // Debug info
            Log.d(TAG, "Button visibility check - isAdmin: " + isAdmin + ", isRegistered: " + isRegistered);
            
            // Update button visibility based on user type and registration status
            if (isAdmin) {
                // Hide both buttons for admin users
                registerButton.setVisibility(View.GONE);
                leaveButton.setVisibility(View.GONE);
                Log.d(TAG, "Admin user - hiding both buttons");
            } else if (userId > 0) {
                // Regular user with valid ID
                if (isRegistered) {
                    // User is registered, show leave button
                    registerButton.setVisibility(View.GONE);
                    leaveButton.setVisibility(View.VISIBLE);
                    Log.d(TAG, "User is registered - SHOWING leave button, hiding register button");
                    
                    // Ensure leave button has a click listener
                    leaveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            leaveEvent();
                        }
                    });
                    
                    // Make sure the button is enabled and has correct text
                    leaveButton.setText("Leave Event");
                    leaveButton.setEnabled(true);
                    leaveButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_light));
                } else {
                    // User is not registered, show register button
                    leaveButton.setVisibility(View.GONE);
                    registerButton.setVisibility(View.VISIBLE);
                    Log.d(TAG, "User NOT registered - showing register button, HIDING leave button");
                    
                    if (isFull) {
                        registerButton.setText("Event Full");
                        registerButton.setEnabled(false);
                    } else if (!isUpcoming) {
                        registerButton.setText("Registration Closed");
                        registerButton.setEnabled(false);
                    } else {
                        registerButton.setText("Register for Event");
                        registerButton.setEnabled(true);
                        
                        // Ensure register button has a click listener
                        registerButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                registerForEvent();
                            }
                        });
                    }
                }
            } else {
                // Invalid user ID - hide both buttons
                registerButton.setVisibility(View.GONE);
                leaveButton.setVisibility(View.GONE);
                Log.e(TAG, "Invalid user ID: " + userId);
            }
            
            // Final check to log the actual visibility state of buttons after all logic
            Log.d(TAG, "Final button states - Register button visibility: " + 
                (registerButton.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE") + 
                ", Leave button visibility: " + 
                (leaveButton.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE"));
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating registration status: " + e.getMessage(), e);
        }
    }
    
    private void registerForEvent() {
        // Create registration
        Registration registration = new Registration(userId, eventId);
        
        // Add to database
        long registrationId = registrationDao.addRegistration(registration);
        
        if (registrationId != -1) {
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
            
            // Show leave button, hide register button
            registerButton.setVisibility(View.GONE);
            leaveButton.setVisibility(View.VISIBLE);
            
            // Set click listener for leave button
            leaveButton.setOnClickListener(v -> leaveEvent());
            
            // Update participant data
            updateRegistrationStatus();
            loadParticipants(); 
            
            // Log visibility state
            Log.d(TAG, "After registration - Leave button visibility: " + 
                (leaveButton.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE"));
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void leaveEvent() {
        Log.d(TAG, "leaveEvent called - userId: " + userId + ", eventId: " + eventId);
        
        try {
            // Get the user ID if needed
            if (userId <= 0) {
                SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                userId = prefs.getLong("userId", -1);
                if (userId <= 0) {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Confirm the user wants to leave the event
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Leave Event")
                .setMessage("Are you sure you want to leave this event? Your registration will be cancelled.")
                .setPositiveButton("Yes, Leave Event", (dialog, which) -> {
                    // Direct SQL deletion for reliability
                    SQLiteDatabase db = dbHelper.getWritableEncryptedDatabase();
                    if (db != null) {
                        try {
                            String whereClause = DatabaseHelper.COLUMN_EVENT_ID + " = ? AND " + 
                                              DatabaseHelper.COLUMN_USER_ID + " = ?";
                            String[] whereArgs = {String.valueOf(eventId), String.valueOf(userId)};
                            
                            int deletedRows = db.delete(DatabaseHelper.TABLE_REGISTRATIONS, whereClause, whereArgs);
                            Log.d(TAG, "Deleted rows: " + deletedRows);
                            
                            if (deletedRows > 0) {
                                Toast.makeText(EventDetailActivity.this, "You have left the event", Toast.LENGTH_SHORT).show();
                                
                                // Immediately update UI
                                registerButton.setVisibility(View.VISIBLE);
                                leaveButton.setVisibility(View.GONE);
                                
                                // Refresh data
                                loadParticipants();
                                updateRegistrationStatus();
                            } else {
                                Toast.makeText(EventDetailActivity.this, "Failed to leave the event", Toast.LENGTH_SHORT).show();
                            }
                        } finally {
                            if (db.isOpen()) db.close();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
                
        } catch (Exception e) {
            Log.e(TAG, "Error in leaveEvent: " + e.getMessage(), e);
            Toast.makeText(this, "Error leaving event", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onParticipantClick(Registration registration) {
        showParticipantDetailsDialog(registration);
    }
    
    private void showParticipantDetailsDialog(Registration registration) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_participant_details);
        
        // Initialize dialog views
        CircleImageView participantImage = dialog.findViewById(R.id.dialogParticipantImage);
        TextView participantName = dialog.findViewById(R.id.dialogParticipantName);
        TextView registrationStatus = dialog.findViewById(R.id.dialogRegistrationStatus);
        TextView registrationDate = dialog.findViewById(R.id.dialogRegistrationDate);
        TextView bibNumber = dialog.findViewById(R.id.dialogBibNumber);
        TextView finishTime = dialog.findViewById(R.id.dialogFinishTime);
        TextView pace = dialog.findViewById(R.id.dialogPace);
        LinearLayout bibNumberLayout = dialog.findViewById(R.id.bibNumberLayout);
        LinearLayout finishTimeLayout = dialog.findViewById(R.id.finishTimeLayout);
        LinearLayout paceLayout = dialog.findViewById(R.id.paceLayout);
        Button closeButton = dialog.findViewById(R.id.dialogCloseButton);
        
        // Set data
        participantName.setText(registration.getUserName());
        registrationStatus.setText(registration.getStatus());
        registrationDate.setText(registration.getCreatedAt());
        
        // Set status color
        switch (registration.getStatus()) {
            case "REGISTERED":
                registrationStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "COMPLETED":
                registrationStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                break;
            case "CANCELLED":
                registrationStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
            default:
                registrationStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
                break;
        }
        
        // Show optional fields if available
        if (registration.getBibNumber() != null && !registration.getBibNumber().isEmpty()) {
            bibNumber.setText(registration.getBibNumber());
            bibNumberLayout.setVisibility(View.VISIBLE);
        }
        
        if (registration.getFinishTime() != null && !registration.getFinishTime().isEmpty()) {
            finishTime.setText(registration.getFinishTime());
            finishTimeLayout.setVisibility(View.VISIBLE);
        }
        
        if (registration.getPace() != null && !registration.getPace().isEmpty()) {
            pace.setText(registration.getPace());
            paceLayout.setVisibility(View.VISIBLE);
        }
        
        // Set close button click listener
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        
        dialog.show();
    }
} 