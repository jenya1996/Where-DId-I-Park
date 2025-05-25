package com.EvgeniG_EladO_HalelF.myapplication;

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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.GoogleMap.CameraPerspective;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.libraries.navigation.ListenableResultFuture;
import com.google.android.libraries.navigation.NavigationApi;
import com.google.android.libraries.navigation.Navigator;
import com.google.android.libraries.navigation.RoutingOptions;
import com.google.android.libraries.navigation.SimulationOptions;
import com.google.android.libraries.navigation.SupportNavigationFragment;
import com.google.android.libraries.navigation.Waypoint;

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
    private boolean locationPermissionGranted = false;
    final private String apiKey = "AIzaSyBMkFPXxULLdgu1pU3RDHdvoZIvzMs81XM";

    private LatLng jerusalem = new LatLng(31.7683, 35.2137);
    private LatLng telAviv = new LatLng(32.0853, 34.7818);

    private final ActivityResultLauncher<String> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                locationPermissionGranted = isGranted;
                if (isGranted) {
                    initializeNavigationSdk();
                } else {
                    displayMessage("Location permission denied.");
                }
            });

    void createRoute(LatLng start, LatLng destination){
        mMap.addMarker(new MarkerOptions().position(start).title("start"));
        mMap.addMarker(new MarkerOptions().position(destination).title("destination"));

        double midLat = (start.latitude + destination.latitude) / 2.0;
        double midLng = (start.longitude + destination.longitude) / 2.0;

        LatLng mean = new LatLng(midLat, midLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mean, 9f));

        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + start.latitude + "," + start.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude +
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        checkLocationPermissionAndInitialize();

//        SupportMapFragment mapFragment = (SupportMapFragment)
//                getSupportFragmentManager().findFragmentById(R.id.map);
//
//        if (mapFragment != null) {
//            mapFragment.getMapAsync(this);
//        }

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
                mRoutingOptions.travelMode(RoutingOptions.TravelMode.CYCLING);

                navigateToPlace(telAviv, mRoutingOptions);
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
//            displayMessage("[INFO] - location =[" + location + "]");
            destination = Waypoint.builder().setLatLng(location.latitude, location.longitude).build();
            displayMessage("[INFO] - destination =[" + destination + "]");

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
