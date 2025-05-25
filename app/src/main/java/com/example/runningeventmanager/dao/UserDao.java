package com.example.runningeventmanager.dao;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import com.example.runningeventmanager.database.DatabaseHelper;
import com.example.runningeventmanager.models.User;
import com.example.runningeventmanager.utils.EncryptionUtils;

import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private static final String TAG = "UserDao";
    
    private final DatabaseHelper dbHelper;
    
    public UserDao(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }
    
    // Add a new user to the database
    public long addUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableEncryptedDatabase();
        long userId = -1;
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USERNAME, user.getUsername());
            values.put(DatabaseHelper.COLUMN_EMAIL, user.getEmail());
            values.put(DatabaseHelper.COLUMN_PASSWORD, EncryptionUtils.hashPassword(user.getPassword()));
            values.put(DatabaseHelper.COLUMN_IS_ADMIN, user.isAdmin() ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_CREATED_AT, getCurrentDateTime());
            values.put(DatabaseHelper.COLUMN_UPDATED_AT, getCurrentDateTime());
            
            // Insert row
            userId = db.insert(DatabaseHelper.TABLE_USERS, null, values);
            Log.d(TAG, "User added with ID: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Error while adding user", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return userId;
    }
    
    // Get a single user by ID
    public User getUser(long userId) {
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        User user = null;
        
        try {
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    null,
                    DatabaseHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(userId)},
                    null, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting user with ID: " + userId, e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return user;
    }
    
    // Get a user by email
    public User getUserByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        User user = null;
        
        try {
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    null,
                    DatabaseHelper.COLUMN_EMAIL + "=?",
                    new String[]{email},
                    null, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting user by email: " + email, e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return user;
    }
    
    // Get all users
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_USERS;
        
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        
        try {
            Cursor cursor = db.rawQuery(selectQuery, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    User user = cursorToUser(cursor);
                    userList.add(user);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting all users", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return userList;
    }
    
    // Update a user's information
    public int updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableEncryptedDatabase();
        int rowsAffected = 0;
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USERNAME, user.getUsername());
            values.put(DatabaseHelper.COLUMN_EMAIL, user.getEmail());
            values.put(DatabaseHelper.COLUMN_IS_ADMIN, user.isAdmin() ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_STRAVA_ID, user.getStravaId());
            values.put(DatabaseHelper.COLUMN_STRAVA_TOKEN, user.getStravaToken());
            values.put(DatabaseHelper.COLUMN_AVATAR, user.getAvatar());
            values.put(DatabaseHelper.COLUMN_UPDATED_AT, getCurrentDateTime());
            
            // Update row
            rowsAffected = db.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    DatabaseHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(user.getId())}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error while updating user", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return rowsAffected;
    }
    
    // Update a user's password
    public int updatePassword(long userId, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableEncryptedDatabase();
        int rowsAffected = 0;
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_PASSWORD, EncryptionUtils.hashPassword(newPassword));
            values.put(DatabaseHelper.COLUMN_UPDATED_AT, getCurrentDateTime());
            
            // Update row
            rowsAffected = db.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    DatabaseHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(userId)}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error while updating password", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return rowsAffected;
    }
    
    // Delete a user
    public int deleteUser(long userId) {
        SQLiteDatabase db = dbHelper.getWritableEncryptedDatabase();
        int rowsAffected = 0;
        
        try {
            rowsAffected = db.delete(
                    DatabaseHelper.TABLE_USERS,
                    DatabaseHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(userId)}
            );
            
            Log.d(TAG, "User deletion - ID: " + userId + ", rows affected: " + rowsAffected);
        } catch (Exception e) {
            Log.e(TAG, "Error while deleting user", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return rowsAffected;
    }
    
    // Check if a user with the given email exists
    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        boolean exists = false;
        
        try {
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COLUMN_ID},
                    DatabaseHelper.COLUMN_EMAIL + "=?",
                    new String[]{email},
                    null, null, null, null);
            
            if (cursor != null) {
                exists = cursor.getCount() > 0;
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking if email exists: " + email, e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return exists;
    }
    
    // Authenticate a user
    public User authenticate(String email, String password) {
        User user = getUserByEmail(email);
        
        if (user != null) {
            String storedPassword = user.getPassword();
            if (EncryptionUtils.verifyPassword(password, storedPassword)) {
                return user;
            }
        }
        
        return null;
    }
    
    // Count total users
    public int getUserCount() {
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        int count = 0;
        
        try {
            String countQuery = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USERS;
            Cursor cursor = db.rawQuery(countQuery, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user count", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return count;
    }
    
    // Update user's Strava connection details
    public int updateStravaConnection(long userId, String stravaId, String stravaToken) {
        SQLiteDatabase db = dbHelper.getWritableEncryptedDatabase();
        int rowsAffected = 0;
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_STRAVA_ID, stravaId);
            values.put(DatabaseHelper.COLUMN_STRAVA_TOKEN, stravaToken);
            values.put(DatabaseHelper.COLUMN_UPDATED_AT, getCurrentDateTime());
            
            // Update row
            rowsAffected = db.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    DatabaseHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(userId)}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error while updating Strava connection", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return rowsAffected;
    }
    
    // Authenticate an admin user by username and password
    public User authenticateAdmin(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        User user = null;
        
        try {
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    null,
                    DatabaseHelper.COLUMN_USERNAME + "=? AND " + DatabaseHelper.COLUMN_IS_ADMIN + "=?",
                    new String[]{username, "1"},
                    null, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
                cursor.close();
                
                // Verify password
                if (!EncryptionUtils.verifyPassword(password, user.getPassword())) {
                    user = null;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error authenticating admin: " + username, e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return user;
    }
    
    // Check if an admin with the given username exists
    public boolean checkAdminExists(String username) {
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        boolean exists = false;
        
        try {
            // Thêm log để kiểm tra
            Log.d(TAG, "Checking if admin exists: " + username);
            
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COLUMN_ID},
                    DatabaseHelper.COLUMN_USERNAME + "=? AND " + DatabaseHelper.COLUMN_IS_ADMIN + "=?",
                    new String[]{username, "1"},
                    null, null, null, null);
            
            if (cursor != null) {
                exists = cursor.getCount() > 0;
                Log.d(TAG, "Admin exists: " + exists + ", count: " + cursor.getCount());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking if admin exists: " + username, e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return exists;
    }
    
    // Get all regular users (non-admin)
    public List<User> getAllRegularUsers() {
        List<User> userList = new ArrayList<>();
        
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        
        try {
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    null,
                    DatabaseHelper.COLUMN_IS_ADMIN + "=?",
                    new String[]{"0"},
                    null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    User user = cursorToUser(cursor);
                    userList.add(user);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all regular users", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return userList;
    }
    
    // Helper method to convert a Cursor to a User object
    private User cursorToUser(Cursor cursor) {
        User user = new User();
        
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD)));
        user.setAdmin(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_ADMIN)) == 1);
        
        // Handle nullable columns
        int stravaIdIdx = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STRAVA_ID);
        if (!cursor.isNull(stravaIdIdx)) {
            user.setStravaId(cursor.getString(stravaIdIdx));
        }
        
        int stravaTokenIdx = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STRAVA_TOKEN);
        if (!cursor.isNull(stravaTokenIdx)) {
            user.setStravaToken(cursor.getString(stravaTokenIdx));
        }
        
        int avatarIdx = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AVATAR);
        if (!cursor.isNull(avatarIdx)) {
            user.setAvatar(cursor.getString(avatarIdx));
        }
        
        user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT)));
        user.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UPDATED_AT)));
        
        return user;
    }
    
    // Helper method to get current date and time in SQLite format
    private String getCurrentDateTime() {
        return java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
    }
    
    // Thêm phương thức này để lấy user theo username
    public User getUserByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        User user = null;
        
        try {
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    null,
                    DatabaseHelper.COLUMN_USERNAME + "=?",
                    new String[]{username},
                    null, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user by username: " + username, e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return user;
    }
    
    // Thêm phương thức đặc biệt để tạo tài khoản admin mặc định
    public long createDefaultAdmin() {
        SQLiteDatabase db = dbHelper.getWritableEncryptedDatabase();
        long userId = -1;
        
        try {
            // Kiểm tra xem admin đã tồn tại chưa
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COLUMN_ID},
                    DatabaseHelper.COLUMN_USERNAME + "=? OR " + DatabaseHelper.COLUMN_EMAIL + "=?",
                    new String[]{"admin", "admin@runmanager.com"},
                    null, null, null, null);
            
            if (cursor != null && cursor.getCount() > 0) {
                Log.d(TAG, "Default admin already exists");
                cursor.close();
                
                // Trả về ID của admin đã tồn tại
                cursor = db.query(
                        DatabaseHelper.TABLE_USERS,
                        new String[]{DatabaseHelper.COLUMN_ID},
                        DatabaseHelper.COLUMN_USERNAME + "=?",
                        new String[]{"admin"},
                        null, null, null, null);
                
                if (cursor != null && cursor.moveToFirst()) {
                    userId = cursor.getLong(0);
                    cursor.close();
                }
                
                return userId;
            }
            
            if (cursor != null) {
                cursor.close();
            }
            
            // Tạo admin mới
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USERNAME, "admin");
            values.put(DatabaseHelper.COLUMN_EMAIL, "admin@runmanager.com");
            values.put(DatabaseHelper.COLUMN_PASSWORD, EncryptionUtils.hashPassword("admin"));
            values.put(DatabaseHelper.COLUMN_IS_ADMIN, 1);
            values.put(DatabaseHelper.COLUMN_CREATED_AT, getCurrentDateTime());
            values.put(DatabaseHelper.COLUMN_UPDATED_AT, getCurrentDateTime());
            
            // Insert trực tiếp vào database
            userId = db.insert(DatabaseHelper.TABLE_USERS, null, values);
            
            Log.d(TAG, "Default admin created with ID: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Error creating default admin: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        
        return userId;
    }
    
    // Thêm phương thức để kiểm tra database
    public void debugDatabase() {
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        
        try {
            Log.d(TAG, "=== DATABASE DEBUG ===");
            
            // Đếm số lượng users
            Cursor countCursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_USERS, null);
            if (countCursor != null && countCursor.moveToFirst()) {
                int count = countCursor.getInt(0);
                Log.d(TAG, "Total users in database: " + count);
                countCursor.close();
            }
            
            // Liệt kê tất cả users
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{
                        DatabaseHelper.COLUMN_ID,
                        DatabaseHelper.COLUMN_USERNAME,
                        DatabaseHelper.COLUMN_EMAIL,
                        DatabaseHelper.COLUMN_IS_ADMIN
                    },
                    null, null, null, null, null);
            
            if (cursor != null) {
                Log.d(TAG, "Users in database:");
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    String username = cursor.getString(1);
                    String email = cursor.getString(2);
                    boolean isAdmin = cursor.getInt(3) == 1;
                    
                    Log.d(TAG, "User: ID=" + id + ", Username=" + username + 
                          ", Email=" + email + ", IsAdmin=" + isAdmin);
                }
                cursor.close();
            }
            
            Log.d(TAG, "=== END DEBUG ===");
        } catch (Exception e) {
            Log.e(TAG, "Error debugging database", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
} 