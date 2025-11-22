package com.example.matrix_events.database;

import java.util.List;

/**
 * A generic listener interface for receiving callbacks from asynchronous database operations.
 * This interface is designed to be implemented by classes that need to react to the completion
 * of a database read operation, specifically when a list of objects is retrieved.
 *
 * @param <T> The type of {@link DBObject} that this listener will handle.
 */
public interface DBListener<T extends DBObject> {

    /**
     * Called when an asynchronous operation to read all documents from a collection has completed.
     *
     * @param objects A list of objects of type T retrieved from the database.
     *                This list may be empty if the collection contains no documents.
     */
    void readAllAsync_Complete(List<T> objects);
}