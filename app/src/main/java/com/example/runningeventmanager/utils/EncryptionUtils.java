package com.example.runningeventmanager.utils;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtils {
    private static final String TAG = "EncryptionUtils";
    private static final String DATABASE_PASSWORD = "run_manager_secure_key_2023";
    
    // AES encryption constants
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS7Padding";
    private static final int AES_KEY_SIZE = 256;

    // Password hashing
    public static String hashPassword(String password) {
        try {
            // Create SHA-256 Hash with salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Combine salt and hash
            byte[] saltedHash = new byte[salt.length + hash.length];
            System.arraycopy(salt, 0, saltedHash, 0, salt.length);
            System.arraycopy(hash, 0, saltedHash, salt.length, hash.length);
            
            return Base64.encodeToString(saltedHash, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error hashing password", e);
            return null;
        }
    }

    public static boolean verifyPassword(String password, String storedHash) {
        try {
            byte[] saltedHash = Base64.decode(storedHash, Base64.DEFAULT);
            
            // Extract salt and hash
            byte[] salt = new byte[16];
            byte[] hash = new byte[saltedHash.length - 16];
            System.arraycopy(saltedHash, 0, salt, 0, 16);
            System.arraycopy(saltedHash, 16, hash, 0, hash.length);
            
            // Hash the input password with the same salt
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] checkHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Compare the hashes
            if (hash.length != checkHash.length) return false;
            for (int i = 0; i < hash.length; i++) {
                if (hash[i] != checkHash[i]) return false;
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error verifying password", e);
            return false;
        }
    }

    public static String getDatabasePassword() {
        return DATABASE_PASSWORD;
    }

    public static MasterKey createMasterKey(Context context) throws Exception {
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                "_androidx_security_master_key_",
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build();

        return new MasterKey.Builder(context)
                .setKeyGenParameterSpec(spec)
                .build();
    }

    public static String encryptData(String data) {
        try {
            // Generate a random 16-byte initialization vector
            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[16];
            random.nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            
            // Create AES key
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGenerator.init(AES_KEY_SIZE);
            SecretKey secretKey = generateKey();
            
            // Encrypt
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and encrypted data
            byte[] encryptedIVAndText = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, encryptedIVAndText, 0, iv.length);
            System.arraycopy(encrypted, 0, encryptedIVAndText, iv.length, encrypted.length);
            
            return Base64.encodeToString(encryptedIVAndText, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error encrypting data", e);
            return null;
        }
    }

    public static String decryptData(String encryptedData) {
        try {
            byte[] encryptedIvTextBytes = Base64.decode(encryptedData, Base64.DEFAULT);
            
            // Extract IV
            byte[] iv = new byte[16];
            System.arraycopy(encryptedIvTextBytes, 0, iv, 0, iv.length);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            
            // Extract encrypted data
            int encryptedSize = encryptedIvTextBytes.length - iv.length;
            byte[] encryptedBytes = new byte[encryptedSize];
            System.arraycopy(encryptedIvTextBytes, iv.length, encryptedBytes, 0, encryptedSize);
            
            // Create AES key
            SecretKey secretKey = generateKey();
            
            // Decrypt
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            byte[] decrypted = cipher.doFinal(encryptedBytes);
            
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e(TAG, "Error decrypting data", e);
            return null;
        }
    }

    private static SecretKey generateKey() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(DATABASE_PASSWORD.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, AES_ALGORITHM);
    }
} 