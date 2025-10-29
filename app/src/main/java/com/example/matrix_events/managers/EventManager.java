package com.example.matrix_events.managers;

import android.util.Log;

import com.example.matrix_events.database.DBConnector;
import com.example.matrix_events.database.DBListener;
import com.example.matrix_events.entities.Event;

import java.util.ArrayList;
import java.util.List;

public class EventManager implements DBListener<Event> {
    private static final String TAG = "DEBUG";

    private List<Event> events;
    private final DBConnector<Event> connector;
    private static EventManager manager = new EventManager();
    private EventManager() {
        events = new ArrayList<>();
        connector = new DBConnector<>("events", this, Event.class);
    }
    public static EventManager getInstance() {
        return manager;
    }

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
    public void createEvent() {
        Event e = new Event("Nikolai's Event", "Wicked cool description");
        connector.createAsync(e);
    }
    public void updateEvent(Event event) {
        connector.updateAsync(event);
    }

    @Override
    public void createAsync_Complete(Event object) {
        events.add(object);
        // TODO notify views
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

    @Override
    public void updateAsync_Complete(Event object) {
        for (Event event : events) {
            if (event.equals(object)) {
                event = object;
                break;
            }
        }
        // TODO notify views
    }

    @Override
    public void deleteAsync_Complete(Event object) {
        events.remove(object);
        // TODO notify views
    }
}
