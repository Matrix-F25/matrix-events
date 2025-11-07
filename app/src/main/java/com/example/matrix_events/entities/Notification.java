package com.example.matrix_events.entities;

import com.example.matrix_events.database.DBObject;

import java.io.Serializable;
import com.google.firebase.Timestamp;

/**
 * Represents a notification to be sent from one user profile to another.
 * This class extends {@link DBObject} for Firestore compatibility and implements
 * {@link Serializable} to be passed between Android components. It includes
 * details about the sender, receiver, message content, and a timestamp.
 */
public class Notification extends DBObject implements Serializable {
    private Profile sender;
    private Profile receiver;
    private String message;
    private Timestamp timestamp;
    private boolean readFlag = false;

    /**
     * Default constructor required for Firestore data mapping.
     */
    public Notification() {}

    /**
     * Constructs a new Notification object.
     *
     * @param sender    The {@link Profile} of the user sending the notification.
     * @param receiver  The {@link Profile} of the user receiving the notification.
     * @param message   The content of the notification message.
     * @param timestamp The time at which the notification was sent.
     */
    public Notification(Profile sender, Profile receiver, String message, Timestamp timestamp) {
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
     * @param sender The {@link Profile} of the sender.
     */
    public void setSender(Profile sender) {
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
     * @param receiver The {@link Profile} of the receiver.
     */
    public void setReceiver(Profile receiver) {
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
     * @param message The message string.
     */
    public void setMessage(String message) {
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
     * @param timestamp The {@link Timestamp} to set.
     */
    public void setTimestamp(Timestamp timestamp) {
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
}