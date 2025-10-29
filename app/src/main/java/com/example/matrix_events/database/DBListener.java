package com.example.matrix_events.database;

import java.util.List;

public interface DBListener<T extends DBObject> {
    void readAllAsync_Complete(List<T> objects);
}
