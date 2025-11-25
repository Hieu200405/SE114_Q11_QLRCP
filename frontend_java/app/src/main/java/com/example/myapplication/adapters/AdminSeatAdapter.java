package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.FirmShow;
import com.example.myapplication.models.Seat;

import java.util.List;

public class AdminSeatAdapter extends RecyclerView.Adapter<AdminSeatAdapter.AdminSeatViewHolder> {
    private final List<Seat> seatList;

    Seat selectedSeat;
    private OnItemClickListener listener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public AdminSeatAdapter( List<Seat> seatList, Seat selectedSeat) {
        this.seatList = seatList;
        this.selectedSeat = selectedSeat;
    }


    public interface OnItemClickListener {
        void onItemClick(Seat seat);
    }



    @NonNull
    @Override
    public AdminSeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seat_user, parent, false);
        return new AdminSeatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminSeatViewHolder holder, int position) {
        Seat seat = seatList.get(position);

        holder.seatName.setText(seat.getName());

        // Set background theo trạng thái
        if (seat.isBought()) {
            holder.imageSeat.setBackgroundResource(R.drawable.bg_seat_bought);
        } else {
            holder.imageSeat.setBackgroundResource(R.drawable.bg_seat_default);
        }

        // Xử lý xem chi tiết ghế đã đặt rồi, ghế chưa đặt k có sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if(seat.isBought()) {
                if(listener != null) {
                    listener.onItemClick(seat);
                }
            }
        });

    }




    @Override
    public int getItemCount() {
        return seatList.size();
    }

    static class AdminSeatViewHolder extends RecyclerView.ViewHolder {
        ImageView imageSeat;
        TextView seatName;
        AdminSeatViewHolder(View itemView) {
            super(itemView);
            imageSeat = itemView.findViewById(R.id.imageSeat);
            seatName = itemView.findViewById(R.id.textSeatName);
        }
    }
}
