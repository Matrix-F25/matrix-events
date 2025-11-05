package com.example.matrix_events.entities;

// Imports
import com.example.matrix_events.database.DBObject;

import java.io.Serializable;

// Profile Class
public class Profile extends DBObject implements Serializable {

    // Important Profile Attribute Declarations
    private String deviceId;
    private String name;
    private String email;
    private String phoneNumber; // Optional

    // Profile Notification Preferences Declarations
    // Notifications on by Default
    private boolean emailAdminNotifications = true;
    private boolean emailOrganizerNotifications = true;
    private boolean phoneAdminNotifications = true;
    private boolean phoneOrganizerNotifications = true;

    // Constructors
    public Profile() {
    }       // Required for Firestore

    public Profile(String name, String email, String deviceId) {
        this.name = name;
        this.email = email;
        this.deviceId = deviceId;
    }

    public Profile(String name, String email, String phoneNumber, String deviceId) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.deviceId = deviceId;
    }

    // Getters and Setters
    public String getDeviceId() {
        return deviceId;
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

    public boolean isEmailAdminNotifications() {return emailAdminNotifications;}
    public void setEmailAdminNotifications(boolean emailAdminNotifications) {this.emailAdminNotifications = emailAdminNotifications;}
    public boolean isEmailOrganizerNotifications() {return emailOrganizerNotifications;}
    public void setEmailOrganizerNotifications(boolean emailOrganizerNotifications) {this.emailOrganizerNotifications = emailOrganizerNotifications;}
    public boolean isPhoneAdminNotifications() {return phoneAdminNotifications;}
    public void setPhoneAdminNotifications(boolean phoneAdminNotifications) {this.phoneAdminNotifications = phoneAdminNotifications;}
    public boolean isPhoneOrganizerNotifications() {return phoneOrganizerNotifications;}
    public void setPhoneOrganizerNotifications(boolean phoneOrganizerNotifications) {this.phoneOrganizerNotifications = phoneOrganizerNotifications;}
}

