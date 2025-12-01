package com.example.matrix_events.entities;

import androidx.annotation.NonNull;

import com.example.matrix_events.database.DBObject;

import java.io.Serializable;

/**
 * Represents an event poster.
 * <p>
 * This class stores information about the poster image, including its URL in Firebase Storage,
 * the ID of the event it is associated with, and the specific file name.
 * It extends {@link DBObject} to be compatible with the Firestore database and
 * implements {@link Serializable} to be passable between Android components.
 * </p>
 */
public class Poster extends DBObject implements Serializable {
    private String imageUrl;
    private String eventId;
    private String fileName;

    /**
     * Default constructor required for Firestore data mapping.
     */
    public Poster() {}

    /**
     * Constructs a new Poster object.
     *
     * @param imageUrl The public URL where the poster image can be downloaded. Cannot be null.
     * @param eventId  The unique identifier of the event this poster is for. Cannot be null.
     * @param fileName The name of the image file as stored in Firebase Storage. Cannot be null.
     */
    public Poster(@NonNull String imageUrl, @NonNull String eventId, @NonNull String fileName) {
        this.imageUrl = imageUrl;
        this.eventId = eventId;
        this.fileName = fileName;
    }

    /**
     * Gets the public URL of the poster image.
     *
     * @return A string representing the image URL.
     */
    public String getImageUrl() { return imageUrl; }

    /**
     * Sets the public URL of the poster image.
     *
     * @param imageUrl The string URL of the image. Cannot be null.
     */
    public void setImageUrl(@NonNull String imageUrl) { this.imageUrl = imageUrl; }

    /**
     * Gets the ID of the event associated with this poster.
     *
     * @return The event ID string.
     */
    public String getEventId() { return eventId; }

    /**
     * Sets the ID of the event associated with this poster.
     *
     * @param eventId The event ID string. Cannot be null.
     */
    public void setEventId(@NonNull String eventId) { this.eventId = eventId; }

    /**
     * Gets the file name of the poster image in Firebase Storage.
     * <p>
     * This is useful for managing the file lifecycle, such as deleting the old image
     * from storage when a poster is updated or removed.
     * </p>
     *
     * @return The file name string.
     */
    public String getFileName() { return fileName; }

    /**
     * Sets the file name of the poster image in Firebase Storage.
     *
     * @param fileName The file name string. Cannot be null.
     */
    public void setFileName(@NonNull String fileName) { this.fileName = fileName; }
}