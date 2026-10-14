package com.example.heritage;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.heritage.data.SessionManager;

public class VirasatApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Apply the saved light/dark choice before any screen is shown
        AppCompatDelegate.setDefaultNightMode(new SessionManager(this).getThemeMode());
    }
}
