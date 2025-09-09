package com.example.projectkrs;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private LatLng userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize bottom navigation view
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Retrieve user location passed from MainActivity
        userLocation = getIntent().getParcelableExtra("user_location");

        // Set listener for bottom navigation item clicks
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    // Create a new instance of HomeFragment
                    HomeFragment homeFragment = new HomeFragment();

                    // Set arguments for the fragment
                    Bundle args = new Bundle();
                    args.putParcelable("user_location", userLocation);
                    homeFragment.setArguments(args);

                    // Replace the fragment in the container
                    replaceFragment(homeFragment);
                    return true;
                case R.id.navigation_search:
                    // Create a new instance of SearchFragment
                    SearchFragment searchFragment = new SearchFragment();

                    // Set arguments for the fragment
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("user_location", userLocation);
                    searchFragment.setArguments(bundle);

                    // Replace the fragment in the container
                    replaceFragment(searchFragment);
                    return true;
                case R.id.navigation_profile:
                    replaceFragment(new ProfileFragment());
                    return true;
                default:
                    return false;
            }
        });

        // Set the default fragment to display when HomeActivity is first opened
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    // Method to replace fragments in the container
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}