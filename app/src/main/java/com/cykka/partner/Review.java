package com.cykka.partner;

import android.widget.RatingBar;

public class Review {
    private String name, reviewText, timestamp;
    private String rating;

    public Review(){
    }

    public Review(String name, String reviewText, String timestamp, String rating){
        this.name = name;
        this.reviewText = reviewText;
        this.timestamp = timestamp;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
}
