package com.example.runningeventmanager.dao;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import com.example.runningeventmanager.database.DatabaseHelper;
import com.example.runningeventmanager.models.Event;

import java.util.ArrayList;
import java.util.List;

public class EventDao {
    private static final String TAG = "EventDao";
    
    private final DatabaseHelper dbHelper;
    
    public EventDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }
    
    // Add a new event to the database
    public long addEvent(Event event) {
        SQLiteDatabase db = dbHelper.getWritableEncryptedDatabase();
        long eventId = -1;
        
        try {
            ContentValues values = new ContentValues();
                        values.put(DatabaseHelper.COLUMN_EVENT_NAME, event.getName());            values.put(DatabaseHelper.COLUMN_EVENT_DATE, event.getDate());            values.put(DatabaseHelper.COLUMN_EVENT_DESCRIPTION, event.getDescription());            values.put(DatabaseHelper.COLUMN_PACE_REQUIREMENT, event.getPaceRequirement());            values.put(DatabaseHelper.COLUMN_STATUS, event.getStatus());            values.put(DatabaseHelper.COLUMN_MAX_PARTICIPANTS, event.getMaxParticipants());            values.put(DatabaseHelper.COLUMN_CREATED_BY, event.getCreatedBy());                        // Ensure these fields are properly set            if (event.getLocation() != null) {                values.put(DatabaseHelper.COLUMN_LOCATION, event.getLocation());            }            if (event.getDistance() > 0) {                values.put(DatabaseHelper.COLUMN_EVENT_DISTANCE, event.getDistance());            }                        values.put(DatabaseHelper.COLUMN_CREATED_AT, getCurrentDateTime());            values.put(DatabaseHelper.COLUMN_UPDATED_AT, getCurrentDateTime());
            
            // Insert row
            eventId = db.insert(DatabaseHelper.TABLE_EVENTS, null, values);
            Log.d(TAG, "Event added with ID: " + eventId);
        } catch (Exception e) {
            Log.e(TAG, "Error while adding event", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return eventId;
    }
    
    // Get a single event by ID
    public Event getEvent(long eventId) {
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        Event event = null;
        
        try {
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_EVENTS,
                    null,
                    DatabaseHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(eventId)},
                    null, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                event = cursorToEvent(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting event with ID: " + eventId, e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return event;
    }
    
    // Get all events
    public List<Event> getAllEvents() {
        List<Event> eventList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_EVENTS + 
                           " ORDER BY " + DatabaseHelper.COLUMN_EVENT_DATE + " ASC";
        
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Event event = cursorToEvent(cursor);
                    eventList.add(event);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting all events", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return eventList;
    }
    
    // Get upcoming events
    public List<Event> getUpcomingEvents() {
        List<Event> eventList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_EVENTS + 
                           " WHERE " + DatabaseHelper.COLUMN_STATUS + " = 'UPCOMING'" +
                           " ORDER BY " + DatabaseHelper.COLUMN_EVENT_DATE + " ASC";
        
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Event event = cursorToEvent(cursor);
                    eventList.add(event);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting upcoming events", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return eventList;
    }
    
    // Update an event
    public int updateEvent(Event event) {
        SQLiteDatabase db = dbHelper.getWritableEncryptedDatabase();
        int rowsAffected = 0;
        
        try {
            ContentValues values = new ContentValues();
                        values.put(DatabaseHelper.COLUMN_EVENT_NAME, event.getName());            values.put(DatabaseHelper.COLUMN_EVENT_DATE, event.getDate());            values.put(DatabaseHelper.COLUMN_EVENT_DESCRIPTION, event.getDescription());            values.put(DatabaseHelper.COLUMN_PACE_REQUIREMENT, event.getPaceRequirement());            values.put(DatabaseHelper.COLUMN_STATUS, event.getStatus());            values.put(DatabaseHelper.COLUMN_MAX_PARTICIPANTS, event.getMaxParticipants());                        // Ensure these fields are properly set            if (event.getLocation() != null) {                values.put(DatabaseHelper.COLUMN_LOCATION, event.getLocation());            }            if (event.getDistance() > 0) {                values.put(DatabaseHelper.COLUMN_EVENT_DISTANCE, event.getDistance());            }                        values.put(DatabaseHelper.COLUMN_UPDATED_AT, getCurrentDateTime());
            
            // Update row
            rowsAffected = db.update(
                    DatabaseHelper.TABLE_EVENTS,
                    values,
                    DatabaseHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(event.getId())}
            );
            
            Log.d(TAG, "Updated event ID: " + event.getId() + ", rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error while updating event: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return rowsAffected;
    }
    
    // Delete an event
    public boolean deleteEvent(long eventId) {
        SQLiteDatabase db = dbHelper.getWritableEncryptedDatabase();
        boolean success = false;
        
        try {
            int rowsAffected = db.delete(
                    DatabaseHelper.TABLE_EVENTS,
                    DatabaseHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(eventId)}
            );
            
            success = rowsAffected > 0;
            Log.d(TAG, "Event deletion - ID: " + eventId + ", success: " + success + ", rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error while deleting event", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return success;
    }
    
    // Get events created by a specific user
    public List<Event> getEventsByCreator(long userId) {
        List<Event> eventList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_EVENTS + 
                           " WHERE " + DatabaseHelper.COLUMN_CREATED_BY + " = " + userId +
                           " ORDER BY " + DatabaseHelper.COLUMN_EVENT_DATE + " DESC";
        
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Event event = cursorToEvent(cursor);
                    eventList.add(event);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting events by creator", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return eventList;
    }
    
    // Count total events
    public int getEventCount() {
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        int count = 0;
        
        try {
            String countQuery = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_EVENTS;
            Cursor cursor = db.rawQuery(countQuery, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting event count", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return count;
    }
    
    // Helper method to convert a Cursor to an Event object
    private Event cursorToEvent(Cursor cursor) {
        Event event = new Event();
        
        event.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        event.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_NAME)));
        event.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_DATE)));
        event.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_DESCRIPTION)));
        event.setPaceRequirement(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PACE_REQUIREMENT)));
        event.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS)));
        
        int maxParticipantsIdx = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MAX_PARTICIPANTS);
        if (!cursor.isNull(maxParticipantsIdx)) {
            event.setMaxParticipants(cursor.getInt(maxParticipantsIdx));
        }
        
        event.setCreatedBy(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_BY)));
        
        int locationIdx = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION);
        if (!cursor.isNull(locationIdx)) {
            event.setLocation(cursor.getString(locationIdx));
        }
        
        int distanceIdx = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_DISTANCE);
        if (!cursor.isNull(distanceIdx)) {
            event.setDistance(cursor.getDouble(distanceIdx));
        }
        
        event.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT)));
        event.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UPDATED_AT)));
        
        return event;
    }
    
    // Helper method to get current date and time in SQLite format
    private String getCurrentDateTime() {
        return java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
    }
}