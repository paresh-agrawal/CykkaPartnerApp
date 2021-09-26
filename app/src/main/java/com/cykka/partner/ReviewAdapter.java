package com.cykka.partner;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyViewHolder> {

    private List<Review> reviewList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, reviewText, timestamp, tvReviewer;
        public RatingBar reviewRating;
        public ImageView ivReviewer;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.tv_review_name);
            reviewText = view.findViewById(R.id.tv_review_text);
            timestamp = view.findViewById(R.id.tv_time);
            reviewRating = view.findViewById(R.id.review_rating);
            tvReviewer = view.findViewById(R.id.tv_reviewer);
            ivReviewer = view.findViewById(R.id.iv_reviewer);
        }
    }


    public ReviewAdapter(List<Review> reviewList, Context applicationContext) {
        this.reviewList = reviewList;
        this.context = applicationContext;
    }

    @Override
    public ReviewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_review_list_item, parent, false);

        return new ReviewAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ReviewAdapter.MyViewHolder holder, int position) {

        Review review = reviewList.get(position);
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        String name = review.getName();
        char firstChar = name.charAt(0);

        int[] androidColors = context.getResources().getIntArray(R.array.rainbow);
        int randomColor = androidColors[new Random().nextInt(androidColors.length)];

        //Log.d("Name", name);

        holder.name.setText(review.getName());
        holder.reviewText.setText(review.getReviewText());

        String timestamp = review.getTimestamp();
        SimpleDateFormat dt = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        Date date = null;
        try {
            date = dt.parse(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat dtNew = new SimpleDateFormat("dd MMM yyyy");
        //System.out.println(dt1.format(date));

        holder.timestamp.setText(dtNew.format(date));
        holder.reviewRating.setRating(Integer.parseInt(review.getRating()));

        holder.tvReviewer.setText(String.valueOf(review.getName().charAt(0)));

        GradientDrawable bgShape = (GradientDrawable) holder.ivReviewer.getBackground();
        bgShape.setColor(randomColor);


        //holder.ivReviewer.setCircleBackgroundColor(color);

    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }
}
