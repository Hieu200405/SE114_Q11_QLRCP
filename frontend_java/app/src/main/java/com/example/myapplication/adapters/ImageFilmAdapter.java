package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.ImageFilm;

import java.util.List;

public class ImageFilmAdapter extends RecyclerView.Adapter<ImageFilmAdapter.ImageFilmViewHolder> {

    private List<ImageFilm> imageFilmList;
    public ImageFilmAdapter(List<ImageFilm> imageFilms){
        this.imageFilmList = imageFilms;
    }


    @NonNull
    @Override
    public ImageFilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageFilmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageFilmViewHolder holder, int position) {
        ImageFilm imageFilm = imageFilmList.get(position);
        Glide.with(holder.itemView.getContext())
                .load(imageFilm.getImageUrl())
                .error(R.drawable.default_img) // Replace with your default image resource
                .into(((ImageFilmViewHolder) holder).imageView);
    }

    @Override
    public int getItemCount() {
        if (imageFilmList != null) {
            return imageFilmList.size();
        } else {
            return 0; // Return 0 if the list is null to avoid NullPointerException
        }
    }

    public static class ImageFilmViewHolder extends RecyclerView.ViewHolder {
        // Define your ViewHolder here
        ImageView imageView;
        public ImageFilmViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView); // Replace with your actual ImageView ID
            // Initialize your views here
        }

    }

}
