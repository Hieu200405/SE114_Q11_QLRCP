package com.example.myapplication.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;

import java.util.ArrayList;

public class CoverImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_IMAGE = 0;
    private static final int TYPE_ADD_BUTTON = 1;

    private Context context;
    private ArrayList<Uri> imageUris;
    private OnAddImageClickListener addImageClickListener;
    private OnImageRemoveClickListener removeClickListener;

    public interface OnAddImageClickListener {
        void onAddImageClick();
    }

    public interface OnImageRemoveClickListener {
        void onImageRemoveClick(int position);
    }
    public CoverImageAdapter(Context context, ArrayList<Uri> imageUris, OnAddImageClickListener listener, OnImageRemoveClickListener removeListener) {
        this.context = context;
        this.imageUris = imageUris;
        this.addImageClickListener = listener;
        this.removeClickListener = removeListener;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == imageUris.size()) ? TYPE_ADD_BUTTON : TYPE_IMAGE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_IMAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_cover_image, parent, false);
            return new ImageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_cover_image, parent, false);
            return new AddButtonViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageViewHolder) {
            Uri uri = imageUris.get(position);
            Glide.with(context)
                    .load(uri)
                    .into(((ImageViewHolder) holder).imageCover);

            ((ImageViewHolder) holder).buttonAddImage.setVisibility(View.GONE);
            ((ImageViewHolder) holder).buttonRemoveImage.setVisibility(View.VISIBLE);
            ((ImageViewHolder) holder).buttonRemoveImage.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (removeClickListener != null && pos >= 0 && pos < imageUris.size()) {
                    removeClickListener.onImageRemoveClick(pos);
                }
            });
        } else {
            ((AddButtonViewHolder) holder).imageCover.setVisibility(View.GONE);
            ((AddButtonViewHolder) holder).buttonAddImage.setVisibility(View.VISIBLE);
            ((AddButtonViewHolder) holder).buttonAddImage.setOnClickListener(v -> addImageClickListener.onAddImageClick());

        }

    }

    @Override
    public int getItemCount() {
        return imageUris.size() + 1; // +1 cho nút add
    }

    public void addImage(Uri uri) {
        imageUris.add(uri);
        notifyItemInserted(imageUris.size() - 1);
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageCover;
        ImageButton buttonAddImage;
        ImageButton buttonRemoveImage;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageCover = itemView.findViewById(R.id.imageCover);
            buttonAddImage = itemView.findViewById(R.id.buttonAddImage);
            buttonRemoveImage = itemView.findViewById(R.id.buttonRemoveImage);
        }
    }

    static class AddButtonViewHolder extends RecyclerView.ViewHolder {
        ImageView imageCover;
        ImageButton buttonAddImage;

        public AddButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            imageCover = itemView.findViewById(R.id.imageCover);
            buttonAddImage = itemView.findViewById(R.id.buttonAddImage);
        }
    }
    public void removeImage(int position) {
        if (position >= 0 && position < imageUris.size()) {
            imageUris.remove(position);
            notifyItemRemoved(position);
        }
    }

}
