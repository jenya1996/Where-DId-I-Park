package com.EvgeniG_EladO_HalelF.myapplication;

import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.fragment.app.FragmentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap.CameraPerspective;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.libraries.navigation.SupportNavigationFragment;
import com.google.android.libraries.navigation.ListenableResultFuture;
import com.google.android.libraries.navigation.NavigationApi;
import com.google.android.libraries.navigation.Navigator;
import com.google.android.libraries.navigation.RoutingOptions;
import com.google.android.libraries.navigation.SimulationOptions;
import com.google.android.libraries.navigation.CustomControlPosition;
import com.google.android.libraries.navigation.Waypoint;

public class NavigationActivity extends FragmentActivity {

    private static final String TAG = NavigationActivity.class.getSimpleName();
    private Navigator mNavigator;
    private SupportNavigationFragment mNavFragment;
    private RoutingOptions mRoutingOptions;
    private boolean locationPermissionGranted = true;
    private Button stopButton;
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
            stopButton.setVisibility(View.GONE);
        }
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

                String mode = getIntent().getStringExtra("MODE");
                if ("walking".equals(mode)) {
                    mRoutingOptions.travelMode(RoutingOptions.TravelMode.WALKING);
                } else {
                    mRoutingOptions.travelMode(RoutingOptions.TravelMode.DRIVING);
                }


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

        View stopButtonView = getLayoutInflater().inflate(R.layout.view_stop_button, null);
        stopButton = stopButtonView.findViewById(R.id.btn_stop_guidance);

        stopButton.setOnClickListener(v -> {
            if (mNavigator != null) {
                mNavigator.stopGuidance();
                mNavigator.clearDestinations();
                mNavigator.cleanup();
                stopButton.setVisibility(View.GONE);
            }
            displayMessage("Navigation stopped.");
            finish();
        });

        // Add the button inside the Google UI at the bottom-right corner (adaptive)
        mNavFragment.setCustomControl(stopButtonView, CustomControlPosition.BOTTOM_END_BELOW);
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
                    stopButton.setVisibility(View.VISIBLE);


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
