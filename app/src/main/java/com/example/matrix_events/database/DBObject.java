package com.example.matrix_events.database;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * An abstract base class for objects that can be stored in the Firestore database.
 * <p>
 * This class provides a common structure for all database entities (like Events, Profiles, etc.).
 * It manages the Firestore Document ID, which is essential for:
 * <ul>
 * <li>Uniquely identifying objects within the app.</li>
 * <li>Performing updates and deletions in the database.</li>
 * <li>Determining object equality in Lists and Sets.</li>
 * </ul>
 * It implements {@link Serializable} so that any subclass can be passed between Android
 * activities via Intents.
 * </p>
 */
public abstract class DBObject implements Serializable {
    private String id;

    /**
     * Retrieves the document ID of this object.
     * <p>
     * This ID is typically assigned by Firestore automatically upon document creation.
     * If the object has not yet been saved to the database, this may be null.
     * </p>
     *
     * @return The unique document ID string, or {@code null} if not yet assigned.
     */
    @Nullable
    public String getId() {
        return id;
    }

    /**
     * Sets the document ID for this object.
     * <p>
     * This is generally called internally by the {@link DBConnector} immediately after an object is
     * created in or read from the database. It ensures the local object matches the remote document.
     * </p>
     *
     * @param id The unique document ID string to set.
     */
    public void setId(@Nullable String id) {
        this.id = id;
    }

    /**
     * Compares this DBObject to another object for equality.
     * <p>
     * Two DBObjects are considered equal if:
     * <ol>
     * <li>They are of the exact same class.</li>
     * <li>Their IDs are both non-null and identical.</li>
     * </ol>
     * If either object has a null ID (e.g., it hasn't been saved yet), they are considered not equal
     * (unless they are the exact same instance in memory).
     * </p>
     *
     * @param obj The object to compare with this one.
     * @return {@code true} if the objects are of the same type and have the same ID; {@code false} otherwise.
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DBObject dbObject = (DBObject) obj;
        // Use Objects.equals for null safety
        return Objects.equals(id, dbObject.id);
    }

    /**
     * Returns a hash code value for the object.
     * <p>
     * The hash code is based on the object's ID, ensuring that two equal objects
     * have the same hash code. If the ID is null, it returns 0.
     * </p>
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}