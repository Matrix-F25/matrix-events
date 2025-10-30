package com.example.matrix_events.entities;

import com.example.matrix_events.database.DBObject;

public class Profile extends DBObject {
    private String name;
    private String email;
    private String phoneNumber; // Optional
    private String deviceId;
    private boolean notificationsEnabled = true; // Default is true

    public Profile() {}       // Required for Firestore
    public Profile(String name, String email, String phoneNumber, String deviceId) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getDeviceId() {
        return deviceId;
    }
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}