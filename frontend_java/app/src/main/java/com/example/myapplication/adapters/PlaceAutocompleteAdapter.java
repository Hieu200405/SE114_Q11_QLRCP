package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.PlaceAutocomplete;

import java.util.List;

/**
 * Adapter for displaying place autocomplete suggestions
 */
public class PlaceAutocompleteAdapter extends RecyclerView.Adapter<PlaceAutocompleteAdapter.PlaceViewHolder> {

    private List<PlaceAutocomplete.Prediction> predictions;
    private OnPlaceClickListener listener;

    public interface OnPlaceClickListener {
        void onPlaceClick(PlaceAutocomplete.Prediction prediction);
    }

    public PlaceAutocompleteAdapter(List<PlaceAutocomplete.Prediction> predictions) {
        this.predictions = predictions;
    }

    public void setOnPlaceClickListener(OnPlaceClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<PlaceAutocomplete.Prediction> newPredictions) {
        this.predictions = newPredictions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place_autocomplete, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        PlaceAutocomplete.Prediction prediction = predictions.get(position);
        holder.bind(prediction, listener);
    }

    @Override
    public int getItemCount() {
        return predictions != null ? predictions.size() : 0;
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMainText;
        private TextView tvSecondaryText;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMainText = itemView.findViewById(R.id.tvMainText);
            tvSecondaryText = itemView.findViewById(R.id.tvSecondaryText);
        }

        public void bind(PlaceAutocomplete.Prediction prediction, OnPlaceClickListener listener) {
            tvMainText.setText(prediction.getMainText());
            tvSecondaryText.setText(prediction.getSecondaryText());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlaceClick(prediction);
                }
            });
        }
    }
}

