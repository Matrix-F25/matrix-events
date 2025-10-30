package com.example.matrix_events.entities;

public class Poster {
    private String imageUrl;

    public Poster() {}      // Required for Firestore

    public Poster(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
