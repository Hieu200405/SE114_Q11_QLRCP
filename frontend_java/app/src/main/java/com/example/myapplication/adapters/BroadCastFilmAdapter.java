package com.example.myapplication.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.BroadcastFilm;

import java.util.List;
import java.util.Locale;

public class BroadCastFilmAdapter extends RecyclerView.Adapter<BroadCastFilmAdapter.BroadcastFilmHolder> {
    private static final String TAG = "BroadCastFilmAdapter";

    private List<BroadcastFilm> broadcastFilmList;
    private String role = "user"; // Default role

    // User location for distance calculation
    private double userLat = 0;
    private double userLng = 0;

    public BroadCastFilmAdapter(List<BroadcastFilm> broadcastFilmList) {
        this.broadcastFilmList = broadcastFilmList;
    }

    public BroadCastFilmAdapter(List<BroadcastFilm> broadcastFilmList, String role) {
        this.broadcastFilmList = broadcastFilmList;
        this.role = role;
    }

    /**
     * Set user location for distance calculation
     */
    public void setUserLocation(double lat, double lng) {
        this.userLat = lat;
        this.userLng = lng;
        Log.d(TAG, "User location set: " + lat + ", " + lng);
    }

    /**
     * Calculate distance between two points in km
     */
    private String calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        if (lat1 == 0 || lng1 == 0 || lat2 == 0 || lng2 == 0) {
            return null;
        }

        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        float distanceInMeters = results[0];
        float distanceInKm = distanceInMeters / 1000;

