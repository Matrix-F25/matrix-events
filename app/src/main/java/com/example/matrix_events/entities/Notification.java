package com.example.matrix_events.entities;

import com.example.matrix_events.database.DBObject;

import java.io.Serializable;
import com.google.firebase.Timestamp;

public class Notification extends DBObject implements Serializable {

    // there are two different types of notifications
    // one is the automated message (ex: getting selected from the waitlist)
    // the second is one from the actual event organizer. this contains an actual message
    private String type;
    private String title;
    private String textBody;
    private Timestamp timestamp;
    private String deviceId;

    public Notification() {}
    public Notification(String type, String title, String textBody, Timestamp timestamp, String deviceId) {
        this.type = type;
        this.title = title;
        this.textBody = textBody;
        this.timestamp = timestamp;
        this.deviceId = deviceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTextBody() {
        return textBody;
    }

    public void setTextBody(String textBody) {
        this.textBody = textBody;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
