package com.example.heritage.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Account, login state and settings stored in SharedPreferences.
 * The password is never stored as plain text, only its SHA-256 hash.
 */
public class SessionManager {

    public static final int MAX_ATTEMPTS = 3;
    public static final long LOCK_MILLIS = 30_000;

    private static final String PREFS = "virasat_session";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD_HASH = "password_hash";
    private static final String KEY_REMEMBER = "remember_me";
    private static final String KEY_FAILED = "failed_attempts";
    private static final String KEY_LOCKED_UNTIL = "locked_until";
    private static final String KEY_THEME = "theme_mode";

    /** True after signing in without "Keep me signed in". Lasts until the app process ends. */
    private static boolean signedInThisLaunch;

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ---------- Account ----------

    public boolean hasAccount() {
        return prefs.contains(KEY_EMAIL);
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public void register(String name, String email, String password) {
        prefs.edit()
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .putString(KEY_PASSWORD_HASH, hash(password))
                .putInt(KEY_FAILED, 0)
                .putLong(KEY_LOCKED_UNTIL, 0)
                .apply();
    }

    public boolean checkCredentials(String email, String password) {
        return hasAccount()
                && email.equalsIgnoreCase(getEmail())
                && hash(password).equals(prefs.getString(KEY_PASSWORD_HASH, ""));
    }

    // ---------- Session ----------

    public void startSession(boolean remember) {
        signedInThisLaunch = true;
        prefs.edit().putBoolean(KEY_REMEMBER, remember).putInt(KEY_FAILED, 0).apply();
    }

    public boolean isSignedIn() {
        return signedInThisLaunch || prefs.getBoolean(KEY_REMEMBER, false);
    }

    public void signOut() {
        signedInThisLaunch = false;
        prefs.edit().putBoolean(KEY_REMEMBER, false).apply();
    }

    // ---------- Failed attempts and lock ----------

    /** Records a wrong password and returns the attempt number. Locks sign in on the last one. */
    public int recordFailedAttempt() {
        int attempts = prefs.getInt(KEY_FAILED, 0) + 1;
        SharedPreferences.Editor editor = prefs.edit().putInt(KEY_FAILED, attempts);
        if (attempts >= MAX_ATTEMPTS) {
            editor.putLong(KEY_LOCKED_UNTIL, System.currentTimeMillis() + LOCK_MILLIS);
        }
        editor.apply();
        return attempts;
    }

    public long lockRemainingMillis() {
        long remaining = prefs.getLong(KEY_LOCKED_UNTIL, 0) - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public void clearLock() {
        prefs.edit().putInt(KEY_FAILED, 0).putLong(KEY_LOCKED_UNTIL, 0).apply();
    }

    // ---------- Settings ----------

    public int getThemeMode() {
        return prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    private static String hash(String text) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
