package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.models.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviewList; // Review model lấy từ dữ liệu serialize của Backend

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
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
        holder.tvUserName.setText(review.getUserName());
        holder.tvComment.setText(review.getComment());
        holder.ratingBar.setRating(review.getRating());
    }

    @Override
    public int getItemCount() { return reviewList.size(); }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvComment;
        RatingBar ratingBar;
        ReviewViewHolder(View v) {
            super(v);
            tvUserName = v.findViewById(R.id.tvUserNameReview);
            tvComment = v.findViewById(R.id.tvCommentReview);
            ratingBar = v.findViewById(R.id.ratingBarReview);
        }
    }
}
