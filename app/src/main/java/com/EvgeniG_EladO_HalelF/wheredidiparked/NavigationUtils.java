package com.EvgeniG_EladO_HalelF.wheredidiparked;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationUtils {
    public static void setupBottomNavBar(Activity activity) {
        BottomNavigationView nav = activity.findViewById(R.id.bottom_navigation);

        // Set the selected item BEFORE setting the listener
        highlightTab(activity, nav);

        nav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Prevent reloading the same activity
            if (itemId == R.id.nav_home && activity instanceof MainActivity) return true;
            if (itemId == R.id.nav_map && activity instanceof NavigationActivity) return true;
            if (itemId == R.id.nav_settings && activity instanceof SettingsActivity) return true;

            Intent intent = null;

            if (itemId == R.id.nav_home) {
                intent = new Intent(activity, MainActivity.class);
            } else if (itemId == R.id.nav_map) {
                intent = new Intent(activity, NavigationActivity.class);
            } else if (itemId == R.id.nav_settings) {
                intent = new Intent(activity, SettingsActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }

    private static void highlightTab(Activity activity, BottomNavigationView nav) {
        if (activity instanceof MainActivity) {
            nav.setSelectedItemId(R.id.nav_home);
        } else if (activity instanceof NavigationActivity) {
            nav.setSelectedItemId(R.id.nav_map);
        } else if (activity instanceof SettingsActivity) {
            nav.setSelectedItemId(R.id.nav_settings);
        }

    }
}
