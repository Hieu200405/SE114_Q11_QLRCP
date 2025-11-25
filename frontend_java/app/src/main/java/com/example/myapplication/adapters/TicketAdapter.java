package com.example.myapplication.adapters;



import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.activities.UserActivityHistoryBookingTicket;
import com.example.myapplication.activities.UserAdminShowDetailTicket;
import com.example.myapplication.activities.UserShowSeatsActivity;
import com.example.myapplication.models.BookingTicketRequest;
import com.example.myapplication.models.BookingTicketResponse;
import com.example.myapplication.models.Ticket;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiTicketService;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    String accessToken;

    List<Ticket> ticketList;
    private OnItemClickListener listener;

    public TicketAdapter(List<Ticket> ticketList, String accessToken) {
        this.ticketList = ticketList;
        this.accessToken = accessToken;
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Ticket ticket);
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);
        if (ticket != null) {
            holder.tvBroadcastID.setText("Mã lịch chiếu: "+  String.valueOf(ticket.getBroadcastId()));
            holder.tvSeatName.setText("Tên chỗ ngồi: " + ticket.getSeatName());
            holder.tvPrice.setText("Giá: " + String.valueOf(ticket.getPrice()));
            holder.tvDateOrder.setText("Ngày đặt: "+ ticket.getDateOrder());
        } else {
            // Handle the case where ticket is null
            holder.tvBroadcastID.setText("N/A");
            holder.tvSeatName.setText("N/A");
            holder.tvPrice.setText("N/A");
            holder.tvDateOrder.setText("N/A");
        }

        // You can set click listeners or other interactions here if needed
        holder.itemView.setOnClickListener(v -> {
//           loadDetalTicketByApi(v, accessToken, ticket.getId());
//            // Toast.makeText(v.getContext(), "Clicked on ticket: " + ticket.getId(), Toast.LENGTH_SHORT).show();
            if (listener != null) {
                Toast.makeText(v.getContext(), "Clicked on ticket: " + ticket.getId(), Toast.LENGTH_SHORT).show();
                listener.onItemClick(ticket);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (ticketList != null) {
            return ticketList.size();
        } else {
            return 0; // Return 0 if the list is null to avoid NullPointerException
        }
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvBroadcastID;
        TextView tvSeatName;
        TextView tvPrice;
        TextView tvDateOrder;
        TicketViewHolder(View itemView) {
            super(itemView);
            tvBroadcastID = itemView.findViewById(R.id.tvBroadcastID);
            tvSeatName = itemView.findViewById(R.id.tvSeatName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDateOrder = itemView.findViewById(R.id.tvDateOrder);

        }
    }



    private void loadDetalTicketByApi(View v, String accessToken, int ticketId) {

        String token = "Bearer " + accessToken;

        ApiTicketService apiTicketService = ApiClient.getRetrofit().create(ApiTicketService.class);
        Call<BookingTicketResponse> call = apiTicketService.getTicketDetail(token, String.valueOf(ticketId));

        call.enqueue(new Callback<BookingTicketResponse>() {
            @Override
            public void onResponse(Call<BookingTicketResponse> call, Response<BookingTicketResponse> response) {
                if (response.isSuccessful()) {
                    BookingTicketResponse bookingResponse = response.body();
                    Intent intent = new Intent(v.getContext(), UserAdminShowDetailTicket.class);
                    intent.putExtra("bookingTicketResponse", bookingResponse);
                    startActivity(v.getContext(), intent, null);

                } else {
                    Log.e("API_ERROR", "Failed to load ticket details: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BookingTicketResponse> call, Throwable t) {
                Log.e("API_ERROR", "Failed to load ticket details: " + t.getMessage());
            }
        });
    }
}




