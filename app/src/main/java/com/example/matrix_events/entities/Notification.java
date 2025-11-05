package com.example.matrix_events.entities;

import com.example.matrix_events.database.DBObject;

import java.io.Serializable;
import com.google.firebase.Timestamp;

public class Notification extends DBObject implements Serializable {
    private Profile sender;
    private Profile receiver;
    private String message;
    private Timestamp timestamp;

    public Notification() {}        // Required for Firestore
    public Notification(Profile sender, Profile receiver, String message, Timestamp timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.timestamp = timestamp;
    }

    public Profile getSender() {
        return sender;
    }

    public void setSender(Profile sender) {
        this.sender = sender;
    }

    public Profile getReceiver() {
        return receiver;
    }

    public void setReceiver(Profile receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
