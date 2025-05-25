package com.EvgeniG_EladO_HalelF.myapplication;
import com.EvgeniG_EladO_HalelF.myapplication.PolylineDecoder;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.graphics.Color;
//import android.os.Bundle;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.FragmentActivity;
//
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

//import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
//import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;



public class NavigationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Setup Bottom Navigation
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(R.id.nav_map); // Mark current tab as selected

        nav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_map) {
                return true; // Already here
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng jerusalem = new LatLng(31.7683, 35.2137);
        LatLng telAviv = new LatLng(32.0853, 34.7818);
//        LatLng ramatGan = new LatLng(32.0809, 34.8148);

        mMap.addMarker(new MarkerOptions().position(jerusalem).title("Jerusalem"));
        mMap.addMarker(new MarkerOptions().position(telAviv).title("Tel Aviv"));
//        mMap.addMarker(new MarkerOptions().position(ramatGan).title("Ramat Gan"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jerusalem, 8f));

        String apiKey = "AIzaSyBMkFPXxULLdgu1pU3RDHdvoZIvzMs81XM";

        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + jerusalem.latitude + "," + jerusalem.longitude +
                "&destination=" + telAviv.latitude + "," + telAviv.longitude +
                "&key=" + apiKey;

        new Thread(() -> {
            try {
                URL directionUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) directionUrl.openConnection();
                conn.connect();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);

                JSONObject json = new JSONObject(result.toString());
                String encoded = json.getJSONArray("routes")
                        .getJSONObject(0)
                        .getJSONObject("overview_polyline")
                        .getString("points");

                List<LatLng> points = PolylineDecoder.decode(encoded);

                runOnUiThread(() -> {
                    mMap.addPolyline(new PolylineOptions()
                            .addAll(points)
                            .color(Color.BLUE)
                            .width(10f));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }
}
