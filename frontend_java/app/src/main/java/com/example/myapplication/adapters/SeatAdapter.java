package com.example.myapplication.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.Seat;

import java.util.List;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatViewHolder> {

    private final List<Seat> seatList;

    Seat selectedSeat;

    public SeatAdapter( List<Seat> seatList, Seat selectedSeat) {
        this.seatList = seatList;
        this.selectedSeat = selectedSeat;
    }


    public Seat getSelectedSeat() {
        return selectedSeat;
    }

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seat_user, parent, false);
        return new SeatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        Seat seat = seatList.get(position);

        holder.seatName.setText(seat.getName());

        // Set background theo trạng thái
        if (seat.isBought()) {
            holder.imageSeat.setBackgroundResource(R.drawable.bg_seat_bought);
        } else if (seat.equals(selectedSeat)) {
            holder.imageSeat.setBackgroundResource(R.drawable.bg_seat_selected);
        } else {
            holder.imageSeat.setBackgroundResource(R.drawable.bg_seat_default);
        }

        // Xử lý chọn ghế
        holder.itemView.setOnClickListener(v -> {
            if (!seat.isBought()) {
                if (seat.equals(selectedSeat)) {
                    // Click lại ghế đã chọn -> bỏ chọn
                    selectedSeat = null;
                    notifyItemChanged(position);
                } else {
                    int prevSelectedPos = seatList.indexOf(selectedSeat);
                    selectedSeat = seat;
                    notifyItemChanged(position); // Ghế mới được chọn
                    if (prevSelectedPos != -1) {
                        notifyItemChanged(prevSelectedPos); // Ghế cũ được hủy chọn
                    }
                }
            }
        });

    }




    @Override
    public int getItemCount() {
        return seatList.size();
    }

    static class SeatViewHolder extends RecyclerView.ViewHolder {
        ImageView imageSeat;
        TextView seatName;
        SeatViewHolder(View itemView) {
            super(itemView);
            imageSeat = itemView.findViewById(R.id.imageSeat);
            seatName = itemView.findViewById(R.id.textSeatName);
        }
    }
}
