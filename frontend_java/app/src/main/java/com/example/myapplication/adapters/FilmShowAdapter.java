package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.FilmShow;

import java.util.List;

public class FilmShowAdapter extends RecyclerView.Adapter<FilmShowAdapter.FilmShowViewHolder> {
    private List <FilmShow> filmShowList;
    private OnItemClickListener listener;

    public FilmShowAdapter(List<FilmShow> filmShowList) {
        this.filmShowList = filmShowList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public FilmShowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_film_show, parent, false);
        return new FilmShowViewHolder(view);
    }


    public interface OnItemClickListener {
        void onItemClick(FilmShow filmShow, int position);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmShowViewHolder holder, int position) {
        FilmShow filmShow = filmShowList.get(position);
        if( filmShow == null){
            return;
        }
        Glide.with(holder.itemView.getContext())
                .load(filmShow.getThumbnailPath())
                .error(R.drawable.default_img)
                .into(holder.thumbnailImageView);
        holder.nameFilmTextView.setText("Name: "+ filmShow.getName());
        holder.startedFilmTextView.setText("Started on: " + filmShow.getStartDate());



        // ✅ Handle click to go to detail activity
        holder.itemView.setOnClickListener(v -> {
//            Intent intent = new Intent(v.getContext(), UserDetailFilm.class); // Change to your actual Detail Activity
//            intent.putExtra("film_id", filmShow.getId());
//            v.getContext().startActivity(intent);

            if (listener != null) {
                listener.onItemClick(filmShow, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (filmShowList != null) {
            return filmShowList.size();
        }
        return 0;
    }

    public static class FilmShowViewHolder extends RecyclerView.ViewHolder {

        private ImageView thumbnailImageView;
        private TextView nameFilmTextView;
        private TextView startedFilmTextView;

        public FilmShowViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.imageThumbnail);
            nameFilmTextView = itemView.findViewById(R.id.textName);
            startedFilmTextView = itemView.findViewById(R.id.textStarted);

        }
    }
}