        if (distanceInKm < 1) {
            return String.format(Locale.getDefault(), "%.0f m", distanceInMeters);
        } else {
            return String.format(Locale.getDefault(), "%.1f km", distanceInKm);
        }
    }

    /**
     * Estimate travel duration based on distance (assuming 30 km/h average speed in city)
     */
    private String estimateDuration(double lat1, double lng1, double lat2, double lng2) {
        if (lat1 == 0 || lng1 == 0 || lat2 == 0 || lng2 == 0) {
            return null;
        }

        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        float distanceInKm = results[0] / 1000;

        // Assume average speed of 30 km/h in city traffic
        int durationMinutes = (int) Math.ceil(distanceInKm / 30 * 60);

        if (durationMinutes < 1) {
            return "1 phút";
        } else if (durationMinutes < 60) {
            return durationMinutes + " phút";
        } else {
            int hours = durationMinutes / 60;
            int mins = durationMinutes % 60;
            if (mins == 0) {
                return hours + " giờ";
            }
            return hours + " giờ " + mins + " phút";
        }
    }

    public interface OnItemClickListener {
        void onItemClick(BroadcastFilm broadcastFilm);
        void onDeleteClick(BroadcastFilm broadcastFilm);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public BroadcastFilmHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_broadcast_film, parent, false);
        return new BroadcastFilmHolder(view);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull BroadcastFilmHolder holder, int position) {
        BroadcastFilm broadcast = broadcastFilmList.get(position);
        holder.textTime.setText(broadcast.getTimeBroadcast().substring(0, 5)); // Giờ:Phút
        holder.textDate.setText(broadcast.getDateBroadcast());
        holder.textRoomSeats.setText("Phòng " + broadcast.getRoomID() + " • " + broadcast.getSeats() + " ghế");
        holder.textPrice.setText(String.format("%,.0f đ", broadcast.getPrice()));

        // Hiển thị thông tin Cinema nếu có
        if (holder.textCinemaInfo != null) {
            String cinemaName = broadcast.getCinemaName();
            Double cinemaLat = broadcast.getCinemaLatitude();
            Double cinemaLng = broadcast.getCinemaLongitude();

            // Get distance/duration from enrichment or calculate
            String distanceText = broadcast.getDistanceText();
            String durationText = broadcast.getDurationText();

            // If no enriched distance/duration but we have user location and cinema location, calculate
            if ((distanceText == null || distanceText.isEmpty()) &&
                userLat != 0 && userLng != 0 &&
                cinemaLat != null && cinemaLng != null && cinemaLat != 0 && cinemaLng != 0) {
                distanceText = calculateDistance(userLat, userLng, cinemaLat, cinemaLng);
                durationText = estimateDuration(userLat, userLng, cinemaLat, cinemaLng);
            }

            if (cinemaName != null && !cinemaName.isEmpty()) {
                // Has cinema info - show full details
                // Reset color to green (in case it was orange from previous binding)
                holder.textCinemaInfo.setTextColor(0xFF4CAF50); // Green

                StringBuilder cinemaInfo = new StringBuilder("🎬 " + cinemaName);
                if (distanceText != null && !distanceText.isEmpty()) {
                    cinemaInfo.append(" • ").append(distanceText);
                }
                if (durationText != null && !durationText.isEmpty()) {
                    cinemaInfo.append(" (~").append(durationText).append(")");
                    cinemaInfo.append(" (~").append(durationText).append(")");
                }
                holder.textCinemaInfo.setText(cinemaInfo.toString());
                holder.textCinemaInfo.setVisibility(View.VISIBLE);
                Log.d(TAG, "Broadcast " + broadcast.getID() + " → Cinema: " + cinemaName);
            } else {
                // No cinema info - show friendly message
                holder.textCinemaInfo.setText("⚠️ Phòng chưa gán rạp chiếu");
                holder.textCinemaInfo.setVisibility(View.VISIBLE);
                holder.textCinemaInfo.setTextColor(0xFFFF9800); // Orange color
                Log.w(TAG, "Broadcast " + broadcast.getID() + " (Room " + broadcast.getRoomID() +
                      ") → No cinema assigned");
            }
        }

        // Setup navigate button
        if (holder.btnNavigate != null) {
            Double cinemaLat = broadcast.getCinemaLatitude();
            Double cinemaLng = broadcast.getCinemaLongitude();

            if (cinemaLat != null && cinemaLng != null && cinemaLat != 0 && cinemaLng != 0) {
                holder.btnNavigate.setVisibility(View.VISIBLE);
                holder.btnNavigate.setOnClickListener(v -> {
                    // Open Google Maps navigation
                    String uri = String.format(Locale.US, "google.navigation:q=%f,%f", cinemaLat, cinemaLng);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setPackage("com.google.android.apps.maps");

                    if (intent.resolveActivity(v.getContext().getPackageManager()) != null) {
                        v.getContext().startActivity(intent);
                    } else {
                        // Fallback to web browser
                        String webUri = String.format(Locale.US, "https://www.google.com/maps/dir/?api=1&destination=%f,%f", cinemaLat, cinemaLng);
                        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUri));
                        v.getContext().startActivity(webIntent);
                    }
                });
            } else {
                holder.btnNavigate.setVisibility(View.GONE);
            }
        }


        if(role != null && role.equals("admin")) {
            holder.buttonDelete.setVisibility(View.VISIBLE);
        }
        else {
            holder.buttonDelete.setVisibility(View.GONE);
        }


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(broadcast);
            }

        });
        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(broadcast);
            }
        });

    }

    @Override
    public int getItemCount() {
        return broadcastFilmList.size();
    }

    public static class BroadcastFilmHolder extends RecyclerView.ViewHolder {
        TextView textTime, textDate, textRoomSeats, textPrice, textCinemaInfo;
        ImageButton buttonDelete;
        ImageView btnNavigate;

        public BroadcastFilmHolder(@NonNull View itemView) {
            super(itemView);
            textTime = itemView.findViewById(R.id.textTime);
            textDate = itemView.findViewById(R.id.textDate);
            textRoomSeats = itemView.findViewById(R.id.textRoomSeats);
            textPrice = itemView.findViewById(R.id.textPrice);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            textCinemaInfo = itemView.findViewById(R.id.textCinemaInfo);
            btnNavigate = itemView.findViewById(R.id.btnNavigate);
        }
    }
}
