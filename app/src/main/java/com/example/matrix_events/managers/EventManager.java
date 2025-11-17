package com.example.matrix_events.managers;

import android.util.Log;

import com.example.matrix_events.database.DBConnector;
import com.example.matrix_events.database.DBListener;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.mvc.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all event-related data and operations within the application.
 * This class follows the singleton pattern to provide a single point of access to the event data.
 * It connects to the Firestore 'events' collection and maintains a local cache of {@link Event} objects.
 * As a {@link Model} in the MVC pattern, it notifies registered views of any data changes.
 */
public class EventManager extends Model implements DBListener<Event> {
    private static final String TAG = "EventManager";

    private List<Event> events = new ArrayList<>();
    private final DBConnector<Event> connector = new DBConnector<Event>("events", this, Event.class);

    // Singleton
    private static EventManager manager = new EventManager();
    /**
     * Gets the singleton instance of the EventManager.
     *
     * @return The single, static instance of EventManager.
     */
    public static EventManager getInstance() {
        return manager;
    }

    // Event getters
    /**
     * Retrieves the local cache of all events.
     *
     * @return A list of all {@link Event} objects currently held by the manager.
     */
    public List<Event> getEvents() { return events; }

    /**
     * Finds and retrieves an event by its unique Firestore document ID.
     *
     * @param id The Firestore document ID of the event.
     * @return The {@link Event} object with the matching ID, or {@code null} if no event is found.
     */
    public Event getEventByDBID(String id) {
        for (Event event : events) {
            if (event.getId().equals(id)) {
                return event;
            }
        }
        return null;
    }

    /**
     * Finds and retrieves an event by its name. Note: This assumes event names are unique.
     *
     * @param name The name of the event to find.
     * @return The first {@link Event} object with the matching name, or {@code null} if no event is found.
     */
    public Event getEventByName(String name) {
        for (Event event : events) {
            if (event.getName().equals(name)) {
                return event;
            }
        }
        return null;
    }

    /**
     * Checks if an event with the given name exists.
     *
     * @param name The name of the event to check.
     * @return {@code true} if an event with the specified name exists, {@code false} otherwise.
     */
    public boolean doesEventExist(String name) {
        return getEventByName(name) != null;
    }

    /**
     * Filters and returns a list of events for which the registration period has not yet closed.
     * This includes events where registration is currently open or will open in the future.
     *
     * @return A list of {@link Event} objects with open registration.
     */
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

    public List<Event> getOrganizerEventsRegistrationNotClosed(String deviceID) {
        List<Event> organizerEventsNotClosed = new ArrayList<>();
        for (Event event : events) {
            if (!event.isRegistrationClosed() && event.getOrganizer().getDeviceId().equals(deviceID)) {
                // Registration closes in the future and deviceID is the organizer
                organizerEventsNotClosed.add(event);
            }
        }
        return organizerEventsNotClosed;
    }

    /**
     * Filters and returns a list of events for which the registration period has closed.
     *
     * @return A list of {@link Event} objects with closed registration.
     */
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

    public List<Event> getOrganizerEventsRegistrationClosed(String deviceID) {
        List<Event> organizerEventsClosed = new ArrayList<>();
        for (Event event : events) {
            if (event.isRegistrationClosed() && event.getOrganizer().getDeviceId().equals(deviceID)) {
                // Registration is closed and deviceID is the organizer
                organizerEventsClosed.add(event);
            }
        }
        return organizerEventsClosed;
    }

    /**
     * Retrieves all events for which a specific user is on the waitlist.
     *
     * @param deviceID The device ID of the user.
     * @return A list of {@link Event} objects where the user is on the waitlist.
     */
    public List<Event> getEventsInWaitlist(String deviceID) {
        List<Event> eventsInWaitlist = new ArrayList<>();
        for (Event event : events) {
            if (event.inWaitList(deviceID)) {
                eventsInWaitlist.add(event);
            }
        }
        return eventsInWaitlist;
    }

    /**
     * Retrieves all events for which a specific user is on the pending list (invited).
     *
     * @param deviceID The device ID of the user.
     * @return A list of {@link Event} objects where the user is on the pending list.
     */
    public List<Event> getEventsInPending(String deviceID) {
        List<Event> eventsInPending = new ArrayList<>();
        for (Event event : events) {
            if (event.inPendingList(deviceID)) {
                eventsInPending.add(event);
            }
        }
        return eventsInPending;
    }

    /**
     * Retrieves all events that a specific user has been accepted into (is attending).
     *
     * @param deviceID The device ID of the user.
     * @return A list of {@link Event} objects where the user is on the accepted list.
     */
    public List<Event> getEventsInAccepted(String deviceID) {
        List<Event> eventsInAccepted = new ArrayList<>();
        for (Event event : events) {
            if (event.inAcceptedList(deviceID)) {
                eventsInAccepted.add(event);
            }
        }
        return eventsInAccepted;
    }

    /**
     * Retrieves all events that a specific user has declined.
     *
     * @param deviceID The device ID of the user.
     * @return A list of {@link Event} objects where the user is on the declined list.
     */
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

    /**
     * Asynchronously creates a new event in the Firestore database.
     *
     * @param event The {@link Event} object to create.
     */
    public void createEvent(Event event) { connector.createAsync(event); }

    /**
     * Asynchronously updates an existing event in the Firestore database.
     *
     * @param event The {@link Event} object with updated data. Its ID must be set.
     */
    public void updateEvent(Event event) { connector.updateAsync(event); }

    /**
     * Asynchronously deletes an event from the Firestore database.
     *
     * @param event The {@link Event} object to delete. Its ID must be set.
     */
    public void deleteEvent(Event event) { connector.deleteAsync(event); }

    /**
     * Callback method invoked by {@link DBConnector} when the event data changes in Firestore.
     * It updates the local event cache and notifies all registered views of the change.
     *
     * @param objects The updated list of {@link Event} objects from Firestore.
     */
    @Override
    public void readAllAsync_Complete(List<Event> objects) {
        Log.d(TAG, "EventManager read all complete, notifying views");
        events = objects;
        // Notify views of event changes
        notifyViews();
    }
}