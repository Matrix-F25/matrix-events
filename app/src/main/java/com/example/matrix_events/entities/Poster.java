package com.example.matrix_events.entities;

import com.example.matrix_events.database.DBObject;

import java.io.Serializable;

/**
 * Represents an event poster.
 * This class stores information about the poster image, including its URL in Firebase Storage,
 * the ID of the event it is associated with, and the specific file name.
 * It extends {@link DBObject} to be compatible with the Firestore database and
 * implements {@link Serializable} to be passable between Android components.
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
     * @param imageUrl The public URL where the poster image can be downloaded.
     * @param eventId  The unique identifier of the event this poster is for.
     * @param fileName The name of the image file as stored in Firebase Storage.
     */
    public Poster(String imageUrl, String eventId, String fileName) {
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
     * @param imageUrl The string URL of the image.
     */
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /**
     * Gets the ID of the event associated with this poster.
     *
     * @return The event ID string.
     */
    public String getEventId() { return eventId; }

    /**
     * Sets the ID of the event associated with this poster.
     *
     * @param eventId The event ID string.
     */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /**
     * Gets the file name of the poster image in Firebase Storage.
     * This is useful for managing the file, such as for deletion.
     *
     * @return The file name string.
     */
    public String getFileName() { return fileName; }

    /**
     * Sets the file name of the poster image in Firebase Storage.
     *
     * @param fileName The file name string.
     */
    public void setFileName(String fileName) { this.fileName = fileName; }
}
