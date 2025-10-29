package com.example.matrix_events.managers;

import android.util.Log;

import com.example.matrix_events.database.DBConnector;
import com.example.matrix_events.database.DBListener;
import com.example.matrix_events.entities.Event;

import java.util.ArrayList;
import java.util.List;

public class EventManager implements DBListener<Event> {
    private static final String TAG = "EventManager";

    private List<Event> events;
    private final DBConnector<Event> connector;

    // Singleton
    private static EventManager manager = new EventManager();
    private EventManager() {
        events = new ArrayList<>();
        connector = new DBConnector<Event>("events", this, Event.class);
    }
    public static EventManager getInstance() {
        return manager;
    }

    // Event getters for views
    public Event getEvent(String id) {
        for (Event event : events) {
            if (event.getId().equals(id)) {
                return event;
            }
        }
        return null;
    }
    public List<Event> getEvents() {
        return events;
    }

    // Create, update, delete operations for organizers and admins
    public void createEvent(Event event) { connector.createAsync(event); }
    public void updateEvent(Event event) {
        connector.updateAsync(event);
    }
    public void deleteEvent(Event event) {
        connector.deleteAsync(event);
    }

    @Override
    public void readAllAsync_Complete(List<Event> objects) {
        events = objects;
        Log.d(TAG, "Read async complete");
        for (Event e : objects) {
            Log.d(TAG, e.getId());
        }
        // TODO notify views
    }
}
