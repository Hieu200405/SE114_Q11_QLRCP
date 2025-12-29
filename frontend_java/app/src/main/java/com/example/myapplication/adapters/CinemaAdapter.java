package com.example.myapplication.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.Cinema;

import java.util.List;

/**
 * Adapter for displaying list of cinemas with distance information
 */
public class CinemaAdapter extends RecyclerView.Adapter<CinemaAdapter.CinemaViewHolder> {

    private List<Cinema> cinemaList;
    private OnCinemaClickListener listener;
    private boolean showAdminControls = false;

    public interface OnCinemaClickListener {
        void onCinemaClick(Cinema cinema);
        void onEditClick(Cinema cinema);
        void onDeleteClick(Cinema cinema);
        void onNavigateClick(Cinema cinema);
    }

    public CinemaAdapter(List<Cinema> cinemaList) {
        this.cinemaList = cinemaList;
    }

    public CinemaAdapter(List<Cinema> cinemaList, boolean showAdminControls) {
        this.cinemaList = cinemaList;
        this.showAdminControls = showAdminControls;
    }

    public void setOnCinemaClickListener(OnCinemaClickListener listener) {
        this.listener = listener;
    }

    public void setShowAdminControls(boolean show) {
        this.showAdminControls = show;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<Cinema> newList) {
        this.cinemaList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CinemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cinema, parent, false);
        return new CinemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CinemaViewHolder holder, int position) {
        Cinema cinema = cinemaList.get(position);
        holder.bind(cinema, listener, showAdminControls);
    }

    @Override
    public int getItemCount() {
        return cinemaList != null ? cinemaList.size() : 0;
    }

    public static class CinemaViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCinemaImage;
        private TextView tvCinemaName;
        private TextView tvCinemaAddress;
        private TextView tvDistance;
        private TextView tvDuration;
        private TextView tvPhone;
        private ImageView btnNavigate;
        private ImageView btnEdit;
        private ImageView btnDelete;
        private View layoutAdminControls;

        public CinemaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCinemaImage = itemView.findViewById(R.id.ivCinemaImage);
            tvCinemaName = itemView.findViewById(R.id.tvCinemaName);
            tvCinemaAddress = itemView.findViewById(R.id.tvCinemaAddress);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            btnNavigate = itemView.findViewById(R.id.btnNavigate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            layoutAdminControls = itemView.findViewById(R.id.layoutAdminControls);
        }

        @SuppressLint("SetTextI18n")
        public void bind(Cinema cinema, OnCinemaClickListener listener, boolean showAdminControls) {
            // Set cinema name
            tvCinemaName.setText(cinema.getName());

            // Set address
            tvCinemaAddress.setText(cinema.getAddress());

            // Set phone if available
            if (tvPhone != null) {
                if (cinema.getPhone() != null && !cinema.getPhone().isEmpty()) {
                    tvPhone.setText(cinema.getPhone());
                    tvPhone.setVisibility(View.VISIBLE);
                } else {
                    tvPhone.setVisibility(View.GONE);
                }
            }

            // Set distance info if available
            if (tvDistance != null) {
                String distanceText = cinema.getDistanceText();
                if (distanceText != null && !distanceText.isEmpty()) {
                    tvDistance.setText(distanceText);
                    tvDistance.setVisibility(View.VISIBLE);
                } else {
                    tvDistance.setVisibility(View.GONE);
                }
            }

            // Set duration info if available
            if (tvDuration != null) {
                String durationText = cinema.getDurationText();
                if (durationText != null && !durationText.isEmpty()) {
                    tvDuration.setText("~" + durationText);
                    tvDuration.setVisibility(View.VISIBLE);
                } else {
                    tvDuration.setVisibility(View.GONE);
                }
            }

            // Load cinema image
            if (ivCinemaImage != null && cinema.getImageUrl() != null && !cinema.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(cinema.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .centerCrop()
                        .into(ivCinemaImage);
            }

            // Admin controls visibility
            if (layoutAdminControls != null) {
                layoutAdminControls.setVisibility(showAdminControls ? View.VISIBLE : View.GONE);
            }

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onCinemaClick(cinema);
            });

            if (btnNavigate != null) {
                btnNavigate.setOnClickListener(v -> {
                    if (listener != null) listener.onNavigateClick(cinema);
                });
            }

            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> {
                    if (listener != null) listener.onEditClick(cinema);
                });
            }

            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> {
                    if (listener != null) listener.onDeleteClick(cinema);
                });
            }
        }
    }
}

