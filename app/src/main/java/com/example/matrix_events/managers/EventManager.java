package com.example.matrix_events.managers;

import android.util.Log;

import com.example.matrix_events.database.DBConnector;
import com.example.matrix_events.database.DBListener;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Notification;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.mvc.Model;
import com.google.firebase.Timestamp;

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
    public List<Event> getEvents() {
        return events;
    }

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

    /**
     * Filters and returns a list of events organized by a specific user where registration is NOT closed.
     * Use this to show an organizer their active or upcoming events.
     *
     * @param deviceID The device ID of the organizer.
     * @return A filtered list of {@link Event} objects managed by the specified organizer.
     */
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

    /**
     * Filters and returns a list of events organized by a specific user where registration IS closed.
     * Use this to show an organizer their past or in-progress events where new entrants cannot join.
     *
     * @param deviceID The device ID of the organizer.
     * @return A filtered list of {@link Event} objects managed by the specified organizer.
     */
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
    public void createEvent(Event event) {
        connector.createAsync(event);
    }

    /**
     * Asynchronously updates an existing event in the Firestore database.
     *
     * @param event The {@link Event} object with updated data. Its ID must be set.
     */
    public void updateEvent(Event event) {
        connector.updateAsync(event);
    }

    /**
     * Asynchronously deletes an event from the Firestore database.
     * <p>
     * If the event has an associated poster image, the poster is also deleted from storage.
     * </p>
     *
     * @param event The {@link Event} object to delete. Its ID must be set.
     */
    public void deleteEvent(Event event) {

        if (event.getPoster() != null && event.getPoster().getImageUrl() != null) {
            PosterManager.getInstance().deletePoster(event.getPoster());
        }

        connector.deleteAsync(event);
    }

    /**
     * Cancels an event, notifies all associated users, and deletes the event record.
     * <p>
     * This method iterates through all user lists (Waitlist, Pending, Accepted, Declined),
     * sends a cancellation notification to each user, and then permanently deletes the event.
     * </p>
     *
     * @param event   The {@link Event} to be cancelled.
     * @param message The cancellation message to send to all users.
     */
    public void cancelEventAndNotifyUsers(Event event, String message) {

        List<String> usersToNotify = new ArrayList<>();

        if (event.getWaitList() != null) {
            usersToNotify.addAll(event.getWaitList());
        }
        if (event.getPendingList() != null) {
            usersToNotify.addAll(event.getPendingList());
        }
        if (event.getAcceptedList() != null) {
            usersToNotify.addAll(event.getAcceptedList());
        }
        if (event.getDeclinedList() != null) {
            usersToNotify.addAll(event.getDeclinedList());
        }

        Profile sender = event.getOrganizer();
        Timestamp currentTime = Timestamp.now();

        for (String userId : usersToNotify) {
            Profile receiver = ProfileManager.getInstance().getProfileByDeviceId(userId);
            if (receiver != null) {
                Notification notification = new Notification(sender, receiver, message, Notification.NotificationType.ORGANIZER, currentTime);
                NotificationManager.getInstance().createNotification(notification);
            }
        }

        deleteEvent(event);
    }

    /**
     * Removes a specific user from all events in the system.
     * <p>
     * This method is typically called when a user deletes their profile or is banned.
     * <ul>
     * <li>If the user is an <b>Organizer</b>, their events are cancelled, and participants are notified.</li>
     * <li>If the user is a <b>Participant</b>, they are removed from all lists (Waitlist, Pending, etc.)
     * in any event they are associated with.</li>
     * </ul>
     * </p>
     *
     * @param deviceId The unique device ID of the user to remove.
     */
    public void removeFromAllEvents(String deviceId) {
        List<Event> allEvents = new ArrayList<>(events);

        for (Event event : allEvents) {

            if (event.getOrganizer() != null && event.getOrganizer().getDeviceId().equals(deviceId)) {
                String organizerRemovedMessage = "Urgent: The event '" + event.getName() + "' has been cancelled because the organizer's account was removed.";
                cancelEventAndNotifyUsers(event, organizerRemovedMessage);
                continue;
            }

            // attempt to remove the user from all lists
            boolean removeFromWaitlist = event.getWaitList().remove(deviceId);
            boolean removeFromPendingList = event.getPendingList().remove(deviceId);
            boolean removeFromAcceptedList = event.getAcceptedList().remove(deviceId);
            boolean removeFromDeclinedList = event.getDeclinedList().remove(deviceId);

            // if the user has been removed from any list, update the event
            if (removeFromWaitlist || removeFromPendingList || removeFromAcceptedList || removeFromDeclinedList) {
                updateEvent(event);
            }
        }
    }

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