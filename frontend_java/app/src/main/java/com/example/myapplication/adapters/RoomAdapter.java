package com.example.myapplication.adapters;

import android.annotation.SuppressLint;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.cacheModels.CinemaCache;
import com.example.myapplication.models.Cinema;
import com.example.myapplication.models.RoomResponse;

import java.util.List;
import java.util.Locale;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<RoomResponse> roomList;
    private static OnRoomClickListener listener;

    // User location for distance calculation
    private static double userLat = 0;
    private static double userLng = 0;

    public void setOnRoomClickListener(OnRoomClickListener listener) {
        RoomAdapter.listener = listener;
    }

    public RoomAdapter(List<RoomResponse> roomList) {
        this.roomList = roomList;
    }

    /**
     * Set user location for distance calculation
     */
    public void setUserLocation(double lat, double lng) {
        userLat = lat;
        userLng = lng;
    }

    /**
     * Calculate distance between two points
     */
    private static String calculateDistance(double lat1, double lng1, double lat2, double lng2) {
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
     * Estimate travel duration based on distance
     */
    private static String estimateDuration(double lat1, double lng1, double lat2, double lng2) {
        if (lat1 == 0 || lng1 == 0 || lat2 == 0 || lng2 == 0) {
            return null;
        }

        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        float distanceInKm = results[0] / 1000;

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


    public interface OnRoomClickListener {
        void onEditClick(RoomResponse room);
        void onDeleteClick(RoomResponse room);
    }


    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        RoomResponse room = roomList.get(position);
        holder.bind(room);
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRoomName;
        private TextView tvSeats;
        private TextView tvCinemaInfo;
        private ImageView buttonEdit;
        private ImageView buttonDelete;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvSeats = itemView.findViewById(R.id.tvSeats);
            tvCinemaInfo = itemView.findViewById(R.id.tvCinemaInfo);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        @SuppressLint("SetTextI18n")
        public void bind(RoomResponse room) {
            tvRoomName.setText("Tên phòng: " + room.getName());
            tvSeats.setText("Số chỗ ngồi: " + room.getSeats());

            // Display cinema information with distance
            if (tvCinemaInfo != null) {
                if (room.getCinemaId() != null && room.getCinemaId() > 0) {
                    // Try to get cinema from cache
                    Cinema cinema = CinemaCache.getCinemaById(room.getCinemaId());
                    if (cinema != null) {
                        StringBuilder cinemaInfo = new StringBuilder("🎬 " + cinema.getName());

                        // Calculate distance if we have user location and cinema location
                        // Note: Cinema uses primitive double, so check for non-zero values
                        if (userLat != 0 && userLng != 0 &&
                            cinema.getLatitude() != 0 && cinema.getLongitude() != 0) {
                            String distance = calculateDistance(userLat, userLng,
                                    cinema.getLatitude(), cinema.getLongitude());
                            String duration = estimateDuration(userLat, userLng,
                                    cinema.getLatitude(), cinema.getLongitude());

                            if (distance != null) {
                                cinemaInfo.append(" • ").append(distance);
                            }
                            if (duration != null) {
                                cinemaInfo.append(" (~").append(duration).append(")");
                            }
                        }

                        tvCinemaInfo.setText(cinemaInfo.toString());
                        tvCinemaInfo.setVisibility(View.VISIBLE);
                    } else {
                        // Cinema not found in cache - show ID temporarily
                        tvCinemaInfo.setText("🎬 Rạp ID: " + room.getCinemaId());
                        tvCinemaInfo.setVisibility(View.VISIBLE);
                        android.util.Log.w("RoomAdapter", "Cinema not found in cache for ID: " + room.getCinemaId());
                    }
                } else {
                    // No cinema assigned
                    tvCinemaInfo.setVisibility(View.GONE);
                }
            }

            buttonEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(room);
            });

            buttonDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(room);
            });
        }
    }
}
