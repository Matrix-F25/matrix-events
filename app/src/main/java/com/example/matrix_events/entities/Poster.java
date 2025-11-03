package com.example.matrix_events.entities;

import java.io.Serializable;

public class Poster implements Serializable {
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
