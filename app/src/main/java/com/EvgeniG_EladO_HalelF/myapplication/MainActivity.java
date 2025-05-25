package com.EvgeniG_EladO_HalelF.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Uses XML layout

        // Set text in TextView
//        TextView textView = findViewById(R.id.textView);
//        textView.setText("MainActivity loaded successfully");

        Spinner locationSpinner = findViewById(R.id.location_spinner);

// Fake location names
        String[] locationNames = {
                "Tel Aviv - Azrieli",
                "Jerusalem - Central Station",
                "Haifa - Carmel Beach",
                "Be'er Sheva - University",
                "Ramat Gan - Safari"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, locationNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);

        // Setup Bottom Navigation
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(R.id.nav_home); // Mark current tab as selected

        nav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true; // Already here
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
}
