package com.example.projectkrs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;

import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ViewHolder> {

    private Context context;
    private List<Place> placesList;

    // Constructor
    public ImageSliderAdapter(Context context, List<Place> placesList) {
        this.context = context;
        this.placesList = placesList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item_image_slider.xml layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_slider, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to views
        Place place = placesList.get(position);

        // Load image using Glide library
        List<PhotoMetadata> photoMetadataList = place.getPhotoMetadatas();
        if (photoMetadataList != null && !photoMetadataList.isEmpty()) {
            // Photo metadata is available
            PhotoMetadata photoMetadata = photoMetadataList.get(0); // Retrieve the first photo metadata
            String photoReference = photoMetadata.zzb(); // Retrieve photo reference
            String apiKey = context.getString(R.string.places_api_key); // Retrieve API key from strings.xml
            String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + apiKey;
            Glide.with(holder.itemView)
                    .load(photoUrl)
                    .apply(RequestOptions.centerCropTransform())
                    .into(holder.imageView);
        }

        // Set place name
        holder.placeNameTextView.setText(place.getName());
    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView placeNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_slider_item);
            placeNameTextView = itemView.findViewById(R.id.imageNameTextView);
        }
    }
    public void updateData(List<Place> newData) {
        placesList.clear();
        placesList.addAll(newData);
        notifyDataSetChanged();
    }

}
