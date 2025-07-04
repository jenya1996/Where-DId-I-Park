package com.EvgeniG_EladO_HalelF.wheredidiparked;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import android.location.Address;
import android.location.Geocoder;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends BaseActivity  {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationDatabaseHelper dbHelper;
    private Spinner locationSpinner;
    private List<LatLng> savedLatLngList = new ArrayList<>();
    private EditText noteInput;
    private List<String> savedNotesList = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private static final String PREFS_NAME = "AppSettings";
    private static final String NOTIFICATION_TIME_KEY = "notification_time";
    private RadioGroup navigationModeGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        locationSpinner = findViewById(R.id.location_spinner);
        Button saveButton = findViewById(R.id.save_location_button);
        Button navigateButton = findViewById(R.id.navigate_button);
//        dbHelper = new LocationDatabaseHelper(this);
        dbHelper = DatabaseProvider.get();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);

        noteInput = findViewById(R.id.note_input);

        navigationModeGroup = findViewById(R.id.navigation_mode_group_main);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String defaultMode = prefs.getString("navigation_mode", "walking");

        if ("driving".equals(defaultMode)) {
            navigationModeGroup.check(R.id.driving_mode_main);
        } else {
            navigationModeGroup.check(R.id.walking_mode_main);
        }

        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                googleMap.setMyLocationEnabled(true);

                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(current, 15f));
                    }
                });
            });
        }


        checkLocationPermission();
        saveButton.setOnClickListener(v -> saveCurrentLocation());

        navigateButton.setOnClickListener(v -> {
            int selected = locationSpinner.getSelectedItemPosition();
            if (selected >= 0 && selected < savedLatLngList.size()) {
                LatLng latLng = savedLatLngList.get(selected);
                int selectedModeId = navigationModeGroup.getCheckedRadioButtonId();
                String mode = (selectedModeId == R.id.driving_mode_main) ? "driving" : "walking";

                Intent intent = new Intent(this, NavigationActivity.class);
                intent.putExtra("LAT", latLng.latitude);
                intent.putExtra("LNG", latLng.longitude);
                intent.putExtra("MODE", mode);
                startActivity(intent);
            }
        });

        // Setup Bottom Navigation
        NavigationUtils.setupBottomNavBar(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavigationModeFromPreferences();
        loadRecentLocations(); // reloads spinner after deleting locations
        NavigationUtils.setupBottomNavBar(this);


    }

    private void checkLocationPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            loadRecentLocations();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);

        if (requestCode == LOCATION_PERMISSION_REQUEST &&
                results.length > 0 &&
                results[0] == PackageManager.PERMISSION_GRANTED) {
            loadRecentLocations();
        } else {
            Toast.makeText(this, "GPS permission is required", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCurrentLocation() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(this, "GPS permission not granted", Toast.LENGTH_SHORT).show();
        return;
    }

    fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
        if (location != null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                String label = "Unknown Address";

                try {
                    List<Address> addresses = geocoder.getFromLocation(
                            location.getLatitude(),
                            location.getLongitude(),
                            1
                    );
                    if (addresses != null && !addresses.isEmpty()) {
                        Address addr = addresses.get(0);
                        label = addr.getAddressLine(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String note = noteInput.getText().toString();

                dbHelper.insertLocationWithLabel(
                        location.getLatitude(),
                        location.getLongitude(),
                        label,
                        note
                );

                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                int minutesBefore = prefs.getInt(NOTIFICATION_TIME_KEY, 10);
                scheduleReminder(minutesBefore);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Location saved", Toast.LENGTH_SHORT).show();
                    loadRecentLocations();
                });

                executor.shutdown(); // Cleanly shuts down the thread after task is complete
            });
        } else {
            Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show();
        }
    });
}

    private void loadRecentLocations() {
        Cursor cursor = dbHelper.getAllLocations();
        List<String> labels = new ArrayList<>();
        savedLatLngList.clear();
        savedNotesList.clear();

        if (cursor.moveToLast()) {
            int latIndex = cursor.getColumnIndex("latitude");
            int lngIndex = cursor.getColumnIndex("longitude");
            int labelIndex = cursor.getColumnIndex("label");
            int noteIndex = cursor.getColumnIndex("note");

            do {
                double lat = cursor.getDouble(latIndex);
                double lng = cursor.getDouble(lngIndex);
                String label = cursor.getString(labelIndex);
                String note = cursor.getString(noteIndex);

                savedLatLngList.add(new LatLng(lat, lng));
                savedNotesList.add(note != null ? note : "");
                labels.add(label != null ? label : "Location: " + lat + ", " + lng);
            } while (cursor.moveToPrevious() && savedLatLngList.size() < 5);
        }

        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, labels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);

        locationSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position >= 0 && position < savedNotesList.size()) {
                    noteInput.setText(savedNotesList.get(position));
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                noteInput.setText("");
            }
        });
    }

    private void scheduleReminder(int minutesFromNow) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("title", "Where Did I Park");
        intent.putExtra("message", "\"You saved your parking location. Open the app to view it.\"\n" );

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long triggerAtMillis = System.currentTimeMillis() + (minutesFromNow * 60 * 1000);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }

    private void updateNavigationModeFromPreferences() {
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        String navMode = prefs.getString("navigation_mode", "walking");

        if ("walking".equals(navMode)) {
            navigationModeGroup.check(R.id.walking_mode_main); // or mode_walking depending on your ID
        } else {
            navigationModeGroup.check(R.id.driving_mode_main); // or mode_driving
        }
    }

}
