package com.EvgeniG_EladO_HalelF.myapplication;

import static androidx.fragment.app.FragmentManager.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.gms.maps.model.LatLng;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationDatabaseHelper dbHelper;
    private Spinner locationSpinner;
    private List<LatLng> savedLatLngList = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationSpinner = findViewById(R.id.location_spinner);
        Button saveButton = findViewById(R.id.save_location_button);
        Button navigateButton = findViewById(R.id.navigate_button);
        dbHelper = new LocationDatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);

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
//        this.deleteDatabase("locationDB"); // DEV ONLY: clears DB to rebuild schema

        saveButton.setOnClickListener(v -> saveCurrentLocation());

        navigateButton.setOnClickListener(v -> {
            int selected = locationSpinner.getSelectedItemPosition();
            if (selected >= 0 && selected < savedLatLngList.size()) {
                LatLng latLng = savedLatLngList.get(selected);
                Intent intent = new Intent(this, NavigationActivity.class);
                intent.putExtra("LAT", latLng.latitude);
                intent.putExtra("LNG", latLng.longitude);
                startActivity(intent);
            }
        });

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(R.id.nav_home);
        nav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true; // Already on this screen
            } else if (itemId == R.id.nav_map) {
                startActivity(new Intent(this, NavigationActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;

        });
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

//    private void saveCurrentLocation() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "GPS permission not granted", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
//            if (location != null) {
//                dbHelper.insertLocation(location.getLatitude(), location.getLongitude());
//                Toast.makeText(this, "Location saved", Toast.LENGTH_SHORT).show();
//                loadRecentLocations();
//            } else {
//                Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//        try {
//            List<Address> addresses = geocoder.getFromLocation(
//                    location.getLatitude(),
//                    location.getLongitude(),
//                    1
//            );
//
//            String label = "Unknown Location";
//            if (addresses != null && !addresses.isEmpty()) {
//                Address addr = addresses.get(0);
//                label = addr.getFeatureName(); // or addr.getAddressLine(0)
//            }
//
//            // Save to DB with label
//            dbHelper.insertLocationWithLabel(location.getLatitude(), location.getLongitude(), label);
//
//            Toast.makeText(this, "Location saved", Toast.LENGTH_SHORT).show();
//            loadRecentLocations();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Could not retrieve address", Toast.LENGTH_SHORT).show();
//        }
//
//    }

    private void saveCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "GPS permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(
                            location.getLatitude(),
                            location.getLongitude(),
                            1
                    );

                    String label = "Unknown Address";
                    if (addresses != null && !addresses.isEmpty()) {
                        Address addr = addresses.get(0);
                        label = addr.getAddressLine(0);
                    }

                    dbHelper.insertLocationWithLabel(
                            location.getLatitude(),
                            location.getLongitude(),
                            label
                    );

//                    Log.d(TAG, "saveCurrentLocation: " + label); // debug message
                    Toast.makeText(this, "Location saved", Toast.LENGTH_SHORT).show();
                    loadRecentLocations();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Could not retrieve address", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadRecentLocations() {
        Cursor cursor = dbHelper.getAllLocations();
        List<String> labels = new ArrayList<>();
        savedLatLngList.clear();

        if (cursor.moveToLast()) {
            int latIndex = cursor.getColumnIndex("latitude");
            int lngIndex = cursor.getColumnIndex("longitude");

            do {
                double lat = cursor.getDouble(latIndex);
                double lng = cursor.getDouble(lngIndex);
                savedLatLngList.add(new LatLng(lat, lng));
                String label = cursor.getString(cursor.getColumnIndex("label"));
                labels.add(label != null ? label : "Location: " + lat + ", " + lng);
            } while (cursor.moveToPrevious() && savedLatLngList.size() < 5);
        }

        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, labels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);
    }
}
