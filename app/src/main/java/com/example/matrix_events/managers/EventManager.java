package com.example.matrix_events.managers;

import android.util.Log;

import com.example.matrix_events.database.DBConnector;
import com.example.matrix_events.database.DBListener;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.mvc.Model;

import java.util.ArrayList;
import java.util.List;

public class EventManager extends Model implements DBListener<Event> {
    private static final String TAG = "EventManager";

    private List<Event> events = new ArrayList<>();
    private final DBConnector<Event> connector = new DBConnector<Event>("events", this, Event.class);

    // Singleton
    private static EventManager manager = new EventManager();
    public static EventManager getInstance() {
        return manager;
    }

    // Event getters
    public List<Event> getEvents() { return events; }

    public Event getEventByDBID(String id) {
        for (Event event : events) {
            if (event.getId().equals(id)) {
                return event;
            }
        }
        return null;
    }

    public Event getEventByName(String name) {
        for (Event event : events) {
            if (event.getName().equals(name)) {
                return event;
            }
        }
        return null;
    }

    public boolean doesEventExist(String name) {
        return getEventByName(name) != null;
    }

    public List<Event> getEventsRegistrationNotClosed() {
        List<Event> eventsNotClosed = new ArrayList<>();
        for (Event event : events) {
            if (!event.isRegistrationClosed()) {
                // Registration closes in the future
                eventsNotClosed.add(event);
            }
        }
        return eventsNotClosed;
    }

    public List<Event> getEventsRegistrationClosed() {
        List<Event> eventsClosed = new ArrayList<>();
        for (Event event : events) {
            if (event.isRegistrationClosed()) {
                // Registration is closed
                eventsClosed.add(event);
            }
        }
        return eventsClosed;
    }

    public List<Event> getEventsInWaitlist(String deviceID) {
        List<Event> eventsInWaitlist = new ArrayList<>();
        for (Event event : events) {
            if (event.inWaitList(deviceID)) {
                eventsInWaitlist.add(event);
            }
        }
        return eventsInWaitlist;
    }

    public List<Event> getEventsInPending(String deviceID) {
        List<Event> eventsInPending = new ArrayList<>();
        for (Event event : events) {
            if (event.inPendingList(deviceID)) {
                eventsInPending.add(event);
            }
        }
        return eventsInPending;
    }

    public List<Event> getEventsInAccepted(String deviceID) {
        List<Event> eventsInAccepted = new ArrayList<>();
        for (Event event : events) {
            if (event.inAcceptedList(deviceID)) {
                eventsInAccepted.add(event);
            }
        }
        return eventsInAccepted;
    }

    public List<Event> getEventsInDeclined(String deviceID) {
        List<Event> eventsInDeclined = new ArrayList<>();
        for (Event event : events) {
            if (event.inDeclinedList(deviceID)) {
                eventsInDeclined.add(event);
            }
        }
        return eventsInDeclined;
    }

    // Create, update, delete operations for organizers and admins
    public void createEvent(Event event) { connector.createAsync(event); }
    public void updateEvent(Event event) { connector.updateAsync(event); }
    public void deleteEvent(Event event) { connector.deleteAsync(event); }

    @Override
    public void readAllAsync_Complete(List<Event> objects) {
        Log.d(TAG, "EventManager read all complete, notifying views");
        events = objects;
        // Notify views of event changes
        notifyViews();
    }
}
