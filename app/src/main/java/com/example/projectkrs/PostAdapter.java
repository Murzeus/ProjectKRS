package com.example.projectkrs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;

import java.util.List;

public class PostAdapter<T> extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<T> itemsList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public PostAdapter(List<T> itemsList) {
        this.itemsList = itemsList;
    }

    public void updateData(List<T> newData) {
        itemsList.clear();
        itemsList.addAll(newData);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        T item = itemsList.get(position);
        if (item instanceof Place) {
            holder.bind((Place) item);
        } else if (item instanceof PlaceWithDistance) {
            holder.bind((PlaceWithDistance) item);
        }
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView nameTextView;

        public PostViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);

            // Set click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }

        public void bind(Place place) {
            List<PhotoMetadata> photoMetadataList = place.getPhotoMetadatas();
            if (photoMetadataList != null && !photoMetadataList.isEmpty()) {
                // Photo metadata is available
                PhotoMetadata photoMetadata = photoMetadataList.get(0); // Retrieve the first photo metadata
                String photoReference = photoMetadata.zzb(); // Retrieve photo reference
                String imageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + itemView.getContext().getString(R.string.places_api_key);
                Glide.with(itemView)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image) // Placeholder image resource
                        .error(R.drawable.error_image) // Error image resource
                        .into(imageView);
                System.out.println("Image URL: " + imageUrl);
            } else {
                // No photo metadata available, load placeholder image
                Glide.with(itemView)
                        .load(R.drawable.no_image_placeholder) // Placeholder image for no photo
                        .into(imageView);
            }

            // Set name
            nameTextView.setText(place.getName());
        }

        public void bind(PlaceWithDistance placeWithDistance) {
            Place place = placeWithDistance.getPlace();
            double distance = placeWithDistance.getDistance();

            List<PhotoMetadata> photoMetadataList = place.getPhotoMetadatas();
            if (photoMetadataList != null && !photoMetadataList.isEmpty()) {
                // Photo metadata is available
                PhotoMetadata photoMetadata = photoMetadataList.get(0); // Retrieve the first photo metadata
                String photoReference = photoMetadata.zzb(); // Retrieve photo reference
                String imageUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + itemView.getContext().getString(R.string.places_api_key);
                Glide.with(itemView)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image) // Placeholder image resource
                        .error(R.drawable.error_image) // Error image resource
                        .into(imageView);
                System.out.println("Image URL: " + imageUrl);
            } else {
                // No photo metadata available, load placeholder image
                Glide.with(itemView)
                        .load(R.drawable.no_image_placeholder) // Placeholder image for no photo
                        .into(imageView);
            }

            // Set name and distance
            nameTextView.setText(place.getName() + " - " + String.format("%.2f Kilometrai", distance/1000));

        }


    }

}
