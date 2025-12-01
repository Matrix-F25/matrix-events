package com.example.matrix_events.entities;

import androidx.annotation.NonNull;

import com.example.matrix_events.database.DBObject;

import java.io.Serializable;

/**
 * Represents a user profile within the application.
 * <p>
 * This class stores user-specific information including contact details,
 * unique device identifiers, notification preferences, and administrative status.
 * It extends {@link DBObject} for Firestore integration and implements {@link Serializable}
 * for easy transfer between Android components.
 * </p>
 */
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

    /**
     * Default constructor required for Firestore data mapping.
     */
    public Profile() {}

    /**
     * Constructs a new Profile object with the specified details.
     *
     * @param name        The name of the user. Cannot be null.
     * @param email       The email address of the user. Cannot be null.
     * @param phoneNumber The phone number of the user. This field is optional and can be null.
     * @param deviceId    The unique device ID associated with this profile. Cannot be null.
     */
    public Profile(@NonNull String name, @NonNull String email, String phoneNumber, @NonNull String deviceId) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.deviceId = deviceId;
    }

    // Getters and Setters

    /**
     * Gets the name of the user.
     *
     * @return The user's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the user.
     *
     * @param name The new name to set. Cannot be null.
     */
    public void setName(@NonNull String name) {
        this.name = name;
    }

    /**
     * Gets the email address of the user.
     *
     * @return The user's email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the user.
     *
     * @param email The new email address to set. Cannot be null.
     */
    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    /**
     * Gets the phone number of the user.
     *
     * @return The user's phone number, or {@code null} if not set.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the phone number of the user.
     *
     * @param phoneNumber The new phone number to set. Can be null.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gets the unique device ID associated with the profile.
     *
     * @return The unique device identifier.
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Gets the URL of the user's profile picture.
     *
     * @return A string representing the URL of the profile picture, or {@code null} if not set.
     */
    public String getProfilePictureUrl() { return profilePictureUrl; }

    /**
     * Sets the URL of the user's profile picture.
     *
     * @param profilePictureUrl The URL string to set.
     */
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    /**
     * Checks if the user has enabled push notifications from administrators.
     *
     * @return {@code true} if admin notifications are enabled, {@code false} otherwise.
     */
    public boolean isAdminPushNotifications() {
        return adminPushNotifications;
    }

    /**
     * Checks if the user has enabled push notifications from organizers.
     *
     * @return {@code true} if organizer notifications are enabled, {@code false} otherwise.
     */
    public boolean isOrganizerPushNotifications() {
        return organizerPushNotifications;
    }

    /**
     * Sets the preference for receiving push notifications from administrators.
     *
     * @param notificationsEnabled {@code true} to enable notifications, {@code false} to disable.
     */
    public void setAdminPushNotifications(boolean notificationsEnabled) {
        this.adminPushNotifications = notificationsEnabled;
    }

    /**
     * Sets the preference for receiving push notifications from organizers.
     *
     * @param notificationsEnabled {@code true} to enable notifications, {@code false} to disable.
     */
    public void setOrganizerPushNotifications(boolean notificationsEnabled) {
        this.organizerPushNotifications = notificationsEnabled;
    }

    /**
     * Checks if the user has administrative privileges.
     *
     * @return {@code true} if the user is an admin, {@code false} otherwise.
     */
    public boolean isAdmin() { return isAdmin; }

    /**
     * Sets the administrative status of the user.
     *
     * @param admin {@code true} to grant admin privileges, {@code false} to revoke.
     */
    public void setAdmin(boolean admin) { isAdmin = admin; }
}