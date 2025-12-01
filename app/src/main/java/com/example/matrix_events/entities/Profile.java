package com.example.matrix_events.entities;

import androidx.annotation.NonNull;

import com.example.matrix_events.database.DBObject;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class Profile extends DBObject implements Serializable {
    // Attribute Declarations
    private String name;
    private String email;
    private String phoneNumber; // optional, can be null
    private String deviceId;
    private String profilePictureUrl;
    private String profilePictureFileName;
    private String FCMToken = "";

    // Profile Notification Preferences
    // Notifications on by Default
    private boolean inAppAdminNotifications = true;
    private boolean inAppOrganizerNotifications = true;
    private boolean pushAdminNotifications = true;
    private boolean pushOrganizerNotifications = true;
    private boolean notificationsEnabled = true;

    private boolean isAdmin = false;

    // Constructors
    public Profile() {} // Required for Firestore

    public Profile(@NonNull String name, @NonNull String email, String phoneNumber, @NonNull String deviceId) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.deviceId = deviceId;

        // Safe defaults for new fields
        this.profilePictureUrl = "";
        this.profilePictureFileName = "";
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
    public String getFCMToken() { return FCMToken; }
    public void setFCMToken(String FCMToken) { this.FCMToken = FCMToken; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
    public boolean isInAppAdminNotifications() {
        return inAppAdminNotifications;
    }
    public void setInAppAdminNotifications(boolean inAppAdminNotifications) {
        this.inAppAdminNotifications = inAppAdminNotifications;
    }
    public boolean isInAppOrganizerNotifications() {
        return inAppOrganizerNotifications;
    }
    public void setInAppOrganizerNotifications(boolean inAppOrganizerNotifications) {
        this.inAppOrganizerNotifications = inAppOrganizerNotifications;
    }
    public boolean isPushAdminNotifications() {
        return pushAdminNotifications;
    }
    public void setPushAdminNotifications(boolean pushAdminNotifications) {
        this.pushAdminNotifications = pushAdminNotifications;
    }
    public boolean isPushOrganizerNotifications() { return pushOrganizerNotifications; }
    public void setPushOrganizerNotifications(boolean pushOrganizerNotifications) {
        this.pushOrganizerNotifications = pushOrganizerNotifications;
    }
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = (profilePictureUrl == null) ? "" : profilePictureUrl;
    }
    public String getProfilePictureFileName() { return profilePictureFileName; }
    public void setProfilePictureFileName(String profilePictureFileName) {
        this.profilePictureFileName = profilePictureFileName;
    }
}