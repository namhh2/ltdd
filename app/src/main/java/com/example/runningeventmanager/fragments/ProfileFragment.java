package com.example.runningeventmanager.fragments;

import android.content.ContentValues;
import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.runningeventmanager.R;
import com.example.runningeventmanager.database.DatabaseHelper;
import com.example.runningeventmanager.utils.EncryptionUtils;

public class ProfileFragment extends Fragment {
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText currentPasswordEditText;
    private EditText newPasswordEditText;
    private Button updateProfileButton;
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        dbHelper = DatabaseHelper.getInstance(getContext());
        userId = getArguments().getInt("USER_ID", -1);

        // Initialize views
        usernameEditText = view.findViewById(R.id.usernameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        currentPasswordEditText = view.findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText);
        updateProfileButton = view.findViewById(R.id.updateProfileButton);

        loadUserProfile();

        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    updateProfile();
                }
            }
        });

        return view;
    }

    private void loadUserProfile() {
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        
        String[] projection = {
            DatabaseHelper.COLUMN_USERNAME,
            DatabaseHelper.COLUMN_EMAIL
        };
        
        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        );

        if (cursor.moveToFirst()) {
            usernameEditText.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USERNAME)));
            emailEditText.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL)));
        }
        cursor.close();
    }

    private boolean validateInput() {
        if (usernameEditText.getText().toString().trim().isEmpty()) {
            usernameEditText.setError("Username is required");
            return false;
        }
        if (emailEditText.getText().toString().trim().isEmpty()) {
            emailEditText.setError("Email is required");
            return false;
        }
        if (currentPasswordEditText.getText().toString().trim().isEmpty()) {
            currentPasswordEditText.setError("Current password is required");
            return false;
        }
        return true;
    }

    private void updateProfile() {
        SQLiteDatabase db = dbHelper.getReadableEncryptedDatabase();
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String hashedCurrentPassword = EncryptionUtils.hashPassword(currentPassword);
        
        // Verify current password
        String[] projection = {DatabaseHelper.COLUMN_ID};
        String selection = DatabaseHelper.COLUMN_ID + " = ? AND " + 
                         DatabaseHelper.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {String.valueOf(userId), hashedCurrentPassword};

        Cursor cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        );

        if (cursor.moveToFirst()) {
            // Password verified, update profile
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USERNAME, usernameEditText.getText().toString().trim());
            values.put(DatabaseHelper.COLUMN_EMAIL, emailEditText.getText().toString().trim());
            
            String newPassword = newPasswordEditText.getText().toString().trim();
            if (!newPassword.isEmpty()) {
                values.put(DatabaseHelper.COLUMN_PASSWORD, EncryptionUtils.hashPassword(newPassword));
            }

            db = dbHelper.getWritableEncryptedDatabase();
            int count = db.update(
                DatabaseHelper.TABLE_USERS,
                values,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)}
            );

            if (count > 0) {
                Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                currentPasswordEditText.setText("");
                newPasswordEditText.setText("");
            } else {
                Toast.makeText(getContext(), "Error updating profile", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }
} 