package com.EvgeniG_EladO_HalelF.myapplication;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationUtils {
    public static void setupBottomNavBar(Activity activity) {
        BottomNavigationView nav = activity.findViewById(R.id.bottom_navigation);

        // Highlight the current tab
        if (activity instanceof MainActivity) {
            nav.setSelectedItemId(R.id.nav_home);
        } else if (activity instanceof NavigationActivity) {
            nav.setSelectedItemId(R.id.nav_map);
        } else if (activity instanceof SettingsActivity) {
            nav.setSelectedItemId(R.id.nav_settings);
        }

        nav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent;

            if (itemId == R.id.nav_map && activity instanceof NavigationActivity) return true;
            if (itemId == R.id.nav_home && activity instanceof MainActivity) return true;
            if (itemId == R.id.nav_settings && activity instanceof SettingsActivity) return true;

            if (itemId == R.id.nav_map) {
                intent = new Intent(activity, NavigationActivity.class);
            } else if (itemId == R.id.nav_home) {
                intent = new Intent(activity, MainActivity.class);
            } else if (itemId == R.id.nav_settings) {
                intent = new Intent(activity, SettingsActivity.class);
            } else {
                return false;
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0);
            return true;
        });
    }
}

