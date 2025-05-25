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
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.fragment.app.Fragment;

import com.example.runningeventmanager.R;
import com.example.runningeventmanager.activities.CreateEventActivity;
import com.example.runningeventmanager.activities.EventDetailActivity;
import com.example.runningeventmanager.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventsFragment extends Fragment {
    private static final String TAG = "EventsFragment";
    private DatabaseHelper dbHelper;
    private ListView eventsListView;
    private Button createEventButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        try {
            dbHelper = DatabaseHelper.getInstance(getContext());
            eventsListView = view.findViewById(R.id.eventsListView);
            createEventButton = view.findViewById(R.id.createEventButton);

            // Check if user is admin
            SharedPreferences prefs = getActivity().getSharedPreferences("UserSession", getActivity().MODE_PRIVATE);
            boolean isAdmin = prefs.getBoolean("isAdmin", false);

            // Show/hide create event button based on admin status
            if (isAdmin) {
                createEventButton.setVisibility(View.VISIBLE);
                createEventButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), CreateEventActivity.class));
                    }
                });
            } else {
                createEventButton.setVisibility(View.GONE);
            }

            // Set up click listener for events list
            eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Get the selected event
                    Map<String, String> event = (Map<String, String>) parent.getItemAtPosition(position);
                    long eventId = Long.parseLong(event.get("id"));
                    
                    // Open event details
                    Intent intent = new Intent(getActivity(), EventDetailActivity.class);
                    intent.putExtra("EVENT_ID", eventId);
                    startActivity(intent);
                }
            });

            loadEvents();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage(), e);
        }

        return view;
    }

    private void loadEvents() {
        try {
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
                DatabaseHelper.COLUMN_PACE_REQUIREMENT
            };

            Cursor cursor = db.query(
                DatabaseHelper.TABLE_EVENTS,
                projection,
                null,
                null,
                null,
                null,
                DatabaseHelper.COLUMN_EVENT_DATE + " ASC"
            );

            List<Map<String, String>> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Map<String, String> item = new HashMap<>();
                item.put("id", cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID)));
                item.put("name", cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_NAME)));
                item.put("date", cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_DATE)));
                item.put("description", cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_DESCRIPTION)));
                item.put("pace", cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PACE_REQUIREMENT)));
                data.add(item);
            }
            cursor.close();

            String[] from = {"name", "date", "description", "pace"};
            int[] to = {R.id.eventName, R.id.eventDate, R.id.eventDescription, R.id.eventPace};

            SimpleAdapter adapter = new SimpleAdapter(
                getContext(),
                data,
                R.layout.event_list_item,
                from,
                to
            );

            eventsListView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error loading events: " + e.getMessage(), e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents(); // Refresh events list when returning to fragment
    }
} 