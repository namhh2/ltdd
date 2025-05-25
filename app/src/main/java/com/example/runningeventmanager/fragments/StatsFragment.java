package com.example.runningeventmanager.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.runningeventmanager.R;
import com.example.runningeventmanager.activities.EventDetailActivity;
import com.example.runningeventmanager.database.DatabaseHelper;

public class StatsFragment extends Fragment {
    private static final String TAG = "StatsFragment";
    private TextView totalEventsTextView;
    private TextView completedEventsTextView;
    private TextView upcomingEventsTextView;
    private ListView registeredEventsListView;
    private TextView noEventsTextView;
    private DatabaseHelper dbHelper;
    private long userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        try {
            dbHelper = DatabaseHelper.getInstance(getContext());
            
            // Get userId from arguments or SharedPreferences
            if (getArguments() != null) {
                userId = getArguments().getLong("userId", -1);
            }
            
            // If userId is not valid, get from SharedPreferences
            if (userId <= 0) {
                SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", getActivity().MODE_PRIVATE);
                userId = prefs.getLong("userId", -1);
            }
            
            if (userId <= 0) {
                Toast.makeText(getContext(), "User session not found", Toast.LENGTH_SHORT).show();
                return view;
            }
            
            Log.d(TAG, "Loading stats for user ID: " + userId);

            // Initialize views
            totalEventsTextView = view.findViewById(R.id.totalEventsTextView);
            completedEventsTextView = view.findViewById(R.id.completedEventsTextView);
            upcomingEventsTextView = view.findViewById(R.id.upcomingEventsTextView);
            registeredEventsListView = view.findViewById(R.id.registeredEventsListView);
            noEventsTextView = view.findViewById(R.id.noEventsTextView);
            
            // Set up click listener for events
            registeredEventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    java.util.Map<String, String> event = (java.util.Map<String, String>) parent.getItemAtPosition(position);
                    long eventId = Long.parseLong(event.get("id"));
                    
                    // Navigate to event details
                    Intent intent = new Intent(getActivity(), EventDetailActivity.class);
                    intent.putExtra("EVENT_ID", eventId);
                    startActivity(intent);
                }
            });

            loadStatistics();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error loading statistics", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadStatistics() {
        try {
            SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
            if (db == null) {
                Log.e(TAG, "Failed to get readable database");
                return;
            }

            // Get total registered events
            String countQuery = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_REGISTRATIONS +
                            " WHERE user_id = ?";
            Cursor cursor = db.rawQuery(countQuery, new String[]{String.valueOf(userId)});
            cursor.moveToFirst();
            int totalEvents = cursor.getInt(0);
            cursor.close();

            // Get completed events
            String completedQuery = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_REGISTRATIONS + " r " +
                                "INNER JOIN " + DatabaseHelper.TABLE_EVENTS + " e " +
                                "ON r.event_id = e.id " +
                                "WHERE r.user_id = ? AND e.status = 'COMPLETED'";
            cursor = db.rawQuery(completedQuery, new String[]{String.valueOf(userId)});
            cursor.moveToFirst();
            int completedEvents = cursor.getInt(0);
            cursor.close();
            
            // Get upcoming events
            String upcomingQuery = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_REGISTRATIONS + " r " +
                                "INNER JOIN " + DatabaseHelper.TABLE_EVENTS + " e " +
                                "ON r.event_id = e.id " +
                                "WHERE r.user_id = ? AND e.status = 'UPCOMING'";
            cursor = db.rawQuery(upcomingQuery, new String[]{String.valueOf(userId)});
            cursor.moveToFirst();
            int upcomingEvents = cursor.getInt(0);
            cursor.close();

            // Update UI
            totalEventsTextView.setText("Total Events: " + totalEvents);
            completedEventsTextView.setText("Completed Events: " + completedEvents);
            upcomingEventsTextView.setText("Upcoming Events: " + upcomingEvents);

            // Load registered events
            String eventsQuery = "SELECT e.* FROM " + DatabaseHelper.TABLE_EVENTS + " e " +
                            "INNER JOIN " + DatabaseHelper.TABLE_REGISTRATIONS + " r " +
                            "ON e.id = r.event_id " +
                            "WHERE r.user_id = ? " +
                            "ORDER BY e.event_date DESC";
            
            cursor = db.rawQuery(eventsQuery, new String[]{String.valueOf(userId)});
            
            java.util.List<java.util.Map<String, String>> data = cursorToList(cursor);
            cursor.close();
            
            if (data.isEmpty()) {
                registeredEventsListView.setVisibility(View.GONE);
                noEventsTextView.setVisibility(View.VISIBLE);
            } else {
                registeredEventsListView.setVisibility(View.VISIBLE);
                noEventsTextView.setVisibility(View.GONE);
                
                android.widget.SimpleAdapter adapter = new android.widget.SimpleAdapter(
                    getContext(),
                    data,
                    R.layout.event_list_item,
                    new String[]{"name", "date", "description", "pace"},
                    new int[]{R.id.eventName, R.id.eventDate, R.id.eventDescription, R.id.eventPace}
                );

                registeredEventsListView.setAdapter(adapter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading statistics: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error loading statistics", Toast.LENGTH_SHORT).show();
        }
    }

    private java.util.List<java.util.Map<String, String>> cursorToList(Cursor cursor) {
        java.util.List<java.util.Map<String, String>> data = new java.util.ArrayList<>();
        
        while (cursor.moveToNext()) {
            java.util.Map<String, String> item = new java.util.HashMap<>();
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_NAME);
            int dateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_DATE);
            int descIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_DESCRIPTION);
            int paceIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PACE_REQUIREMENT);
            int statusIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_STATUS);
            
            // Add all fields including ID for navigation
            item.put("id", cursor.getString(idIndex));
            item.put("name", cursor.getString(nameIndex));
            item.put("date", cursor.getString(dateIndex));
            item.put("description", cursor.getString(descIndex));
            item.put("pace", cursor.getString(paceIndex));
            
            // Add status with proper formatting
            String status = cursor.getString(statusIndex);
            item.put("status", status);
            item.put("name", cursor.getString(nameIndex) + " (" + status + ")");
            
            data.add(item);
        }
        
        return data;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics(); // Refresh statistics when returning to fragment
    }
} 