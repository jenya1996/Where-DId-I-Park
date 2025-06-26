package com.EvgeniG_EladO_HalelF.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.fragment.app.FragmentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.GoogleMap.CameraPerspective;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.libraries.navigation.ListenableResultFuture;
import com.google.android.libraries.navigation.NavigationApi;
import com.google.android.libraries.navigation.Navigator;
import com.google.android.libraries.navigation.RoutingOptions;
import com.google.android.libraries.navigation.SimulationOptions;
import com.google.android.libraries.navigation.SupportNavigationFragment;
import com.google.android.libraries.navigation.Waypoint;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class NavigationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Navigator mNavigator;
    private SupportNavigationFragment mNavFragment;
    private RoutingOptions mRoutingOptions;
    private boolean locationPermissionGranted = true;

    private LatLng jerusalem = new LatLng(31.7683, 35.2137);
    private LatLng telAviv = new LatLng(32.0853, 34.7818);
    private LocationDatabaseHelper locationDB = new LocationDatabaseHelper(this);
    private final ActivityResultLauncher<String> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                locationPermissionGranted = isGranted;
                if (isGranted) {
                    initializeNavigationSdk();
                } else {
                    displayMessage("Location permission denied.");
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);




        //this the is where the navigation code starts
        checkLocationPermissionAndInitialize();
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
        nav.setSelectedItemId(R.id.nav_map); // Mark current tab as selected

        nav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true; // Already here
            } else if (itemId == R.id.nav_map) {

                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });



//        BottomNavigationView nav = findViewById(R.id.bottom_navigation);
//        nav.setSelectedItemId(R.id.nav_map);
//        nav.setOnItemSelectedListener(item -> {
//            int itemId = item.getItemId();
//            if (itemId == R.id.nav_home) {
//                startActivity(new Intent(this, MainActivity.class));
//                overridePendingTransition(0, 0);
//                return true;
//            } else if (itemId == R.id.nav_map) {
//                return true; // Already on this screen
//            } else if (itemId == R.id.nav_settings) {
//                startActivity(new Intent(this, SettingsActivity.class));
//                overridePendingTransition(0, 0);
//                return true;
//            }
//            return false;
//
//        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
//        createRoute(jerusalem, telAviv);
    }

    private void checkLocationPermissionAndInitialize() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            initializeNavigationSdk();
        } else {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void initializeNavigationSdk() {
        mNavFragment =
                (SupportNavigationFragment)
                        getSupportFragmentManager().findFragmentById(R.id.navigation_fragment);

        NavigationApi.getNavigator(this, new NavigationApi.NavigatorListener() {
            @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
            @Override
            public void onNavigatorReady(@NonNull Navigator navigator) {
                displayMessage("Navigator ready.");
                mNavigator = navigator;

                mNavFragment.getMapAsync(googleMap ->
                        googleMap.followMyLocation(CameraPerspective.TILTED));

                mRoutingOptions = new RoutingOptions();

                String mode = getIntent().getStringExtra("MODE");
                if ("walking".equals(mode)) {
                    mRoutingOptions.travelMode(RoutingOptions.TravelMode.WALKING);
                } else {
                    mRoutingOptions.travelMode(RoutingOptions.TravelMode.DRIVING);
                }


//                locationDB.insertLocation(11.2222, 33.4444);
//                locationDB.insertLocation(telAviv);
//                LatLng placeToNav = locationDB.getLastLatLng();

                double lat = getIntent().getDoubleExtra("LAT", 32.0853);
                double lng = getIntent().getDoubleExtra("LNG", 34.7818);
                LatLng selectedDestination = new LatLng(lat, lng);
                navigateToPlace(selectedDestination, mRoutingOptions);

            }

            @Override
            public void onError(@NavigationApi.ErrorCode int errorCode) {
                switch (errorCode) {
                    case NavigationApi.ErrorCode.NOT_AUTHORIZED:
                        displayMessage("Error: API key not authorized.");
                        break;
                    case NavigationApi.ErrorCode.TERMS_NOT_ACCEPTED:
                        displayMessage("Error: Terms of use not accepted.");
                        break;
                    case NavigationApi.ErrorCode.LOCATION_PERMISSION_MISSING:
                        displayMessage("Error: Location permission missing.");
                        break;
                    default:
                        displayMessage("Error code: " + errorCode);
                }
            }
        });
    }

    private void navigateToPlace(LatLng location, RoutingOptions travelMode) {
        Waypoint destination;
        try {
            destination = Waypoint.builder().setLatLng(location.latitude, location.longitude).build();

        } catch (IllegalArgumentException e) {
            displayMessage("Invalid LatLng");
            return;
        }

        ListenableResultFuture<Navigator.RouteStatus> pendingRoute =
                mNavigator.setDestination(destination, travelMode);

        pendingRoute.setOnResultListener(result -> {

            switch (result) {
                case OK:
                    if (getActionBar() != null) {
                        getActionBar().hide();
                    }
                    mNavigator.setAudioGuidance(Navigator.AudioGuidance.VOICE_ALERTS_AND_GUIDANCE);
                    if (BuildConfig.DEBUG) {
                        mNavigator.getSimulator().simulateLocationsAlongExistingRoute(
                                new SimulationOptions().speedMultiplier(5));
                    }
                    mNavigator.startGuidance();
                    break;
                case NO_ROUTE_FOUND:
                    displayMessage("No route found.");
                    break;
                case NETWORK_ERROR:
                    displayMessage("Network error.");
                    break;
                case ROUTE_CANCELED:
                    displayMessage("Route canceled.");
                    break;
                default:
                    displayMessage("Route error: " + result);
                    break;
            }
        });
    }

    private void displayMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        Log.d(TAG, msg);
    }
}
