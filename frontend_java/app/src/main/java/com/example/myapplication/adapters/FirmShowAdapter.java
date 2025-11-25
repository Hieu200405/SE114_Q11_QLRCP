package com.example.myapplication.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activities.UserDetailFirm;
import com.example.myapplication.models.FirmShow;

import java.util.List;

public class FirmShowAdapter extends RecyclerView.Adapter<FirmShowAdapter.FirmShowViewHolder> {
    private List <FirmShow> firmShowList;
    private OnItemClickListener listener;

    public FirmShowAdapter(List<FirmShow> firmShowList) {
        this.firmShowList = firmShowList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public FirmShowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_firm_show, parent, false);
        return new FirmShowViewHolder(view);
    }


    public interface OnItemClickListener {
        void onItemClick(FirmShow firmShow, int position);
    }

    @Override
    public void onBindViewHolder(@NonNull FirmShowViewHolder holder, int position) {
        FirmShow firmShow = firmShowList.get(position);
        if( firmShow == null){
            return;
        }
        Glide.with(holder.itemView.getContext())
                .load(firmShow.getThumbnailPath())
                .error(R.drawable.default_img)
                .into(holder.thumbnailImageView);
        holder.nameFirmTextView.setText("Name: "+ firmShow.getName());
        holder.startedFirmTextView.setText("Started on: " +firmShow.getStartDate());



        // ✅ Handle click to go to detail activity
        holder.itemView.setOnClickListener(v -> {
//            Intent intent = new Intent(v.getContext(), UserDetailFirm.class); // Change to your actual Detail Activity
//            intent.putExtra("firm_id", firmShow.getId());
//            v.getContext().startActivity(intent);

            if (listener != null) {
                listener.onItemClick(firmShow, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (firmShowList != null) {
            return firmShowList.size();
        }
        return 0;
    }

    public static class FirmShowViewHolder extends RecyclerView.ViewHolder {

        private ImageView thumbnailImageView;
        private TextView nameFirmTextView;
        private TextView startedFirmTextView;

        public FirmShowViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.imageThumbnail);
            nameFirmTextView = itemView.findViewById(R.id.textName);
            startedFirmTextView = itemView.findViewById(R.id.textStarted);

        }
    }
}
