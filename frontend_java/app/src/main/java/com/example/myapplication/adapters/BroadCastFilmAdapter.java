package com.example.myapplication.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.BroadcastFilm;

import java.util.List;

public class BroadCastFilmAdapter extends RecyclerView.Adapter<BroadCastFilmAdapter.BroadcastFilmHolder> {

    private List<BroadcastFilm> broadcastFilmList;
    private String role = "user"; // Default role

    public BroadCastFilmAdapter(List<BroadcastFilm> broadcastFilmList) {
        this.broadcastFilmList = broadcastFilmList;
    }

    public BroadCastFilmAdapter(List<BroadcastFilm> broadcastFilmList, String role) {
        this.broadcastFilmList = broadcastFilmList;
        this.role = role;
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


        if(role != null && role.equals("admin")) {
            holder.buttonDelete.setVisibility(View.VISIBLE);
        }
        else {
            holder.buttonDelete.setVisibility(View.GONE);
        }


        holder.itemView.setOnClickListener(v -> {
//            Intent intent = new Intent(v.getContext(), UserShowSeatsActivity.class);
//            intent.putExtra("broadcastId", broadcast.getID());
//            v.getContext().startActivity(intent);
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
        TextView textTime, textDate, textRoomSeats, textPrice;
        ImageButton buttonDelete;

        public BroadcastFilmHolder(@NonNull View itemView) {
            super(itemView);
            textTime = itemView.findViewById(R.id.textTime);
            textDate = itemView.findViewById(R.id.textDate);
            textRoomSeats = itemView.findViewById(R.id.textRoomSeats);
            textPrice = itemView.findViewById(R.id.textPrice);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }


    }


}
