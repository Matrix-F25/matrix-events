package com.example.matrix_events.entities;

import androidx.annotation.NonNull;

import com.example.matrix_events.database.DBObject;

import java.io.IOException;
import java.io.Serializable;
import com.google.firebase.Timestamp;

/**
 * Represents a notification to be sent from one user profile to another.
 * <p>
 * This class extends {@link DBObject} for Firestore compatibility and implements
 * {@link Serializable} to be passed between Android components (e.g., via Intents).
 * It includes details about the sender, receiver, message content, and a timestamp.
 * </p>
 */
public class Notification extends DBObject implements Serializable {
    private Profile sender;
    private Profile receiver;
    private String message;
    private transient Timestamp timestamp;
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
     * @param timestamp The time at which the notification was sent. Cannot be null.
     */
    public Notification(@NonNull Profile sender, @NonNull Profile receiver, @NonNull String message, @NonNull Timestamp timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.timestamp = timestamp;
    }

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

    /**
     * Custom serialization logic to handle the {@code transient} Firebase {@link Timestamp} field.
     * <p>
     * Since the Firebase {@code Timestamp} class is not {@code Serializable}, this method
     * converts the timestamp to milliseconds (long) and writes it manually to the output stream.
     * If the timestamp is null, a value of -1L is written.
     * </p>
     *
     * @param out The {@link java.io.ObjectOutputStream} to write to.
     * @throws IOException If an I/O error occurs while writing the object.
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        // Manually write the Timestamp as milliseconds (long)
        out.writeLong(timestamp != null ? timestamp.toDate().getTime() : -1L);
    }

    /**
     * Custom deserialization logic to reconstruct the {@code transient} Firebase {@link Timestamp} field.
     * <p>
     * This method reads the time in milliseconds (long) from the input stream and
     * reconstructs the {@code Timestamp} object. If the read value is -1L, the timestamp remains null.
     * </p>
     *
     * @param in The {@link java.io.ObjectInputStream} to read from.
     * @throws IOException            If an I/O error occurs while reading the object.
     * @throws ClassNotFoundException If the class of a serialized object cannot be found.
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Manually read the milliseconds and reconstruct the Firebase Timestamp
        long time = in.readLong();
        if (time != -1L) {
            this.timestamp = new Timestamp(new java.util.Date(time));
        } else {
            this.timestamp = null;
        }
    }
}