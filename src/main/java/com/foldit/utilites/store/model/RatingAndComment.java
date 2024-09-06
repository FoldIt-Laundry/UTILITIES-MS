package com.foldit.utilites.store.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "StoreInformation")
public class RatingAndComment {
    private Double rating;
    private String comment;

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
