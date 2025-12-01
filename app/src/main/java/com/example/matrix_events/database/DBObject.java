package com.example.matrix_events.database;

import java.io.Serializable;

/**
 * An abstract base class for objects that can be stored in the Firestore database.
 * It provides a common 'id' field to store the document ID from Firestore.
 * This class also overrides {@code equals()} and {@code hashCode()} to be based on the object's ID,
 * which is crucial for uniquely identifying and comparing database objects.
 */
public abstract class DBObject implements Serializable {
    private String id;

    /**
     * Retrieves the document ID of this object.
     * This ID is typically assigned by Firestore upon document creation.
     *
     * @return The unique document ID string.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the document ID for this object.
     * This is generally called internally by the {@link DBConnector} after an object is
     * created in or read from the database.
     *
     * @param id The unique document ID string to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Compares this DBObject to another object for equality.
     * Two DBObjects are considered equal if they are of the same class and their IDs are equal.
     *
     * @param obj The object to compare with this one.
     * @return {@code true} if the objects are of the same type and have the same ID; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return this.id.equals(((DBObject) obj).id);
    }

    /**
     * Returns a hash code value for the object.
     * The hash code is based on the object's ID, ensuring that two equal objects
     * have the same hash code, as required by the general contract of {@code Object.hashCode()}.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}