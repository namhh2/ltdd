package com.example.runningeventmanager.database;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import com.example.runningeventmanager.utils.EncryptionUtils;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "RunningEventDB";
    private static final int DATABASE_VERSION = 2;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_EVENTS = "events";
    public static final String TABLE_REGISTRATIONS = "registrations";
    public static final String TABLE_ACHIEVEMENTS = "achievements";
    public static final String TABLE_NEWS = "news";
    public static final String TABLE_STRAVA_DATA = "strava_data";

    // Common column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    // Users Table Columns
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_IS_ADMIN = "is_admin";
    public static final String COLUMN_STRAVA_ID = "strava_id";
    public static final String COLUMN_STRAVA_TOKEN = "strava_token";
    public static final String COLUMN_AVATAR = "avatar";

    // Events Table Columns
    public static final String COLUMN_EVENT_NAME = "event_name";
    public static final String COLUMN_EVENT_DATE = "event_date";
    public static final String COLUMN_EVENT_DESCRIPTION = "description";
    public static final String COLUMN_PACE_REQUIREMENT = "pace_requirement";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_MAX_PARTICIPANTS = "max_participants";
    public static final String COLUMN_CREATED_BY = "created_by";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_EVENT_DISTANCE = "distance";

    // Achievements Table Columns
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_EVENT_ID = "event_id";
    public static final String COLUMN_ACHIEVEMENT_TITLE = "title";
    public static final String COLUMN_ACHIEVEMENT_DESCRIPTION = "description";
    public static final String COLUMN_ACHIEVEMENT_DATE = "date";

    // News Table Columns
    public static final String COLUMN_NEWS_TITLE = "title";
    public static final String COLUMN_NEWS_CONTENT = "content";
    public static final String COLUMN_NEWS_IMAGE_URL = "image_url";
    public static final String COLUMN_NEWS_AUTHOR = "author";

    // Strava Data Table Columns
    public static final String COLUMN_ACTIVITY_ID = "activity_id";
    public static final String COLUMN_ACTIVITY_TYPE = "activity_type";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_MOVING_TIME = "moving_time";
    public static final String COLUMN_ELAPSED_TIME = "elapsed_time";
    public static final String COLUMN_ELEVATION_GAIN = "elevation_gain";
    public static final String COLUMN_AVERAGE_SPEED = "average_speed";
    public static final String COLUMN_MAX_SPEED = "max_speed";
    public static final String COLUMN_AVERAGE_HEARTRATE = "average_heartrate";
    public static final String COLUMN_MAX_HEARTRATE = "max_heartrate";

    private static DatabaseHelper instance;
    private final Context context;
    private boolean isInitialized = false;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        try {
            SQLiteDatabase.loadLibs(context);
            isInitialized = true;
            Log.d(TAG, "SQLCipher library loaded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error loading SQLCipher library", e);
            isInitialized = false;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT NOT NULL,"
                + COLUMN_EMAIL + " TEXT NOT NULL UNIQUE,"
                + COLUMN_PASSWORD + " TEXT NOT NULL,"
                + COLUMN_IS_ADMIN + " INTEGER DEFAULT 0,"
                + COLUMN_STRAVA_ID + " TEXT,"
                + COLUMN_STRAVA_TOKEN + " TEXT,"
                + COLUMN_AVATAR + " TEXT,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        // Create Events Table
        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EVENT_NAME + " TEXT NOT NULL,"
                + COLUMN_EVENT_DATE + " DATE NOT NULL,"
                + COLUMN_EVENT_DESCRIPTION + " TEXT,"
                + COLUMN_PACE_REQUIREMENT + " TEXT,"
                + COLUMN_STATUS + " TEXT NOT NULL,"
                + COLUMN_MAX_PARTICIPANTS + " INTEGER,"
                + COLUMN_CREATED_BY + " INTEGER REFERENCES " + TABLE_USERS + "(id),"
                + COLUMN_LOCATION + " TEXT,"
                + COLUMN_EVENT_DISTANCE + " REAL,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        // Create Registrations Table
        String CREATE_REGISTRATIONS_TABLE = "CREATE TABLE " + TABLE_REGISTRATIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER REFERENCES " + TABLE_USERS + "(id),"
                + COLUMN_EVENT_ID + " INTEGER REFERENCES " + TABLE_EVENTS + "(id),"
                + COLUMN_STATUS + " TEXT NOT NULL,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        // Create Achievements Table
        String CREATE_ACHIEVEMENTS_TABLE = "CREATE TABLE " + TABLE_ACHIEVEMENTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER REFERENCES " + TABLE_USERS + "(id),"
                + COLUMN_EVENT_ID + " INTEGER REFERENCES " + TABLE_EVENTS + "(id),"
                + COLUMN_ACHIEVEMENT_TITLE + " TEXT NOT NULL,"
                + COLUMN_ACHIEVEMENT_DESCRIPTION + " TEXT,"
                + COLUMN_ACHIEVEMENT_DATE + " DATE,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        // Create News Table
        String CREATE_NEWS_TABLE = "CREATE TABLE " + TABLE_NEWS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NEWS_TITLE + " TEXT NOT NULL,"
                + COLUMN_NEWS_CONTENT + " TEXT,"
                + COLUMN_NEWS_IMAGE_URL + " TEXT,"
                + COLUMN_NEWS_AUTHOR + " TEXT,"
                + COLUMN_EVENT_ID + " INTEGER REFERENCES " + TABLE_EVENTS + "(id),"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        // Create Strava Data Table
        String CREATE_STRAVA_DATA_TABLE = "CREATE TABLE " + TABLE_STRAVA_DATA + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER REFERENCES " + TABLE_USERS + "(id),"
                + COLUMN_ACTIVITY_ID + " TEXT NOT NULL,"
                + COLUMN_ACTIVITY_TYPE + " TEXT,"
                + COLUMN_DISTANCE + " REAL,"
                + COLUMN_MOVING_TIME + " INTEGER,"
                + COLUMN_ELAPSED_TIME + " INTEGER,"
                + COLUMN_ELEVATION_GAIN + " REAL,"
                + COLUMN_AVERAGE_SPEED + " REAL,"
                + COLUMN_MAX_SPEED + " REAL,"
                + COLUMN_AVERAGE_HEARTRATE + " REAL,"
                + COLUMN_MAX_HEARTRATE + " REAL,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_EVENTS_TABLE);
        db.execSQL(CREATE_REGISTRATIONS_TABLE);
        db.execSQL(CREATE_ACHIEVEMENTS_TABLE);
        db.execSQL(CREATE_NEWS_TABLE);
        db.execSQL(CREATE_STRAVA_DATA_TABLE);

        // Thêm admin mặc định ngay khi tạo database
        try {
            String adminPassword = EncryptionUtils.hashPassword("admin");
            String insertAdminSql = "INSERT INTO " + TABLE_USERS + 
                    " (" + COLUMN_USERNAME + ", " + COLUMN_EMAIL + ", " + COLUMN_PASSWORD + ", " + 
                    COLUMN_IS_ADMIN + ") VALUES ('admin', 'admin@runmanager.com', '" + 
                    adminPassword + "', 1)";
            db.execSQL(insertAdminSql);
            Log.d(TAG, "Default admin created during database initialization");
        } catch (Exception e) {
            Log.e(TAG, "Error creating default admin during database initialization", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STRAVA_DATA);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NEWS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACHIEVEMENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_REGISTRATIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            onCreate(db);
        }
    }

    public SQLiteDatabase getWritableEncryptedDatabase() {
        if (!isInitialized) {
            Log.e(TAG, "Database not initialized");
            return null;
        }
        try {
            return getWritableDatabase(EncryptionUtils.getDatabasePassword());
        } catch (Exception e) {
            Log.e(TAG, "Error getting writable database", e);
            return null;
        }
    }

    public SQLiteDatabase getReadableEncryptedDatabase() {
        if (!isInitialized) {
            Log.e(TAG, "Database not initialized");
            return null;
        }
        try {
            return getReadableDatabase(EncryptionUtils.getDatabasePassword());
        } catch (Exception e) {
            Log.e(TAG, "Error getting readable database", e);
            return null;
        }
    }
} 