package com.cykka.partner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class ReviewHolder extends RecyclerView.ViewHolder {

        public TextView name, reviewText, timestamp;
        public RatingBar reviewRating;

        public ReviewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.tv_review_name);
            reviewText = view.findViewById(R.id.tv_review_text);
            timestamp = view.findViewById(R.id.tv_time);
        }

    public void bind(Review review) {
       name.setText(review.getName());
       reviewText.setText(review.getReviewText());
       timestamp.setText(review.getTimestamp());

    }
}