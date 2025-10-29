package com.example.matrix_events.database;

public abstract class DBObject {
    private String id;
    public DBObject() {}    // Required for Firestore
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}