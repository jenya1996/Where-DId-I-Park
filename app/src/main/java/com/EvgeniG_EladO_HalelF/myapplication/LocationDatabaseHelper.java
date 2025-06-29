package com.EvgeniG_EladO_HalelF.myapplication;

import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class LocationDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "locationDB";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_NAME = "locations";
    private static final String COL_ID = "id";
    private static final String COL_LAT = "latitude";
    private static final String COL_LNG = "longitude";
    private static final String COL_LABEL = "label";
    private static final String COL_NOTE = "note";

    public LocationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_LAT + " REAL, " +
                COL_LNG + " REAL, " +
                COL_LABEL + " TEXT, " +
                COL_NOTE + " TEXT)"; // ← include note
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Upgrade path: v1 → v2 adds label, v2 → v3 adds note
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_LABEL + " TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_NOTE + " TEXT");
        }
    }

    public void insertLocation(double lat, double lng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_LAT, lat);
        values.put(COL_LNG, lng);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void insertLocation(LatLng location) {
        insertLocation(location.latitude, location.longitude);
    }

    public void insertLocationWithLabel(double lat, double lng, String label, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_LAT, lat);
        values.put(COL_LNG, lng);
        values.put(COL_LABEL, label);
        values.put(COL_NOTE, note);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void insertLocationWithLabelAndNote(double lat, double lng, String label, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_LAT, lat);
        values.put(COL_LNG, lng);
        values.put(COL_LABEL, label);
        values.put(COL_NOTE, note); // ← added note
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public int deleteAllLocations() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("locations", null, null);
    }

    public Location getLastLocation() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_ID + " DESC LIMIT 1",
                null
        );

        Location location = null;
        if (cursor.moveToFirst()) {
            double lat = cursor.getDouble(cursor.getColumnIndex(COL_LAT));
            double lng = cursor.getDouble(cursor.getColumnIndex(COL_LNG));
            location = new Location("");
            location.setLatitude(lat);
            location.setLongitude(lng);
        }

        cursor.close();
        db.close();
        return location;
    }

    public LatLng getLastLatLng() {
        Location loc = getLastLocation();
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }

    public Cursor getAllLocations() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }
}
