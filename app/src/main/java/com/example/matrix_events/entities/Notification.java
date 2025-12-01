package com.example.matrix_events.entities;

import androidx.annotation.NonNull;

import com.example.matrix_events.database.DBObject;

import java.io.IOException;
import java.io.Serializable;
import com.google.firebase.Timestamp;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

/**
 * Represents a notification to be sent from one user profile to another.
 * This class extends {@link DBObject} for Firestore compatibility and implements
 * {@link Serializable} to be passed between Android components.
 * <p>
 * It includes details about the sender, receiver, message content, and a timestamp.
 * Custom serialization logic is implemented to handle the Firebase {@link Timestamp},
 * which is not natively Serializable.
 * </p>
 */
public class Notification extends DBObject implements Serializable {

    public enum NotificationType {
        ADMIN,
        ORGANIZER
    }

    private Profile sender;
    private Profile receiver;
    private String message;
    private transient Timestamp timestamp;
    private NotificationType type; // Admin or Organizer

    private boolean readFlag = false;

    /**
     * Default constructor required for Firestore data mapping.
     */
    public Notification() {}

    /**
     * Constructs a new Notification object.
     *
     * @param sender    The {@link Profile} of the user sending the notification. Cannot be null.
     * @param receiver  The {@link Profile} of the user receiving the notification. Cannot be null.
     * @param message   The content of the notification message. Cannot be null.
     * @param type      The type of notification: Admin or Organizer.
     * @param timestamp The time at which the notification was sent. Cannot be null.
     */
    public Notification(@NonNull Profile sender, @NonNull Profile receiver, @NonNull String message, @NonNull NotificationType type, @NonNull Timestamp timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.type = (type != null) ? type : NotificationType.ORGANIZER;
        this.timestamp = timestamp;
    }

    // --- Custom Serialization Logic ---

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        // Manually write the timestamp as milliseconds (long)
        long time = (timestamp != null) ? timestamp.toDate().getTime() : -1L;
        out.writeLong(time);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Manually read the long and convert back to Timestamp
        long time = in.readLong();
        if (time != -1L) {
            timestamp = new Timestamp(new Date(time));
        } else {
            timestamp = null;
        }
    }

    // --- Getters and Setters ---

    /**
     * Gets the sender's profile.
     *
     * @return The {@link Profile} of the sender.
     */
    public Profile getSender() {
        return sender;
    }

    /**
     * Sets the sender's profile.
     *
     * @param sender The {@link Profile} of the sender. Cannot be null.
     */
    public void setSender(@NonNull Profile sender) {
        this.sender = sender;
    }

    /**
     * Gets the receiver's profile.
     *
     * @return The {@link Profile} of the receiver.
     */
    public Profile getReceiver() {
        return receiver;
    }

    /**
     * Sets the receiver's profile.
     *
     * @param receiver The {@link Profile} of the receiver. Cannot be null.
     */
    public void setReceiver(@NonNull Profile receiver) {
        this.receiver = receiver;
    }

    /**
     * Gets the message content of the notification.
     *
     * @return The message string.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message content of the notification.
     *
     * @param message The message string. Cannot be null.
     */
    public void setMessage(@NonNull String message) {
        this.message = message;
    }

    /**
     * Gets the timestamp of when the notification was created.
     *
     * @return The {@link Timestamp} of the notification.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of the notification.
     *
     * @param timestamp The {@link Timestamp} to set. Cannot be null.
     */
    public void setTimestamp(@NonNull Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the read status of the notification.
     *
     * @return {@code true} if the notification has been read, {@code false} otherwise.
     */
    public boolean getReadFlag() {
        return readFlag;
    }

    /**
     * Sets the read status of the notification.
     *
     * @param read The boolean value for the read status.
     */
    public void setReadFlag(boolean read) {
        this.readFlag = read;
    }

    // --- Notification Type Methods ---

    /**
     * Gets the notification type.
     * <p>
     * This method is required for Firestore to correctly map the 'type' field.
     * </p>
     *
     * @return The {@link NotificationType} (ADMIN or ORGANIZER). Defaults to ORGANIZER if null.
     */
    public NotificationType getType() {
        return (type != null) ? type : NotificationType.ORGANIZER;
    }

    /**
     * Sets the notification type.
     * <p>
     * This method is required for Firestore to correctly save the 'type' field.
     * </p>
     *
     * @param type The {@link NotificationType} to set.
     */
    public void setType(NotificationType type) {
        this.type = type;
    }

    /**
     * Gets the notification type as an Enum (Alias for getType).
     *
     * @return The {@link NotificationType}.
     */
    public NotificationType getTypeEnum() {
        return getType();
    }

    /**
     * Gets the raw notification type value (potentially null).
     *
     * @return The raw {@link NotificationType} field.
     */
    public NotificationType getTypeRaw() {
        return type;
    }

    /**
     * Sets the notification type (Alias for setType).
     *
     * @param type The {@link NotificationType} to set.
     */
    public void setTypeEnum(NotificationType type) {
        setType(type);
    }
}