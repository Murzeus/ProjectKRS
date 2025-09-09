package com.example.projectkrs;


import com.example.projectkrs.OptionsDialog;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment implements PostAdapter.OnItemClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final long SLIDE_INTERVAL = 5000;

    private ViewPager2 viewPager;
    private RecyclerView postRecyclerView;
    private ImageSliderAdapter imageSliderAdapter;
    private PostAdapter postAdapter;
    private List<Place> placesList;

    private ImageView dot1, dot2, dot3;

    private final Handler slideHandler = new Handler();
    private Runnable slideRunnable;

    private LatLng userLocation; // Location data

    private Button optionsButton;
    private Map<String, Boolean> openingHoursMap = new HashMap<>(); // Map to store opening hours

    private Context context;




    // Default radius in meters
    private static int DEFAULT_RADIUS = 10000;

    // Default types of places to search for
    private static String DEFAULT_TYPE ="tourist_attraction";

    // Constructor
    public HomeFragment() {
        // Required empty public constructor
    }

    // Setter method for userLocation
    public void setUserLocation(LatLng userLocation) {
        this.userLocation = userLocation;
    }
    // Method to create a new instance of HomeFragment with user location
    public static HomeFragment newInstance(LatLng userLocation) {
        HomeFragment fragment = new HomeFragment();
        fragment.setUserLocation(userLocation);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        viewPager = view.findViewById(R.id.viewPager);
        dot1 = view.findViewById(R.id.dot1);
        dot2 = view.findViewById(R.id.dot2);
        dot3 = view.findViewById(R.id.dot3);
        postRecyclerView = view.findViewById(R.id.postRecyclerView);
        Button optionsButton = view.findViewById(R.id.optionsButton);

        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsDialog(); // Call a method to show the options dialog
            }
        });


        // Retrieve location data passed from HomeActivity
        userLocation = getArguments().getParcelable("user_location");

        // Initialize placesList
        placesList = new ArrayList<>();

        // Set up ViewPager2 adapter
        imageSliderAdapter = new ImageSliderAdapter(getContext(), placesList);
        viewPager.setAdapter(imageSliderAdapter);

        // Set up RecyclerView adapter
        postAdapter = new PostAdapter(placesList);
        postAdapter.setOnItemClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        postRecyclerView.setLayoutManager(layoutManager);
        postRecyclerView.setAdapter(postAdapter);

        fetchUserCategoryFromFirestore();





        // Request location permission if not granted
        requestLocationPermission();
        return view;
    }

    private void fetchUserCategoryFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = currentUser.getUid();

            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String selectedCategory = documentSnapshot.getString("categoryType");
                            if (!TextUtils.isEmpty(selectedCategory)) {
                                // Update the default type with the user-selected category
                                DEFAULT_TYPE = selectedCategory;
                                Log.d("HomeFragment", "User selected category: " + selectedCategory);
                                // Fetch nearby places using the user-selected category
                                fetchNearbyTouristAttractions(userLocation);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HomeFragment", "Error fetching user category type from Firestore", e);
                    });
        }
    }
    public void showOptionsDialog() {
        OptionsDialog optionsDialog = new OptionsDialog(getContext(), this); // Pass the HomeFragment instance
        optionsDialog.show();
    }

    public void handleOptionsDialogResult(int radius, String selectedCategory) {
        // Update default values
        DEFAULT_RADIUS = radius*1000;
        DEFAULT_TYPE = selectedCategory;

        // Call fetchNearbyPlaces again with updated values
        fetchNearbyTouristAttractions(userLocation);
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchNearbyTouristAttractions(userLocation); // Use the location data
        }
    }

    private void fetchNearbyTouristAttractions(LatLng currentLocation) {
        Log.d("HomeFragment", "Fetching nearby places for location: " + currentLocation.latitude + ", " + currentLocation.longitude);
        String apiKey = context.getString(R.string.places_api_key); // Retrieve API key from strings.xml

        new Thread(() -> {
            try {
                String urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=" + currentLocation.latitude + "," + currentLocation.longitude +
                        "&radius=" + DEFAULT_RADIUS +
                        "&type=" + DEFAULT_TYPE +
                        "&key=" + apiKey;
                Log.d("HomeFragment", "RADIUS: " + DEFAULT_RADIUS);
                Log.d("HomeFragment", "TYPES: " + DEFAULT_TYPE);
                Log.d("URL", urlString);


                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray results = jsonResponse.getJSONArray("results");
                List<Place> places = new ArrayList<>();
                for (int i = 0; i < results.length(); i++) {
                    JSONObject placeObject = results.getJSONObject(i);

                    // Extract place details
                    String placeId = placeObject.getString("place_id");
                    String name = placeObject.getString("name");
                    String address = placeObject.getString("vicinity");
                    JSONObject geometryObject = placeObject.getJSONObject("geometry");
                    JSONObject locationObject = geometryObject.getJSONObject("location");
                    double latitude = locationObject.getDouble("lat");
                    double longitude = locationObject.getDouble("lng");

                    LatLng latLng = new LatLng(latitude, longitude);
                    List<PhotoMetadata> photoMetadataList = new ArrayList<>();

                    if (placeObject.has("photos")) {
                        JSONArray photosArray = placeObject.getJSONArray("photos");
                        for (int j = 0; j < photosArray.length(); j++) {
                            JSONObject photoObject = photosArray.getJSONObject(j);
                            String photoReference = photoObject.optString("photo_reference");
                            // Extract additional metadata
                            int width = photoObject.optInt("width");
                            int height = photoObject.optInt("height");
                            JSONArray htmlAttributionsArray = photoObject.optJSONArray("html_attributions");
                            List<String> htmlAttributions = new ArrayList<>();
                            if (htmlAttributionsArray != null) {
                                for (int k = 0; k < htmlAttributionsArray.length(); k++) {
                                    htmlAttributions.add(htmlAttributionsArray.getString(k));
                                }
                            }

                            // Create PhotoMetadata object using the Builder
                            PhotoMetadata photoMetadata = PhotoMetadata.builder(photoReference)
                                    .setWidth(width)
                                    .setHeight(height)
                                    .build();
                            photoMetadataList.add(photoMetadata);
                        }
                    }

                    boolean isOpenNow = false;
                    if (placeObject.has("opening_hours")) {
                        JSONObject openingHoursObject = placeObject.getJSONObject("opening_hours");
                        isOpenNow = openingHoursObject.getBoolean("open_now");
                    }

                    // Store opening hours information in the map
                    openingHoursMap.put(placeId, isOpenNow);


                    // Create a Place object with the photo metadata list
                    Place place = Place.builder()
                            .setId(placeId)
                            .setName(name)
                            .setAddress(address)
                            .setPhotoMetadatas(photoMetadataList) // Set the photo metadata list directly
                            .setLatLng(latLng)// Set other details as needed
                            .build();
                    places.add(place);
                }

                requireActivity().runOnUiThread(() -> onPlacesFetched(places));

            } catch (IOException | JSONException e) {
                Log.e("HomeFragment", "Error fetching nearby places", e);
            }
        }).start();
    }

    private void onPlacesFetched(List<Place> places) {
        // Update UI with the fetched places
        placesList.clear();
        placesList.addAll(places);

        // Adding 3 places to the ViewPager
        List<Place> viewPagerPlaces = new ArrayList<>();
        if (places.size() >= 3) {
            viewPagerPlaces.addAll(places.subList(0, 3)); // Add first 3 places
        } else {
            viewPagerPlaces.addAll(places); // Add all places if less than 3
        }

        // Update the ViewPager adapter with the new list of places
        imageSliderAdapter.updateData(viewPagerPlaces);
        imageSliderAdapter.notifyDataSetChanged();

        // Update the PostAdapter with all places
        postAdapter.updateData(places);
        postAdapter.notifyDataSetChanged();

        updateDots(0); // Update UI elements like dots
        startAutoSlide(); // Start auto sliding
    }

    private void updateDots(int position) {
        switch (position) {
            case 0:
                dot1.setImageResource(R.drawable.dot_selected);
                dot2.setImageResource(R.drawable.dot_unselected);
                dot3.setImageResource(R.drawable.dot_unselected);
                break;
            case 1:
                dot1.setImageResource(R.drawable.dot_unselected);
                dot2.setImageResource(R.drawable.dot_selected);
                dot3.setImageResource(R.drawable.dot_unselected);
                break;
            case 2:
                dot1.setImageResource(R.drawable.dot_unselected);
                dot2.setImageResource(R.drawable.dot_unselected);
                dot3.setImageResource(R.drawable.dot_selected);
                break;
        }
    }

    private void startAutoSlide() {
        slideHandler.removeCallbacks(slideRunnable);
        slideRunnable = new Runnable() {
            @Override
            public void run() {
                int nextSlide = viewPager.getCurrentItem() + 1;
                if (nextSlide >= 3) {
                    nextSlide = 0;
                }
                viewPager.setCurrentItem(nextSlide, true);
                updateDots(nextSlide);
                slideHandler.postDelayed(this, SLIDE_INTERVAL);
            }
        };
        slideHandler.postDelayed(slideRunnable, SLIDE_INTERVAL);
    }

    private boolean getOpeningHours(String placeId) {
        return openingHoursMap.getOrDefault(placeId, false); // Default value is false if place ID not found
    }


    @Override
    public void onItemClick(int position) {
        Place selectedPlace = placesList.get(position);
        boolean isOpenNow = getOpeningHours(selectedPlace.getId());
        openDetailActivity(selectedPlace, isOpenNow);
    }
    private void openDetailActivity(Place place, boolean isOpenNow) {
        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
        intent.putExtra("place", place); // Pass the Place object
        intent.putExtra("isOpenNow", isOpenNow); // Pass the opening hours information
        startActivity(intent);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        slideHandler.removeCallbacks(slideRunnable);
    }
}