package com.example.matrix_events.entities;

import com.example.matrix_events.database.DBObject;

import java.io.Serializable;

public class Notification extends DBObject implements Serializable {
    private String name;
    private String description;

    public Notification() {}       // Required for Firestore
    public Notification(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
