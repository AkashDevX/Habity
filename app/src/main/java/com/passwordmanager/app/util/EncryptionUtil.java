package com.passwordmanager.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class EncryptionUtil {
    private static final String TAG = "EncryptionUtil";
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_PREF_NAME = "encryption_keys";
    private static final String KEY_NAME = "master_key";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    
    private static SecretKey getOrCreateMasterKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(KEY_PREF_NAME, Context.MODE_PRIVATE);
        String encodedKey = prefs.getString(KEY_NAME, null);
        
        if (encodedKey == null) {
            try {
                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                keyGenerator.init(256);
                SecretKey key = keyGenerator.generateKey();
                encodedKey = Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
                prefs.edit().putString(KEY_NAME, encodedKey).apply();
            } catch (Exception e) {
                Log.e(TAG, "Error generating master key", e);
                return null;
            }
        }
        
        byte[] decodedKey = Base64.decode(encodedKey, Base64.DEFAULT);
        return new SecretKeySpec(decodedKey, "AES");
    }
    
    public static String encrypt(String plaintext, Context context) {
        try {
            SecretKey key = getOrCreateMasterKey(context);
            if (key == null) return null;
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            
            byte[] iv = cipher.getIV();
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV + encrypted data
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            
            return Base64.encodeToString(combined, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Encryption error", e);
            return null;
        }
    }
    
    public static String decrypt(String ciphertext, Context context) {
        try {
            SecretKey key = getOrCreateMasterKey(context);
            if (key == null) return null;
            
            byte[] combined = Base64.decode(ciphertext, Base64.DEFAULT);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e(TAG, "Decryption error", e);
            return null;
        }
    }
}
