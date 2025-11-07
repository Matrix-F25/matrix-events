package com.example.matrix_events.entities;

import androidx.annotation.NonNull;

import com.example.matrix_events.database.DBObject;

import java.io.Serializable;

public class Profile extends DBObject implements Serializable {
    private String name;
    private String email;
    private String phoneNumber;         // optional, can be null
    private String deviceId;
    private boolean notificationsEnabled = true;

    public Profile() {}       // Required for Firestore
    public Profile(@NonNull String name, @NonNull String email, String phoneNumber, @NonNull String deviceId) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }
    public void setName(@NonNull String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(@NonNull String email) {
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