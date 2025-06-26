package com.EvgeniG_EladO_HalelF.myapplication;

import android.content.Intent;
import android.os.Bundle;
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


public class NavigationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Navigator mNavigator;
    private SupportNavigationFragment mNavFragment;
    private RoutingOptions mRoutingOptions;
    private boolean locationPermissionGranted = true;
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
        // Setup Bottom Navigation
        NavigationUtils.setupBottomNavBar(this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mNavigator != null) {
            mNavigator.stopGuidance();     // Stops voice + route
            mNavigator.clearDestinations();
            mNavigator.cleanup();
            mNavigator = null;
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
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
                mNavigator = navigator;

                mNavFragment.getMapAsync(googleMap ->
                        googleMap.followMyLocation(CameraPerspective.TILTED));

                mRoutingOptions = new RoutingOptions();
                mRoutingOptions.travelMode(RoutingOptions.TravelMode.DRIVING);

                double lat = getIntent().getDoubleExtra("LAT", 0);
                double lng = getIntent().getDoubleExtra("LNG", 0);
                if(lat != 0){
                    LatLng selectedDestination = new LatLng(lat, lng);
                    navigateToPlace(selectedDestination, mRoutingOptions);
                }

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
            displayMessage("Invalid Location");
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
