package com.example.runningeventmanager.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.runningeventmanager.R;
import com.example.runningeventmanager.dao.EventDao;
import com.example.runningeventmanager.models.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateEventActivity extends AppCompatActivity {
    private static final String TAG = "CreateEventActivity";
    private EditText eventNameEditText;
    private EditText eventDateEditText;
    private EditText eventDescriptionEditText;
    private EditText paceRequirementEditText;
    private EditText locationEditText;
    private EditText distanceEditText;
    private EditText maxParticipantsEditText;
    private Button createButton;
    private EventDao eventDao;
    private Calendar calendar;
    private long userId;
    private boolean isAdmin = false;
    private boolean isEditMode = false;
    private long eventId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        try {
            // Initialize database helper first
            eventDao = new EventDao(this);
            calendar = Calendar.getInstance();
            
            // Initialize views FIRST, before accessing them
            eventNameEditText = findViewById(R.id.eventNameEditText);
            eventDateEditText = findViewById(R.id.eventDateEditText);
            eventDescriptionEditText = findViewById(R.id.eventDescriptionEditText);
            paceRequirementEditText = findViewById(R.id.paceRequirementEditText);
            locationEditText = findViewById(R.id.locationEditText);
            distanceEditText = findViewById(R.id.distanceEditText);
            maxParticipantsEditText = findViewById(R.id.maxParticipantsEditText);
            createButton = findViewById(R.id.createButton);
            
            // Get user ID from intent first, then fallback to SharedPreferences
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey("USER_ID") && extras.containsKey("IS_ADMIN")) {
                userId = extras.getLong("USER_ID", -1);
                isAdmin = extras.getBoolean("IS_ADMIN", false);
                
                // Store in SharedPreferences for consistency
                SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("userId", userId);
                editor.putBoolean("isAdmin", isAdmin);
                editor.apply();
                
                // Check if we're in edit mode
                isEditMode = extras.getBoolean("IS_EDIT_MODE", false);
                if (isEditMode) {
                    eventId = extras.getLong("EVENT_ID", -1);
                    eventNameEditText.setText(extras.getString("EVENT_NAME", ""));
                    eventDateEditText.setText(extras.getString("EVENT_DATE", ""));
                    eventDescriptionEditText.setText(extras.getString("EVENT_DESCRIPTION", ""));
                    
                    // Try to load additional event details if available
                    if (eventId != -1) {
                        Event event = eventDao.getEvent(eventId);
                        if (event != null) {
                            // Set additional fields
                            if (event.getPaceRequirement() != null) {
                                paceRequirementEditText.setText(event.getPaceRequirement());
                            }
                            if (event.getLocation() != null) {
                                locationEditText.setText(event.getLocation());
                            }
                            if (event.getDistance() > 0) {
                                distanceEditText.setText(String.valueOf(event.getDistance()));
                            }
                            if (event.getMaxParticipants() > 0) {
                                maxParticipantsEditText.setText(String.valueOf(event.getMaxParticipants()));
                            }
                        }
                    }
                    
                    createButton.setText("Update Event");
                }
            } else {
                // Fallback to SharedPreferences
                SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                userId = prefs.getLong("userId", -1);
                isAdmin = prefs.getBoolean("isAdmin", false);
            }
            
            // Set up action bar with back button if available
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(isEditMode ? "Edit Event" : "Create Event");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            
            Log.d(TAG, "Session check - isAdmin: " + isAdmin + ", userId: " + userId);
            
            if (userId <= 0 || !isAdmin) {
                Log.e(TAG, userId <= 0 ? "User ID not found" : "User is not admin");
                Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show();
                // Return to admin login instead of regular login
                Intent intent = new Intent(this, AdminLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            // Set up date picker dialog
            DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    updateDateLabel();
                }
            };

            eventDateEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DatePickerDialog(CreateEventActivity.this, date,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            });

            createButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (validateInput()) {
                        createEvent();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing activity: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateDateLabel() {
        String format = "yyyy-MM-dd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
        eventDateEditText.setText(dateFormat.format(calendar.getTime()));
    }

    private boolean validateInput() {
        if (eventNameEditText.getText().toString().trim().isEmpty()) {
            eventNameEditText.setError("Event name is required");
            return false;
        }
        if (eventDateEditText.getText().toString().trim().isEmpty()) {
            eventDateEditText.setError("Event date is required");
            return false;
        }
        if (eventDescriptionEditText.getText().toString().trim().isEmpty()) {
            eventDescriptionEditText.setError("Event description is required");
            return false;
        }
        if (locationEditText.getText().toString().trim().isEmpty()) {
            locationEditText.setError("Location is required");
            return false;
        }
        if (distanceEditText.getText().toString().trim().isEmpty()) {
            distanceEditText.setError("Distance is required");
            return false;
        }
        try {
            double distance = Double.parseDouble(distanceEditText.getText().toString().trim());
            if (distance <= 0) {
                distanceEditText.setError("Distance must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            distanceEditText.setError("Please enter a valid number");
            return false;
        }
        if (maxParticipantsEditText.getText().toString().trim().isEmpty()) {
            maxParticipantsEditText.setError("Maximum participants is required");
            return false;
        }
        try {
            int maxParticipants = Integer.parseInt(maxParticipantsEditText.getText().toString().trim());
            if (maxParticipants <= 0) {
                maxParticipantsEditText.setError("Maximum participants must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            maxParticipantsEditText.setError("Please enter a valid number");
            return false;
        }
        return true;
    }

    private void createEvent() {
        try {
            String name = eventNameEditText.getText().toString().trim();
            String date = eventDateEditText.getText().toString().trim();
            String description = eventDescriptionEditText.getText().toString().trim();
            String paceRequirement = paceRequirementEditText.getText().toString().trim();
            String location = locationEditText.getText().toString().trim();
            
            // Parse numeric fields with extra validation
            double distance = 0;
            try {
                distance = Double.parseDouble(distanceEditText.getText().toString().trim());
            } catch (NumberFormatException e) {
                distanceEditText.setError("Please enter a valid number");
                return;
            }
            
            int maxParticipants = 0;
            try {
                maxParticipants = Integer.parseInt(maxParticipantsEditText.getText().toString().trim());
            } catch (NumberFormatException e) {
                maxParticipantsEditText.setError("Please enter a valid number");
                return;
            }
            
            Log.d(TAG, "Creating/updating event with name: " + name + ", date: " + date + 
                  ", location: " + location + ", distance: " + distance);
            
            // Create event object with status set to "UPCOMING" by default
            Event event = new Event(name, description, date, location, distance, paceRequirement, maxParticipants, userId);
            event.setStatus("UPCOMING");
            
            // For debugging
            Log.d(TAG, "Event object: " + event.toString());
            Log.d(TAG, "Location: " + event.getLocation() + ", Distance: " + event.getDistance());
            
            // Add/update event in database
            long resultEventId = -1;
            
            if (isEditMode && this.eventId != -1) {
                // Update existing event
                event.setId(this.eventId);
                int rowsUpdated = eventDao.updateEvent(event);
                
                if (rowsUpdated > 0) {
                    resultEventId = this.eventId;
                    Log.d(TAG, "Updated event with ID: " + resultEventId + ", rows affected: " + rowsUpdated);
                } else {
                    Log.e(TAG, "Failed to update event, no rows affected");
                }
            } else {
                // Create new event
                resultEventId = eventDao.addEvent(event);
                Log.d(TAG, "Created new event with ID: " + resultEventId);
            }
            
            if (resultEventId != -1) {
                Toast.makeText(this, isEditMode ? "Event updated successfully" : "Event created successfully", Toast.LENGTH_SHORT).show();
                
                // Return result to calling activity and close this activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("EVENT_ID", resultEventId);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, isEditMode ? "Error updating event" : "Error creating event", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating/updating event: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 