package com.example.matrix_events.entities;

import com.example.matrix_events.database.DBObject;

import java.io.Serializable;

public class Poster extends DBObject implements Serializable {
    private String imageUrl;
    private String eventId;
    private String fileName;

    public Poster() {} // Required for Firestore

    public Poster(String imageUrl, String eventId, String fileName) {
        this.imageUrl = imageUrl;
        this.eventId = eventId;
        this.fileName = fileName;
    }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}
