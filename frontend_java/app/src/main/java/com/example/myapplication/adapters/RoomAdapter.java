package com.example.myapplication.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.RoomRequest;
import com.example.myapplication.models.RoomResponse;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<RoomResponse> roomList;
    private static OnRoomClickListener listener;

    public void setOnRoomClickListener(OnRoomClickListener listener) {
        this.listener = listener;
    }

    public RoomAdapter( List<RoomResponse> roomList) {
        this.roomList = roomList;
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
        private ImageView buttonEdit;
        private ImageView buttonDelete;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvSeats = itemView.findViewById(R.id.tvSeats);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        @SuppressLint("SetTextI18n")
        public void bind(RoomResponse room) {
            tvRoomName.setText("Tên phòng: " + room.getName());
            tvSeats.setText("Số chỗ ngồi: " + room.getSeats());
            buttonEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(room);
            });

            buttonDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(room);
            });
        }
    }
}
