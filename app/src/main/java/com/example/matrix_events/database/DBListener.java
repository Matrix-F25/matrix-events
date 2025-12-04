package com.example.matrix_events.database;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * A generic listener interface for receiving callbacks from asynchronous database operations.
 * <p>
 * This interface is designed to be implemented by classes (typically Models or Controllers)
 * that need to react to the completion of a database read operation. It is specifically
 * used by {@link DBConnector} to pass data back to the application layer once a
 * Firestore snapshot listener returns new data.
 * </p>
 *
 * @param <T> The type of {@link DBObject} that this listener will handle (e.g., {@code Event}, {@code Profile}).
 */
public interface DBListener<T extends DBObject> {

    /**
     * Called when an asynchronous operation to read all documents from a collection has completed.
     * <p>
     * This method acts as the callback for real-time updates. Whenever the underlying
     * Firestore collection changes (add, update, delete), the {@link DBConnector}
     * parses the new state and invokes this method with the fresh list of objects.
     * </p>
     *
     * @param objects A list of objects of type T retrieved from the database.
     * This list will be empty (not null) if the collection contains no documents.
     */
    void readAllAsync_Complete(@NonNull List<T> objects);
}