package com.example.runningeventmanager.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.runningeventmanager.R;
import com.example.runningeventmanager.dao.EventDao;
import com.example.runningeventmanager.database.DatabaseHelper;
import com.example.runningeventmanager.models.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

public class AdminDashboardActivity extends AppCompatActivity {
    private static final String TAG = "AdminDashboardActivity";
    private static final int CREATE_EVENT_REQUEST = 1001;
    
    private ListView eventsListView;
    private FloatingActionButton createEventFab;
    private Button manageUsersButton;
    private EventDao eventDao;
    private long userId;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_admin_dashboard);

            // Initialize database
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            if (dbHelper == null) {
                throw new Exception("Failed to initialize database");
            }
            eventDao = new EventDao(this);
            
            // Get user ID from intent or SharedPreferences
            userId = -1;
            isAdmin = false;
            
            // First check intent extras
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey("USER_ID") && extras.containsKey("IS_ADMIN")) {
                userId = extras.getLong("USER_ID", -1);
                isAdmin = extras.getBoolean("IS_ADMIN", false);
                
                // Store in SharedPreferences for later use
                SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("userId", userId);
                editor.putBoolean("isAdmin", isAdmin);
                editor.apply();
            } else {
                // Fallback to SharedPreferences
                SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                userId = prefs.getLong("userId", -1);
                isAdmin = prefs.getBoolean("isAdmin", false);
            }
            
            // Check if we have valid admin credentials
            if (userId == -1 || !isAdmin) {
                Toast.makeText(this, "Admin session invalid. Please login again.", Toast.LENGTH_SHORT).show();
                logout();
                return;
            }
            
            // Log the session info
            Log.d(TAG, "Admin session: userId=" + userId + ", isAdmin=" + isAdmin);

            // Set up action bar with title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Admin Dashboard");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            // Initialize views
            eventsListView = findViewById(R.id.eventsListView);
            createEventFab = findViewById(R.id.createEventFab);
            manageUsersButton = findViewById(R.id.manageUsersButton);

            // Set up click listeners
            createEventFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Check if we still have valid credentials
                        if (userId == -1 || !isAdmin) {
                            Toast.makeText(AdminDashboardActivity.this, 
                                "Session error. Please login again.", Toast.LENGTH_SHORT).show();
                            logout();
                            return;
                        }
                        
                        Intent intent = new Intent(AdminDashboardActivity.this, CreateEventActivity.class);
                        intent.putExtra("USER_ID", userId);
                        intent.putExtra("IS_ADMIN", isAdmin);
                        startActivityForResult(intent, CREATE_EVENT_REQUEST);
                    } catch (Exception e) {
                        Log.e(TAG, "Error launching CreateEventActivity", e);
                        Toast.makeText(AdminDashboardActivity.this, 
                            "Error launching Create Event screen", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            manageUsersButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Check if we still have valid credentials
                        if (userId == -1 || !isAdmin) {
                            Toast.makeText(AdminDashboardActivity.this, 
                                "Session error. Please login again.", Toast.LENGTH_SHORT).show();
                            logout();
                            return;
                        }
                        
                        // Try to start the UserManagementActivity if available
                        try {
                            Class<?> userManagementClass = Class.forName("com.example.runningeventmanager.activities.UserManagementActivity");
                            Intent intent = new Intent(AdminDashboardActivity.this, userManagementClass);
                            intent.putExtra("USER_ID", userId);
                            intent.putExtra("IS_ADMIN", isAdmin);
                            startActivity(intent);
                        } catch (ClassNotFoundException e) {
                            Log.d(TAG, "UserManagementActivity not found, using fallback", e);
                            createSimpleUserManagementActivity();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error launching UserManagementActivity", e);
                        Toast.makeText(AdminDashboardActivity.this, 
                            "Error launching User Management screen", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Set up event list click listener
            eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        Map<String, String> event = (Map<String, String>) parent.getItemAtPosition(position);
                        showEventOptions(event);
                    } catch (Exception e) {
                        Log.e(TAG, "Error handling event click", e);
                        Toast.makeText(AdminDashboardActivity.this, 
                            "Error processing event selection", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Load events
            loadEvents();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing dashboard: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload events when activity becomes visible
        loadEvents();
    }

    @Override 
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == CREATE_EVENT_REQUEST) {
            // Reload events after creating/editing an event
            loadEvents();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadEvents() {
        try {
            List<Map<String, String>> data = new ArrayList<>();
            
            // Check if database is initialized
            if (eventDao == null) {
                eventDao = new EventDao(this);
            }
            
            // Try to load events from database first
            List<Event> events = eventDao.getAllEvents();
            
            // Check the event count directly from database as a fallback
            int eventCount = eventDao.getEventCount();
            Log.d(TAG, "Event count from database: " + eventCount);
            
            if (events != null && !events.isEmpty()) {
                for (Event event : events) {
                    Map<String, String> eventMap = new HashMap<>();
                    eventMap.put("id", String.valueOf(event.getId()));
                    eventMap.put("name", event.getName());
                    eventMap.put("date", event.getDate());
                    eventMap.put("description", event.getDescription());
                    eventMap.put("status", event.getStatus());
                    data.add(eventMap);
                }
                
                Log.d(TAG, "Loaded " + events.size() + " events from database");
            } else if (eventCount > 0) {
                // If we have events but couldn't load them, try a different method
                Log.d(TAG, "Events exist but couldn't load them, trying direct SQL");
                loadEventsDirectly(data);
            } else {
                Log.d(TAG, "No events found in database, creating a new event");
                // Instead of loading sample data, create a real event if needed
                createDefaultEvent();
                
                // Try to load again after creating default event
                events = eventDao.getAllEvents();
                if (events != null && !events.isEmpty()) {
                    for (Event event : events) {
                        Map<String, String> eventMap = new HashMap<>();
                        eventMap.put("id", String.valueOf(event.getId()));
                        eventMap.put("name", event.getName());
                        eventMap.put("date", event.getDate());
                        eventMap.put("description", event.getDescription());
                        eventMap.put("status", event.getStatus());
                        data.add(eventMap);
                    }
                }
            }

            // Create adapter with the data we have
            SimpleAdapter adapter = new SimpleAdapter(
                this,
                data,
                R.layout.admin_event_list_item,
                new String[]{"name", "date", "description", "status"},
                new int[]{R.id.eventName, R.id.eventDate, R.id.eventDescription, R.id.eventStatus}
            );

            // Set adapter
            eventsListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading events: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadEventsDirectly(List<Map<String, String>> data) {
        net.sqlcipher.database.SQLiteDatabase db = null;
        try {
            db = DatabaseHelper.getInstance(this).getReadableEncryptedDatabase();
            net.sqlcipher.Cursor cursor = db.rawQuery(
                "SELECT * FROM " + DatabaseHelper.TABLE_EVENTS + 
                " ORDER BY " + DatabaseHelper.COLUMN_EVENT_DATE + " ASC", null);
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Map<String, String> eventMap = new HashMap<>();
                    int idIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                    int nameIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_NAME);
                    int dateIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_DATE);
                    int descIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_DESCRIPTION);
                    int statusIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_STATUS);
                    
                    if (idIdx >= 0) eventMap.put("id", String.valueOf(cursor.getLong(idIdx)));
                    if (nameIdx >= 0) eventMap.put("name", cursor.getString(nameIdx));
                    if (dateIdx >= 0) eventMap.put("date", cursor.getString(dateIdx));
                    if (descIdx >= 0) eventMap.put("description", cursor.getString(descIdx));
                    if (statusIdx >= 0) eventMap.put("status", cursor.getString(statusIdx));
                    
                    data.add(eventMap);
                } while (cursor.moveToNext());
                cursor.close();
                Log.d(TAG, "Loaded " + data.size() + " events directly from database");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading events directly from database", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
    
    private void createDefaultEvent() {
        try {
            // Check if we have a valid user ID
            if (userId == -1) {
                Log.e(TAG, "Cannot create default event: user ID not found");
                return;
            }
            
            // Get tomorrow's date
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            String tomorrowDate = dateFormat.format(calendar.getTime());
            
            // Create a default event
            Event event = new Event(
                "5K Fun Run",
                "A 5K fun run for all fitness levels",
                tomorrowDate,
                "City Park",
                5.0,
                "Any pace welcome",
                100,
                userId
            );
            event.setStatus("UPCOMING");
            
            // Add to database
            long eventId = eventDao.addEvent(event);
            Log.d(TAG, "Created default event with ID: " + eventId);
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating default event", e);
        }
    }
    
    private void showEventOptions(final Map<String, String> event) {
        String[] options = {"Edit", "Delete", "View Participants"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Event Options")
               .setItems(options, (dialog, which) -> {
                   switch (which) {
                       case 0: // Edit
                           editEvent(event);
                           break;
                       case 1: // Delete
                           deleteEvent(event);
                           break;
                       case 2: // View Participants
                           viewParticipants(event);
                           break;
                   }
               })
               .show();
    }
    
    private void editEvent(Map<String, String> event) {
        try {
            // Check if we still have valid credentials
            if (userId == -1 || !isAdmin) {
                Toast.makeText(AdminDashboardActivity.this, 
                    "Session error. Please login again.", Toast.LENGTH_SHORT).show();
                logout();
                return;
            }
            
            Intent intent = new Intent(this, CreateEventActivity.class);
            intent.putExtra("EVENT_ID", event.get("id"));
            intent.putExtra("IS_EDIT_MODE", true);
            intent.putExtra("EVENT_NAME", event.get("name"));
            intent.putExtra("EVENT_DATE", event.get("date"));
            intent.putExtra("EVENT_DESCRIPTION", event.get("description"));
            intent.putExtra("USER_ID", userId);
            intent.putExtra("IS_ADMIN", isAdmin);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error editing event: " + e.getMessage(), e);
            Toast.makeText(this, "Error editing event", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void deleteEvent(Map<String, String> event) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Delete Event")
               .setMessage("Are you sure you want to delete " + event.get("name") + "?")
               .setPositiveButton("Delete", (dialog, which) -> {
                   try {
                       // Get event ID and delete from database
                       long eventId = Long.parseLong(event.get("id"));
                       Log.d(TAG, "Attempting to delete event with ID: " + eventId);
                       
                       // Delete related registrations first to maintain referential integrity
                       int registrationsDeleted = deleteEventRegistrations(eventId);
                       Log.d(TAG, "Deleted " + registrationsDeleted + " registrations for event ID: " + eventId);
                       
                       // Then delete the event
                       boolean eventDeleted = eventDao.deleteEvent(eventId);
                       
                       if (eventDeleted) {
                           Log.d(TAG, "Successfully deleted event with ID: " + eventId);
                           Toast.makeText(AdminDashboardActivity.this,
                               "Event and all registrations deleted successfully", Toast.LENGTH_SHORT).show();
                           
                           // First remove from adapter directly
                           @SuppressWarnings("unchecked")
                           SimpleAdapter adapter = (SimpleAdapter) eventsListView.getAdapter();
                           if (adapter != null) {
                               List<Map<String, String>> data = new ArrayList<>();
                               for (int i = 0; i < adapter.getCount(); i++) {
                                   Map<String, String> item = (Map<String, String>) adapter.getItem(i);
                                   if (!event.get("id").equals(item.get("id"))) {
                                       data.add(item);
                                   }
                               }
                               
                               // If no items left, create an empty adapter
                               if (data.isEmpty()) {
                                   // Force reload from database
                                   loadEvents();
                               } else {
                                   // Update adapter with filtered data
                                   adapter = new SimpleAdapter(
                                       AdminDashboardActivity.this,
                                       data,
                                       R.layout.admin_event_list_item,
                                       new String[]{"name", "date", "description", "status"},
                                       new int[]{R.id.eventName, R.id.eventDate, R.id.eventDescription, R.id.eventStatus}
                                   );
                                   eventsListView.setAdapter(adapter);
                                   adapter.notifyDataSetChanged();
                               }
                           } else {
                               // Force reload events if adapter is null
                               loadEvents();
                           }
                       } else {
                           Log.e(TAG, "Failed to delete event with ID: " + eventId);
                           Toast.makeText(AdminDashboardActivity.this,
                               "Failed to delete event. Please try again.", Toast.LENGTH_SHORT).show();
                       }
                   } catch (Exception e) {
                       Log.e(TAG, "Error deleting event: " + e.getMessage(), e);
                       Toast.makeText(AdminDashboardActivity.this,
                           "Error deleting event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                   }
               })
               .setNegativeButton("Cancel", null)
               .show();
    }
    
    // Helper method to delete event registrations
    private int deleteEventRegistrations(long eventId) {
        int deleted = 0;
        try {
            // Get writable database
            net.sqlcipher.database.SQLiteDatabase db = DatabaseHelper.getInstance(this).getWritableEncryptedDatabase();
            
            // Delete registrations
            deleted = db.delete(
                DatabaseHelper.TABLE_REGISTRATIONS,
                DatabaseHelper.COLUMN_EVENT_ID + " = ?",
                new String[]{String.valueOf(eventId)}
            );
            
            Log.d(TAG, "Deleted " + deleted + " registrations for event ID: " + eventId);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting event registrations: " + e.getMessage(), e);
        }
        return deleted;
    }
    
    private void viewParticipants(Map<String, String> event) {
        Toast.makeText(this, "Participants for " + event.get("name"), Toast.LENGTH_SHORT).show();
    }
    
    private void createSimpleUserManagementActivity() {
        // Create a simple user management activity
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("User Management")
               .setMessage("This is a simple user management dialog.\n\nUsers:\n- User1 (user1@example.com)\n- User2 (user2@example.com)")
               .setPositiveButton("OK", null)
               .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout_admin) {
            logout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
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
} 