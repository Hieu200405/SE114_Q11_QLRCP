package com.example.myapplication.adapters;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.activities.UserShowSeatsActivity;
import com.example.myapplication.models.BroadcastFirm;

import java.util.List;

public class BroadCastFirmAdapter extends RecyclerView.Adapter<BroadCastFirmAdapter.BroadcastFirmHolder> {

    private List<BroadcastFirm> broadcastFirmList;
    private String role = "user"; // Default role

    public BroadCastFirmAdapter(List<BroadcastFirm> broadcastFirmList) {
        this.broadcastFirmList = broadcastFirmList;
    }

    public BroadCastFirmAdapter(List<BroadcastFirm> broadcastFirmList, String role) {
        this.broadcastFirmList = broadcastFirmList;
        this.role = role;
    }

    public interface OnItemClickListener {
        void onItemClick(BroadcastFirm broadcastFirm);
        void onDeleteClick(BroadcastFirm broadcastFirm);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public BroadcastFirmHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_broadcast_firm, parent, false);
        return new BroadcastFirmHolder(view);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull BroadcastFirmHolder holder, int position) {
        BroadcastFirm broadcast = broadcastFirmList.get(position);
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
        return broadcastFirmList.size();
    }

    public static class BroadcastFirmHolder extends RecyclerView.ViewHolder {
        TextView textTime, textDate, textRoomSeats, textPrice;
        ImageButton buttonDelete;

        public BroadcastFirmHolder(@NonNull View itemView) {
            super(itemView);
            textTime = itemView.findViewById(R.id.textTime);
            textDate = itemView.findViewById(R.id.textDate);
            textRoomSeats = itemView.findViewById(R.id.textRoomSeats);
            textPrice = itemView.findViewById(R.id.textPrice);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }


    }


}
