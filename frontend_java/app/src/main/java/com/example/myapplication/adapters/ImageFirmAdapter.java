package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.ImageFirm;

import java.util.List;

public class ImageFirmAdapter extends RecyclerView.Adapter<ImageFirmAdapter.ImageFirmViewHolder> {

    private List<ImageFirm> imageFirmList;
    public ImageFirmAdapter(List<ImageFirm> imageFirms){
        this.imageFirmList = imageFirms;
    }


    @NonNull
    @Override
    public ImageFirmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageFirmAdapter.ImageFirmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageFirmViewHolder holder, int position) {
        ImageFirm imageFirm = imageFirmList.get(position);
        Glide.with(holder.itemView.getContext())
                .load(imageFirm.getImageUrl())
                .error(R.drawable.default_img) // Replace with your default image resource
                .into(((ImageFirmViewHolder) holder).imageView);
    }

    @Override
    public int getItemCount() {
        if (imageFirmList != null) {
            return imageFirmList.size();
        } else {
            return 0; // Return 0 if the list is null to avoid NullPointerException
        }
    }

    public static class  ImageFirmViewHolder extends RecyclerView.ViewHolder {
        // Define your ViewHolder here
        ImageView imageView;
        public ImageFirmViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView); // Replace with your actual ImageView ID
            // Initialize your views here
        }

    }

}
