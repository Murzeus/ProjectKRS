package com.example.projectkrs;

import static android.view.View.GONE;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;

import java.util.List;

public class PostDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Get the Place object from the intent
        Place place = getIntent().getParcelableExtra("place");
        boolean isOpenNow = getIntent().getBooleanExtra("isOpenNow", false);

        Button openMapsButton = findViewById(R.id.openMapsButton);
        openMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (place != null) {
                    // Extract latitude and longitude coordinates from the Place object
                    double latitude = place.getLatLng().latitude;
                    double longitude = place.getLatLng().longitude;

                    // Create an Intent to open maps with the location coordinates
                    Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(" + place.getName() + ")");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps"); // Use Google Maps app
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    } else {
                        // If Google Maps app is not available, handle it gracefully
                        Toast.makeText(getApplicationContext(), "Google Maps app is not installed.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("PostDetailActivity", "No Place object found in intent extras");
                }
            }
        });


        if (place != null) {
            Log.d("PostDetailActivity", "Place ID: " + place.getId());
            Log.d("PostDetailActivity", "Place Name: " + place.getName());
            Log.d("PostDetailActivity", "Place Address: " + place.getAddress());
            Log.d("PostDetailActivity", "Place Photo Metadatas: " + place.getPhotoMetadatas());

            // Set up views
            ImageView imageView = findViewById(R.id.imageView);
            TextView textViewTitle = findViewById(R.id.textViewTitle);
            TextView textViewDescription = findViewById(R.id.textViewDescription);

            // Set image, name, and description
            List<PhotoMetadata> photoMetadataList = place.getPhotoMetadatas();
            if (photoMetadataList != null && !photoMetadataList.isEmpty()) {
                // Photo metadata is available
                PhotoMetadata photoMetadata = photoMetadataList.get(0); // Retrieve the first photo metadata
                String photoReference = photoMetadata.zzb(); // Retrieve photo reference
                String imageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + getString(R.string.places_api_key);
                Glide.with(this).load(imageUrl).into(imageView);
            } else {
                // No photo available, load placeholder image
                Glide.with(this).load(R.drawable.no_image_placeholder).into(imageView);
            }
            textViewTitle.setText(place.getName());
            textViewDescription.setText(place.getAddress());
            TextView textViewOpeningHours = findViewById(R.id.textViewOpeningHours);
            boolean isOpenNowAvailable = getIntent().hasExtra("isOpenNow");

            if (isOpenNowAvailable) {
                textViewOpeningHours.setText(isOpenNow ? "Atidaryta" : "Uždaryta");
            } else {
                // Information not available, so hide the TextView
                textViewOpeningHours.setVisibility(GONE);
            }
        } else {
            Log.e("PostDetailActivity", "No Place object found in intent extras");
        }
    }
}
