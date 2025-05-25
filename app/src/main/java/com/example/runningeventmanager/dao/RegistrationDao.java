package com.example.runningeventmanager.dao;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import com.example.runningeventmanager.database.DatabaseHelper;
import com.example.runningeventmanager.models.Registration;

import java.util.ArrayList;
import java.util.List;

public class RegistrationDao {
    private static final String TAG = "RegistrationDao";
    
    private final DatabaseHelper dbHelper;
    
    public RegistrationDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }
    
    // Add a new registration to the database
    public long addRegistration(Registration registration) {
        SQLiteDatabase db = dbHelper.getWritableEncryptedDatabase();
        long registrationId = -1;
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USER_ID, registration.getUserId());
            values.put(DatabaseHelper.COLUMN_EVENT_ID, registration.getEventId());
            values.put(DatabaseHelper.COLUMN_STATUS, registration.getStatus());
            values.put(DatabaseHelper.COLUMN_CREATED_AT, getCurrentDateTime());
            values.put(DatabaseHelper.COLUMN_UPDATED_AT, getCurrentDateTime());
            
            // Insert row
            registrationId = db.insert(DatabaseHelper.TABLE_REGISTRATIONS, null, values);
            Log.d(TAG, "Registration added with ID: " + registrationId);
        } catch (Exception e) {
            Log.e(TAG, "Error while adding registration", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return registrationId;
    }
    
    // Get a single registration by ID
    public Registration getRegistration(long registrationId) {
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        Registration registration = null;
        
        try {
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_REGISTRATIONS,
                    null,
                    DatabaseHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(registrationId)},
                    null, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                registration = cursorToRegistration(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting registration with ID: " + registrationId, e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return registration;
    }
    
    // Get registrations by user ID
    public List<Registration> getRegistrationsByUser(long userId) {
        List<Registration> registrationList = new ArrayList<>();
        
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        
        try {
            String query = "SELECT r.*, e." + DatabaseHelper.COLUMN_EVENT_NAME + " FROM " + 
                         DatabaseHelper.TABLE_REGISTRATIONS + " r " +
                         "INNER JOIN " + DatabaseHelper.TABLE_EVENTS + " e " +
                         "ON r." + DatabaseHelper.COLUMN_EVENT_ID + " = e." + DatabaseHelper.COLUMN_ID + " " +
                         "WHERE r." + DatabaseHelper.COLUMN_USER_ID + " = ? " +
                         "ORDER BY e." + DatabaseHelper.COLUMN_EVENT_DATE + " DESC";
            
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Registration registration = cursorToRegistration(cursor);
                    registration.setEventName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_NAME)));
                    registrationList.add(registration);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting registrations by user", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return registrationList;
    }
    
    // Get registrations by event ID
    public List<Registration> getRegistrationsByEvent(long eventId) {
        List<Registration> registrationList = new ArrayList<>();
        
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        
        try {
            String query = "SELECT r.*, u." + DatabaseHelper.COLUMN_USERNAME + " FROM " + 
                         DatabaseHelper.TABLE_REGISTRATIONS + " r " +
                         "INNER JOIN " + DatabaseHelper.TABLE_USERS + " u " +
                         "ON r." + DatabaseHelper.COLUMN_USER_ID + " = u." + DatabaseHelper.COLUMN_ID + " " +
                         "WHERE r." + DatabaseHelper.COLUMN_EVENT_ID + " = ? " +
                         "ORDER BY r." + DatabaseHelper.COLUMN_CREATED_AT + " ASC";
            
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(eventId)});
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Registration registration = cursorToRegistration(cursor);
                    registration.setUserName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME)));
                    registrationList.add(registration);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting registrations by event", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return registrationList;
    }
    
    // Check if user is already registered for an event
    public boolean isUserRegistered(long userId, long eventId) {
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        boolean isRegistered = false;
        
        try {
            String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_REGISTRATIONS + 
                         " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ? " +
                         "AND " + DatabaseHelper.COLUMN_EVENT_ID + " = ?";
            
            Log.d(TAG, "Checking if user " + userId + " is registered for event " + eventId);
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(eventId)});
            
            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                isRegistered = count > 0;
                Log.d(TAG, "Registration check result: " + count + " records found, isRegistered=" + isRegistered);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking if user is registered", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return isRegistered;
    }
    
    // Update registration status
    public int updateRegistrationStatus(long registrationId, String newStatus) {
        SQLiteDatabase db = dbHelper.getWritableEncryptedDatabase();
        int rowsAffected = 0;
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_STATUS, newStatus);
            values.put(DatabaseHelper.COLUMN_UPDATED_AT, getCurrentDateTime());
            
            // Update row
            rowsAffected = db.update(
                    DatabaseHelper.TABLE_REGISTRATIONS,
                    values,
                    DatabaseHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(registrationId)}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error while updating registration status", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return rowsAffected;
    }
    
    // Update registration finish time and pace
    public int updateRegistrationResult(long registrationId, String finishTime, String pace) {
        SQLiteDatabase db = dbHelper.getWritableEncryptedDatabase();
        int rowsAffected = 0;
        
        try {
            ContentValues values = new ContentValues();
            values.put("finish_time", finishTime);
            values.put("pace", pace);
            values.put(DatabaseHelper.COLUMN_STATUS, "COMPLETED");
            values.put(DatabaseHelper.COLUMN_UPDATED_AT, getCurrentDateTime());
            
            // Update row
            rowsAffected = db.update(
                    DatabaseHelper.TABLE_REGISTRATIONS,
                    values,
                    DatabaseHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(registrationId)}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error while updating registration result", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return rowsAffected;
    }
    
    // Count registrations for an event
    public int getRegistrationCountForEvent(long eventId) {
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        int count = 0;
        
        try {
            String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_REGISTRATIONS + 
                         " WHERE " + DatabaseHelper.COLUMN_EVENT_ID + " = ?";
            
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(eventId)});
            
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting registration count", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return count;
    }
    
    // Delete a registration
    public void deleteRegistration(long registrationId) {
        SQLiteDatabase db = dbHelper.getWritableEncryptedDatabase();
        
        try {
            db.delete(
                    DatabaseHelper.TABLE_REGISTRATIONS,
                    DatabaseHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(registrationId)}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error while deleting registration", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
    
    // Delete all registrations for a specific user
    public int deleteRegistrationsByUser(long userId) {
        SQLiteDatabase db = dbHelper.getWritableEncryptedDatabase();
        int count = 0;
        
        try {
            count = db.delete(
                DatabaseHelper.TABLE_REGISTRATIONS,
                DatabaseHelper.COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)}
            );
            Log.d(TAG, "Deleted " + count + " registrations for user " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Error while deleting registrations by user", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return count;
    }
    
    // Leave an event (delete registration for specific user and event)
    public boolean leaveEvent(long userId, long eventId) {
        SQLiteDatabase db = dbHelper.getWritableEncryptedDatabase();
        int count = 0;
        
        try {
            String whereClause = DatabaseHelper.COLUMN_EVENT_ID + " = ? AND " + 
                               DatabaseHelper.COLUMN_USER_ID + " = ?";
            String[] whereArgs = {String.valueOf(eventId), String.valueOf(userId)};
            
            Log.d(TAG, "Trying to leave event: userId=" + userId + ", eventId=" + eventId);
            count = db.delete(DatabaseHelper.TABLE_REGISTRATIONS, whereClause, whereArgs);
            Log.d(TAG, "Deleted " + count + " registrations for user " + userId + " and event " + eventId);
            
            return count > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error while leaving event", e);
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
    
    // Helper method to convert a Cursor to a Registration object
    private Registration cursorToRegistration(Cursor cursor) {
        Registration registration = new Registration();
        
        registration.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        registration.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)));
        registration.setEventId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_ID)));
        registration.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS)));
        
        // Handle nullable columns
        int bibNumberIdx = cursor.getColumnIndex("bib_number");
        if (bibNumberIdx != -1 && !cursor.isNull(bibNumberIdx)) {
            registration.setBibNumber(cursor.getString(bibNumberIdx));
        }
        
        int finishTimeIdx = cursor.getColumnIndex("finish_time");
        if (finishTimeIdx != -1 && !cursor.isNull(finishTimeIdx)) {
            registration.setFinishTime(cursor.getString(finishTimeIdx));
        }
        
        int paceIdx = cursor.getColumnIndex("pace");
        if (paceIdx != -1 && !cursor.isNull(paceIdx)) {
            registration.setPace(cursor.getString(paceIdx));
        }
        
        registration.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT)));
        registration.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UPDATED_AT)));
        
        return registration;
    }
    
    // Helper method to get current date and time in SQLite format
    private String getCurrentDateTime() {
        return java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
    }
} 