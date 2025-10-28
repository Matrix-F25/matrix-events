package com.example.matrix_events.database;

import java.util.List;

public interface DBListener<T extends DBObject> {
    void createAsync_Complete(T object);
    void readAllAsync_Complete(List<T> objects);
    void deleteAsync_Complete(T object);
    void database_Changed();
}
