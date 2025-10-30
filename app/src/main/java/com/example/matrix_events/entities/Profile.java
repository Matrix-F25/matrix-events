package com.example.matrix_events.entities;

import com.example.matrix_events.database.DBObject;

public class Profile extends DBObject {
    private String name;
    private String description;

    public Profile() {}       // Required for Firestore
    public Profile(String name, String description) {
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