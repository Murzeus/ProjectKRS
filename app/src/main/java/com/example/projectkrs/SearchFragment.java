package com.example.projectkrs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class SearchFragment extends Fragment implements PostAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private PostAdapter searchAdapter;
    private List<PlaceWithDistance> searchResults;

    private LatLng userLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Retrieve user location passed from HomeActivity
        userLocation = getArguments().getParcelable("user_location");

        // Initialize RecyclerView and adapter
        recyclerView = view.findViewById(R.id.searchRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResults = new ArrayList<>();
        searchAdapter = new PostAdapter(searchResults);
        searchAdapter.setOnItemClickListener(this);

        recyclerView.setAdapter(searchAdapter);

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.places_api_key));
        }

        // Initialize SearchView
        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform search when the user submits the query
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Clear search results when the query text changes
                searchResults.clear();
                searchAdapter.notifyDataSetChanged();
                return true;
            }
        });

        return view;
    }

    @Override
    public void onItemClick(int position) {
        // Handle item click here
        // For example, you can open the PostDetailActivity with the clicked item's data
        PlaceWithDistance clickedPlace = searchResults.get(position);
        // Log the clicked place details
        Log.d("SearchFragment", "Clicked Place: " + clickedPlace.getPlace().getName());
        // Open PostDetailActivity with place details
        openPostDetailActivity(clickedPlace.getPlace());
    }

    private void openPostDetailActivity(Place place) {
        // Log the place details before creating the intent
        Log.d("SearchFragment", "Opening PostDetailActivity for Place: " + place.getName());
        // Open PostDetailActivity with place object
        Intent intent = new Intent(requireContext(), PostDetailActivity.class);
        intent.putExtra("place", place); // Pass the Place object directly
        startActivity(intent);
    }

    private void performSearch(String query) {
        // Use the Places API to perform a text search based on the user's query
        PlacesClient placesClient = Places.createClient(requireContext());

        // Create a new request for autocomplete predictions with location bias
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setLocationBias(RectangularBounds.newInstance(
                        LatLngBounds.builder().include(userLocation).build()
                ))
                .build();

        // Fetch predictions asynchronously
        placesClient.findAutocompletePredictions(request).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                if (task.isSuccessful()) {
                    // Process the predictions and fetch details for each place
                    for (com.google.android.libraries.places.api.model.AutocompletePrediction prediction : Objects.requireNonNull(task.getResult()).getAutocompletePredictions()) {
                        // Fetch details for each place
                        fetchPlaceDetails(prediction.getPlaceId(), prediction.getFullText(null).toString());
                    }
                } else {
                    // Handle errors
                }
            }
        });
    }

    private void fetchPlaceDetails(String placeId, String fullText) {
        PlacesClient placesClient = Places.createClient(requireContext());

        // Specify the fields to be retrieved
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHOTO_METADATAS, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        // Create a FetchPlaceRequest
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        // Fetch place details asynchronously
        placesClient.fetchPlace(request).addOnCompleteListener(new OnCompleteListener<FetchPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FetchPlaceResponse> task) {
                if (task.isSuccessful()) {
                    // Get the retrieved place
                    Place place = task.getResult().getPlace();
                    // Calculate distance
                    double distance = SphericalUtil.computeDistanceBetween(userLocation, place.getLatLng());
                    // Create PlaceWithDistance object
                    PlaceWithDistance placeWithDistance = new PlaceWithDistance(place, distance);
                    // Add the place to the search results
                    searchResults.add(placeWithDistance);
                    // Sort the search results based on distance
                    Collections.sort(searchResults, new Comparator<PlaceWithDistance>() {
                        @Override
                        public int compare(PlaceWithDistance p1, PlaceWithDistance p2) {
                            return Double.compare(p1.getDistance(), p2.getDistance());
                        }
                    });
                    // Update the RecyclerView
                    searchAdapter.notifyDataSetChanged();
                } else {
                    // Handle errors
                }
            }
        });
    }
}
