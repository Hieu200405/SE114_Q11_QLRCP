package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.UserInfo;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    List<UserInfo> userList;
    public UserAdapter(List<UserInfo> userList) {
        this.userList = userList;
    }

    private OnItemClickListener listener;


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onViewClick(UserInfo userInfo);
        void onEditClick(UserInfo userInfo);
        void onDeleteClick(UserInfo userInfo);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_user, parent, false);
        return new UserAdapter.UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserInfo user = userList.get(position);
        if (user != null) {
            holder.textUserId.setText("ID: "+ String.valueOf(user.getId()));
            holder.textName.setText(user.getName());
            holder.textUsername.setText(user.getUsername());


        }
        else {
            holder.textUserId.setText("N/A");
            holder.textName.setText("N/A");
            holder.textUsername.setText("N/A");
        }

        holder.btnView.setOnClickListener(v -> {
            // Handle view button click
            if (listener != null) {
                listener.onViewClick(user);
            }
        });
        holder.btnEdit.setOnClickListener(v -> {
            // Handle edit button click
            if (listener != null) {
                listener.onEditClick(user);
            }
        });
        holder.btnDelete.setOnClickListener(v -> {
            // Handle delete button click
            if (listener != null) {
                listener.onDeleteClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (userList != null) {
            return userList.size();
        } else {
            return 0;
        }
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView textUserId;
        TextView textName;
        TextView textUsername;
        ImageView btnView;
        ImageView btnEdit;
        ImageView btnDelete;
        public UserViewHolder(View itemView) {
            super(itemView);
            // Initialize your view components here
            textUserId = itemView.findViewById(R.id.textUserId);
            textName = itemView.findViewById(R.id.textName);
            textUsername = itemView.findViewById(R.id.textUsername);
            btnView = itemView.findViewById(R.id.btnView);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
