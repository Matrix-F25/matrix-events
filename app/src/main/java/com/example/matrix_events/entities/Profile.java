package com.example.matrix_events.entities;

import androidx.annotation.NonNull;

import com.example.matrix_events.database.DBObject;

import java.io.Serializable;

public class Profile extends DBObject implements Serializable {
    // Attribute Declarations
    private String name;
    private String email;
    private String phoneNumber; // optional, can be null
    private String deviceId;

    // Profile Notification Preferences
    // Notifications on by Default
    private boolean adminPushNotifications = true;
    private boolean organizerPushNotifications = true;
    private boolean isAdmin = false;
    private String profilePictureUrl;

    // Constructors
    public Profile() {} // Required for Firestore

    public Profile(@NonNull String name, @NonNull String email, String phoneNumber, @NonNull String deviceId) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.deviceId = deviceId;
    }

    // Getters and Setters

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

    public String getProfilePictureUrl() { return profilePictureUrl; }

    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public boolean isAdminPushNotifications() {
        return adminPushNotifications;
    }

    public boolean isOrganizerPushNotifications() {
        return organizerPushNotifications;
    }

    public void setAdminPushNotifications(boolean notificationsEnabled) {
        this.adminPushNotifications = notificationsEnabled;
    }

    public void setOrganizerPushNotifications(boolean notificationsEnabled) {
        this.organizerPushNotifications = notificationsEnabled;
    }

    public boolean isAdmin() { return isAdmin; }

    public void setAdmin(boolean admin) { isAdmin = admin; }
}