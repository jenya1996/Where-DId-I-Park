package com.EvgeniG_EladO_HalelF.wheredidiparked;

import android.content.Context;

public class DatabaseProvider {
    private static LocationDatabaseHelper instance;

    public static void init(Context context) {
        if (instance == null) {
            instance = new LocationDatabaseHelper(context.getApplicationContext());
        }
    }

    public static LocationDatabaseHelper get() {
        if (instance == null) {
            throw new IllegalStateException("DatabaseProvider not initialized");
        }
        return instance;
    }
}
