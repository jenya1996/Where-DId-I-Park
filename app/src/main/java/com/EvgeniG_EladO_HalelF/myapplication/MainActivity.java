package com.EvgeniG_EladO_HalelF.myapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.navigation.NavigationActivity;
import com.google.android.libraries.navigation.NavigationActivityIntentBuilder;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.startNavBtn).setOnClickListener(v -> {
            LatLng origin = new LatLng(31.7683, 35.2137);
            LatLng destination = new LatLng(32.0853, 34.7818);

            Intent navIntent = new NavigationActivityIntentBuilder()
                    .setOrigin(origin)
                    .setDestination(destination)
                    .build(this);

            startActivity(navIntent);
        });


    }
}