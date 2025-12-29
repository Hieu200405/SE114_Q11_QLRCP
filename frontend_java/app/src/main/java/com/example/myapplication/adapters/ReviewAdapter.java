package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviewList;
    private boolean isAdmin=false;
    private OnReviewClickListener listener;

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    public interface OnReviewClickListener {
        void onDeleteClick(int rId);
    }

    public void setOnReviewClickListener(OnReviewClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.tvUserNameReview.setText(review.getUserName());
        holder.tvCommentReview.setText(review.getComment());
        holder.ratingBarReview.setRating(review.getRating());


        if (isAdmin) {
            holder.imageDeleteReview.setVisibility(View.VISIBLE);
            holder.imageDeleteReview.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(review.getId());
                }
            });
        } else {
            holder.imageDeleteReview.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserNameReview, tvCommentReview;
        RatingBar ratingBarReview;
        ImageView imageDeleteReview;
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserNameReview = itemView.findViewById(R.id.tvUserNameReview);
            tvCommentReview = itemView.findViewById(R.id.tvCommentReview);
            ratingBarReview = itemView.findViewById(R.id.ratingBarReview);
            imageDeleteReview = itemView.findViewById(R.id.imageDeleteReview);
        }
    }
}
