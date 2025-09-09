package com.example.projectkrs;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is already logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // User is already logged in, set the content view to splash screen
            setContentView(R.layout.splash_screen);

        } else {
            // User is not logged in, continue with existing logic
            setContentView(R.layout.activity_main);
        }
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Request location permission
        requestLocationPermission();

    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            try {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                Log.d("MainActivity", "User location: " + userLocation.latitude + ", " + userLocation.longitude);

                                // Check if the user is already logged in
                                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (user != null) {
                                    // User is already logged in, navigate to HomeActivity
                                    Log.d("openning home", "User location: " + userLocation.latitude + ", " + userLocation.longitude);
                                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                    intent.putExtra("user_location", userLocation);
                                    startActivity(intent);
                                    finish();
                                } else {
                                }
                            } else {
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("MainActivity", "Failed to get location", e);
                            // Handle failure to get location
                            // For example, show a message to the user
                        });
            } catch (SecurityException e) {
                Log.e("MainActivity", "Security Exception occurred", e);
                // Handle security exception
            }
        }
    }


    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                // Handle denial of location permission
                // For example, show a message to the user
            }
        }
    }

    // Method to handle click on Login button
    public void loginOnClick(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    // Method to handle click on Sign Up button
    public void signUpOnClick(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
