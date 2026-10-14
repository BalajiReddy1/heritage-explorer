package com.example.heritage.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.heritage.R;
import com.example.heritage.util.Ui;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Home shell with three tabs. Fragments are created once and shown or hidden,
 * so the Explore search and scroll position survive switching tabs.
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_TAB = "tab";

    private static final String TAG_EXPLORE = "explore";
    private static final String TAG_TRIPS = "trips";
    private static final String TAG_PROFILE = "profile";

    private BottomNavigationView bottomNav;

    /** Opens the home screen on a given tab, reusing the existing instance. */
    public static Intent intent(Context context, int tabId) {
        return new Intent(context, MainActivity.class)
                .putExtra(EXTRA_TAB, tabId)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Ui.padForSystemBars(findViewById(R.id.fragment_container), true, false);

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new ExploreFragment(), TAG_EXPLORE)
                    .add(R.id.fragment_container, new TripsFragment(), TAG_TRIPS)
                    .add(R.id.fragment_container, new ProfileFragment(), TAG_PROFILE)
                    .commitNow();
            bottomNav.setSelectedItemId(getIntent().getIntExtra(EXTRA_TAB, R.id.nav_explore));
            showTab(bottomNav.getSelectedItemId());
        }
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(EXTRA_TAB)) {
            bottomNav.setSelectedItemId(intent.getIntExtra(EXTRA_TAB, R.id.nav_explore));
        }
    }

    private void showTab(int itemId) {
        String tag;
        if (itemId == R.id.nav_trips) tag = TAG_TRIPS;
        else if (itemId == R.id.nav_profile) tag = TAG_PROFILE;
        else tag = TAG_EXPLORE;

        FragmentManager fm = getSupportFragmentManager();
        androidx.fragment.app.FragmentTransaction tx = fm.beginTransaction();
        for (String t : new String[]{TAG_EXPLORE, TAG_TRIPS, TAG_PROFILE}) {
            Fragment f = fm.findFragmentByTag(t);
            if (f == null) continue;
            if (t.equals(tag)) tx.show(f);
            else tx.hide(f);
        }
        tx.commit();
    }
}
