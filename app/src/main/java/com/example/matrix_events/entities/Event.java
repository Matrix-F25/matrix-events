package com.example.matrix_events.entities;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.matrix_events.database.DBObject;
import com.example.matrix_events.managers.NotificationManager;
import com.example.matrix_events.managers.ProfileManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an event within the application.
 * This class extends {@link DBObject} to be compatible with the Firestore database
 * and implements {@link Serializable} to be passable between Android components.
 * It holds all the details about an event, including its name, description, organizer,
 * location, timing, capacity, and attendee lists.
 */
public class Event extends DBObject implements Serializable {
    private String name;
    private String description;
    private Profile organizer;
    private String location;
    private transient Timestamp eventStartDateTime;
    private transient Timestamp eventEndDateTime;
    private Integer eventCapacity;
    private Integer waitlistCapacity;                       // can be null if no limit
    private transient Timestamp registrationStartDateTime;
    private transient Timestamp registrationEndDateTime;
    private Boolean isReoccurring = false;
    private transient Timestamp reoccurringEndDateTime;     // can be null if not reoccurring
    private ReoccurringType reoccurringType;                // can be null if not reoccurring
    private Boolean requireGeolocationTracking = true;
    private transient HashMap<String, GeoPoint> geolocationMap = new HashMap<>(); ;   // store deviceID -> location of where entrants joined the event IF requireGeolocationTracking
    private Poster poster;                                  // can be null if no poster
    private String qrCodeHash;
    private List<String> waitList = new ArrayList<>();      // holds profile deviceId
    private List<String> pendingList = new ArrayList<>();
    private List<String> acceptedList = new ArrayList<>();
    private List<String> declinedList = new ArrayList<>();
    private boolean registrationOpened = false;             // set by firestore cloud function
    private boolean lotteryProcessed = false;               // set by firestore cloud function
    private boolean pendingExpired = false;                 // set by firestore cloud function

    /**
     * Default constructor required for Firestore data mapping.
     */
    public Event() {}

    /**
     * Constructs a new Event object with specified details.
     * Performs validation to ensure that the event dates and times are logical.
     *
     * @param name                       The name of the event. Cannot be null.
     * @param description                The description of the event. Cannot be null.
     * @param organizer                  The profile of the event organizer. Cannot be null.
     * @param location                   The string location of the event. Cannot be null.
     * @param eventStartDateTime         The start date and time of the event. Cannot be null.
     * @param eventEndDateTime           The end date and time of the event. Cannot be null.
     * @param eventCapacity              The maximum number of attendees for the event. Cannot be null.
     * @param waitlistCapacity           The maximum number of users on the waitlist. Can be null for no limit.
     * @param registrationStartDateTime  The start date and time for registration. Cannot be null.
     * @param registrationEndDateTime    The end date and time for registration. Cannot be null.
     * @param isReoccurring              Flag indicating if the event is reoccurring. Cannot be null.
     * @param reoccurringEndDateTime     The end date for reoccurring events. Can be null if not reoccurring.
     * @param reoccurringType            The type of reoccurrence (e.g., daily, weekly). Can be null if not reoccurring.
     * @param requireGeolocationTracking Flag indicating if geolocation tracking is required for this event. Cannot be null.
     * @param poster                     The poster for the event. Can be null if there is no poster.
     * @throws IllegalArgumentException if any date/time constraints are violated.
     */
    public Event(@NonNull String name, @NonNull String description, @NonNull Profile organizer,
                 @NonNull String location, @NonNull Timestamp eventStartDateTime, @NonNull Timestamp eventEndDateTime,
                 @NonNull Integer eventCapacity, Integer waitlistCapacity,
                 @NonNull Timestamp registrationStartDateTime, @NonNull Timestamp registrationEndDateTime,
                 @NonNull Boolean isReoccurring, Timestamp reoccurringEndDateTime, ReoccurringType reoccurringType,
                 @NonNull Boolean requireGeolocationTracking, Poster poster) {
        // Logic checks
        Timestamp now = Timestamp.now();
        if (registrationStartDateTime.compareTo(now) <= 0)
            throw new IllegalArgumentException("Registration start date must be in the future!");
        if (registrationEndDateTime.compareTo(registrationStartDateTime) <= 0)
            throw new IllegalArgumentException("Registration end date must be after registration start date!");
        if (eventStartDateTime.compareTo(registrationEndDateTime) <= 0)
            throw new IllegalArgumentException("Event start date must be after registration end date!");
        if (eventEndDateTime.compareTo(eventStartDateTime) <= 0)
            throw new IllegalArgumentException("Event end date must be after event start date!");
        if (isReoccurring) {
            if (reoccurringEndDateTime == null)
                throw new IllegalArgumentException("ReoccurringEndDateTime cannot be null if event is reoccurring!");
            if (reoccurringType == null)
                throw new IllegalArgumentException("ReoccurringType cannot be null if event is reoccurring!");
            if (reoccurringEndDateTime.compareTo(eventEndDateTime) <= 0)
                throw new IllegalArgumentException("Reoccurring end date must be after event end date!");
        }

        // Assign mandatory fields
        this.name = name;
        this.description = description;
        this.organizer = organizer;
        this.location = location;
        this.eventStartDateTime = eventStartDateTime;
        this.eventEndDateTime = eventEndDateTime;
        this.eventCapacity = eventCapacity;
        this.registrationStartDateTime = registrationStartDateTime;
        this.registrationEndDateTime = registrationEndDateTime;
        this.isReoccurring = isReoccurring;
        this.requireGeolocationTracking = requireGeolocationTracking;

        // Optional fields (nullable)
        this.waitlistCapacity = waitlistCapacity;              // null: no waitlist limit
        this.reoccurringEndDateTime = reoccurringEndDateTime;  // null: not reoccurring
        this.reoccurringType = reoccurringType;                // null: not reoccurring
        this.poster = poster;                                  // null: no poster
    }

    // Event date and time checks

    /**
     * Checks if the current time is before the registration period begins.
     * @return {@code true} if registration has not yet started, {@code false} otherwise.
     */
    @Exclude
    public boolean isBeforeRegistrationStart() {
        Timestamp now = Timestamp.now();
        // Check if registration is yet to open
        return !registrationOpened;
    }

    /**
     * Checks if the event is currently within its registration period.
     * @return {@code true} if registration is currently open, {@code false} otherwise.
     */
    @Exclude
    public boolean isRegistrationOpen() {
        Timestamp now = Timestamp.now();
        // Check if registration is currently open
        return registrationOpened && !lotteryProcessed;
    }

    /**
     * Checks if the registration period for the event has ended.
     * @return {@code true} if registration is closed, {@code false} otherwise.
     */
    @Exclude
    public boolean isRegistrationClosed() {
        Timestamp now = Timestamp.now();
        // Check if registration is closed
        return registrationOpened && lotteryProcessed;
    }

    /**
     * Checks if the current time is before the event is scheduled to start.
     * @return {@code true} if the event has not yet started, {@code false} otherwise.
     */
    @Exclude
    public boolean isBeforeEventStart() {
        Timestamp now = Timestamp.now();
        // Check if event is yet to start
        return !pendingExpired;
    }

    /**
     * Checks if the event is currently in progress.
     * @return {@code true} if the event is ongoing, {@code false} otherwise.
     */
    @Exclude
    public boolean isEventOngoing() {
        Timestamp now = Timestamp.now();
        // Check if event is currently going on
        return pendingExpired && !isEventComplete();
    }

    /**
     * Checks if the event (or its reoccurrence period) has concluded.
     * @return {@code true} if the event is complete, {@code false} otherwise.
     */
    @Exclude
    public boolean isEventComplete() {
        Timestamp now = Timestamp.now();
        if (isReoccurring) {
            // Check if reoccurring event has ended
            return reoccurringEndDateTime.compareTo(now) < 0;
        }
        else {
            // Check if event has ended
            return eventEndDateTime.compareTo(now) < 0;
        }
    }

    // Event join / leave / accept / decline list mechanics

    /**
     * Checks if a user is on the waitlist.
     * @param deviceId The device ID of the user to check.
     * @return {@code true} if the user is on the waitlist, {@code false} otherwise.
     */
    public boolean inWaitList(@NonNull String deviceId) {
        return waitList.contains(deviceId);
    }

    /**
     * Adds a user to the event's waitlist if conditions are met.
     * The user is added only if registration is open, the waitlist is not full,
     * and the user is not already on the waitlist.
     * @param deviceID The device ID of the user to add.
     * @param userLocation The geolocation of the user (can be null).
     */
    public void joinWaitList(@NonNull String deviceID, GeoPoint userLocation) {
        // Check if registration is open
        if (!isRegistrationOpen()) {
            return;
        }
        // Check if room in waitlist
        if (waitlistCapacity != null) {
            if (waitList.size() >= waitlistCapacity) {
                return;
            }
        }
        // Check if already in waitlist
        if (inWaitList(deviceID)) {
            return;
        }
        waitList.add(deviceID);

        // Store location if required and provided
        if (requireGeolocationTracking && userLocation != null) {
            geolocationMap.put(deviceID, userLocation);
        }
    }

    /**
     * Removes a user from the event's waitlist.
     * @param deviceID The device ID of the user to remove.
     * @return {@code true} if the user was successfully removed, {@code false} otherwise.
     */
    public boolean leaveWaitList(@NonNull String deviceID) {
        // remove deviceID from geolocationMap
        geolocationMap.remove(deviceID);
        // remove deviceID from waitlist
        return waitList.remove(deviceID);
    }

    /**
     * Checks if a user is on the pending list (invited but not yet responded).
     * @param deviceId The device ID of the user to check.
     * @return {@code true} if the user is on the pending list, {@code false} otherwise.
     */
    public boolean inPendingList(@NonNull String deviceId) {
        return pendingList.contains(deviceId);
    }

    /**
     * Checks if a user is on the accepted list (attending the event).
     * @param deviceId The device ID of the user to check.
     * @return {@code true} if the user is on the accepted list, {@code false} otherwise.
     */
    public boolean inAcceptedList(@NonNull String deviceId) {
        return acceptedList.contains(deviceId);
    }

    /**
     * Moves a user from the pending list to the accepted list.
     * This action is only valid if registration is closed, the event has not started,
     * there is capacity, and the user is currently on the pending list.
     * @param deviceId The device ID of the user to accept.
     */
    public void joinAcceptedList(@NonNull String deviceId) {
        // Check if registration is closed and event has not started
        if (!isRegistrationClosed() || !isBeforeEventStart()) {
            return;
        }
        // Check if room in event
        if (acceptedList.size() >= eventCapacity) {
            return;
        }
        // Check if in pending list
        if (!inPendingList(deviceId)) {
            return;
        }
        // Check if already in accepted list
        if (inAcceptedList(deviceId)) {
            return;
        }
        pendingList.remove(deviceId);
        acceptedList.add(deviceId);
    }

    /**
     * Checks if a user is on the declined list.
     * @param deviceId The device ID of the user to check.
     * @return {@code true} if the user is on the declined list, {@code false} otherwise.
     */
    public boolean inDeclinedList(@NonNull String deviceId) {
        return declinedList.contains(deviceId);
    }

    /**
     * Moves a user from the pending list to the declined list.
     * If a user declines, this method attempts to offer their spot to the next user on the waitlist
     * by moving them to the pending list and sending them a notification.
     * @param deviceId The device ID of the user to decline.
     */
    public void joinDeclinedList(@NonNull String deviceId) {
        // Check if registration is closed and event has not started
        if (!isRegistrationClosed() || !isBeforeEventStart()) {
            return;
        }
        // Check if in pending list
        if (!inPendingList(deviceId)) {
            return;
        }
        // Check if already in declined list
        if (inDeclinedList(deviceId)) {
            return;
        }
        pendingList.remove(deviceId);
        declinedList.add(deviceId);

        // Second Chance! Lottery select another entrant in the waitlist
        if (!waitList.isEmpty()) {
            String secondChance = waitList.get(0);
            waitList.remove(secondChance);
            pendingList.add(secondChance);

            // Send notification to winner
            Profile sender = this.organizer;
            Profile receiver = ProfileManager.getInstance().getProfileByDeviceId(secondChance);
            if (receiver == null) {
                Log.d("DEBUG", "Error: Profile for winner deviceId " + secondChance + " not found. Skipping notification for this user.");
                return;
            }
            String message = "It's your lucky day! You have been "
                    + "second chance selected for the " + name + " "
                    + "event. Please accept or decline the invitation "
                    + "at your earliest convenience.\n\n"
                    + "This is an automated message.";
            Timestamp now = Timestamp.now();
            Notification notification = new Notification(sender, receiver, message, Notification.NotificationType.ORGANIZER, now);
            NotificationManager.getInstance().createNotification(notification);
        }
    }

    // Event getters and setters

    /**
     * Gets the name of the event.
     * @return The event name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the event.
     * @param name The new event name. Cannot be null.
     */
    public void setName(@NonNull String name) {
        this.name = name;
    }

    /**
     * Gets the description of the event.
     * @return The event description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the event.
     * @param description The new event description. Cannot be null.
     */
    public void setDescription(@NonNull String description) {
        this.description = description;
    }

    /**
     * Gets the profile of the event organizer.
     * @return The organizer's profile.
     */
    public Profile getOrganizer() {
        return organizer;
    }

    /**
     * Sets the profile of the event organizer.
     * @param organizer The new organizer's profile. Cannot be null.
     */
    public void setOrganizer(@NonNull Profile organizer) {
        this.organizer = organizer;
    }

    /**
     * Gets the location of the event.
     * @return The event's string location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location of the event.
     * @param location The new event's string location. Cannot be null.
     */
    public void setLocation(@NonNull String location) {
        this.location = location;
    }

    /**
     * Gets the start date and time of the event.
     * @return The event start timestamp.
     */
    public Timestamp getEventStartDateTime() {
        return eventStartDateTime;
    }

    /**
     * Sets the start date and time of the event.
     * @param eventStartDateTime The new event start timestamp. Cannot be null.
     */
    public void setEventStartDateTime(@NonNull Timestamp eventStartDateTime) {
        this.eventStartDateTime = eventStartDateTime;
    }

    /**
     * Gets the end date and time of the event.
     * @return The event end timestamp.
     */
    public Timestamp getEventEndDateTime() {
        return eventEndDateTime;
    }

    /**
     * Sets the end date and time of the event.
     * @param eventEndDateTime The new event end timestamp. Cannot be null.
     */
    public void setEventEndDateTime(@NonNull Timestamp eventEndDateTime) {
        this.eventEndDateTime = eventEndDateTime;
    }

    /**
     * Gets the maximum capacity of the event.
     * @return The event capacity.
     */
    public Integer getEventCapacity() {
        return eventCapacity;
    }

    /**
     * Sets the maximum capacity of the event.
     * @param eventCapacity The new event capacity. Cannot be null.
     */
    public void setEventCapacity(@NonNull Integer eventCapacity) {
        this.eventCapacity = eventCapacity;
    }

    /**
     * Gets the maximum capacity of the waitlist.
     * @return The waitlist capacity, or null if there is no limit.
     */
    public Integer getWaitlistCapacity() {
        return waitlistCapacity;
    }

    /**
     * Sets the maximum capacity of the waitlist.
     * @param waitlistCapacity The new waitlist capacity. Can be null for no limit.
     */
    public void setWaitlistCapacity(Integer waitlistCapacity) { this.waitlistCapacity = waitlistCapacity; }

    /**
     * Gets the registration start date and time.
     * @return The registration start timestamp.
     */
    public Timestamp getRegistrationStartDateTime() {
        return registrationStartDateTime;
    }

    /**
     * Sets the registration start date and time.
     * @param registrationStartDateTime The new registration start timestamp. Cannot be null.
     */
    public void setRegistrationStartDateTime(@NonNull Timestamp registrationStartDateTime) {
        this.registrationStartDateTime = registrationStartDateTime;
    }

    /**
     * Gets the registration end date and time.
     * @return The registration end timestamp.
     */
    public Timestamp getRegistrationEndDateTime() {
        return registrationEndDateTime;
    }

    /**
     * Sets the registration end date and time.
     * @param registrationEndDateTime The new registration end timestamp. Cannot be null.
     */
    public void setRegistrationEndDateTime(@NonNull Timestamp registrationEndDateTime) {
        this.registrationEndDateTime = registrationEndDateTime;
    }

    /**
     * Checks if the event is reoccurring.
     * @return {@code true} if the event is reoccurring, {@code false} otherwise.
     */
    public Boolean isReoccurring() { return isReoccurring; }

    /**
     * Sets whether the event is reoccurring.
     * @param reoccurring The new reoccurring status. Cannot be null.
     */
    public void setReoccurring(@NonNull Boolean reoccurring) { isReoccurring = reoccurring; }

    /**
     * Gets the end date and time for a reoccurring event.
     * @return The reoccurring end timestamp, or null if not reoccurring.
     */
    public Timestamp getReoccurringEndDateTime() { return reoccurringEndDateTime; }

    /**
     * Sets the end date and time for a reoccurring event.
     * @param reoccurringEndDateTime The new reoccurring end timestamp.
     */
    public void setReoccurringEndDateTime(@NonNull Timestamp reoccurringEndDateTime) {
        this.reoccurringEndDateTime = reoccurringEndDateTime;
    }

    /**
     * Gets the type of reoccurrence for the event.
     * @return The reoccurring type, or null if not reoccurring.
     */
    public ReoccurringType getReoccurringType() { return reoccurringType; }

    /**
     * Sets the type of reoccurrence for the event.
     * @param reoccurringType The new reoccurring type.
     */
    public void setReoccurringType(ReoccurringType reoccurringType) {
        this.reoccurringType = reoccurringType;
    }

    /**
     * Checks if geolocation tracking is required for the event.
     * @return {@code true} if geolocation tracking is required, {@code false} otherwise.
     */
    public Boolean isGeolocationTrackingRequired() {
        return requireGeolocationTracking;
    }

    /**
     * Sets whether geolocation tracking is required for the event.
     * @param requireGeolocationTracking The new geolocation tracking status. Cannot be null.
     */
    public void setRequireGeolocationTracking(@NonNull Boolean requireGeolocationTracking) {
        this.requireGeolocationTracking = requireGeolocationTracking;
    }

    public HashMap<String, GeoPoint> getGeolocationMap() {
        return geolocationMap;
    }

    public void setGeolocationMap(@NonNull HashMap<String, GeoPoint> geolocationMap) {
        this.geolocationMap = geolocationMap;
    }

    /**
     * Gets the event poster.
     * @return The Poster object, or null if there is no poster.
     */
    public Poster getPoster() { return poster; }

    /**
     * Sets the event poster.
     * @param poster The new Poster object.
     */
    public void setPoster(Poster poster) { this.poster = poster; }

    /**
     * Sets the event poster.
     * @return The qrHashCode.
     */
    public String getQrCodeHash() {return qrCodeHash; }

    /**
     * Sets the event poster.
     * @param qrCodeHash The new qrCodeHash.
     */
    public void setQrCodeHash(String qrCodeHash) {this.qrCodeHash = qrCodeHash; }

    /**
     * Gets the list of device IDs on the waitlist.
     * @return A list of strings representing device IDs.
     */
    public List<String> getWaitList() { return waitList; }

    /**
     * Sets the list of device IDs on the waitlist.
     * @param waitList The new waitlist.
     */
    public void setWaitList(@NonNull List<String> waitList) { this.waitList = waitList; }

    /**
     * Gets the list of device IDs on the pending list.
     * @return A list of strings representing device IDs.
     */
    public List<String> getPendingList() { return pendingList; }

    /**
     * Sets the list of device IDs on the pending list.
     * @param pendingList The new pending list.
     */
    public void setPendingList(@NonNull List<String> pendingList) { this.pendingList = pendingList; }

    /**
     * Gets the list of device IDs on the accepted (attending) list.
     * @return A list of strings representing device IDs.
     */
    public List<String> getAcceptedList() { return acceptedList; }

    /**
     * Sets the list of device IDs on the accepted (attending) list.
     * @param acceptedList The new accepted list.
     */
    public void setAcceptedList(@NonNull List<String> acceptedList) { this.acceptedList = acceptedList; }

    /**
     * Gets the list of device IDs on the declined list.
     * @return A list of strings representing device IDs.
     */
    public List<String> getDeclinedList() { return declinedList; }

    /**
     * Sets the list of device IDs on the declined list.
     * @param declinedList The new declined list.
     */
    public void setDeclinedList(@NonNull List<String> declinedList) { this.declinedList = declinedList; }

    /**
     * Checks if the registration opened flag is set.
     * @return The registration opened status.
     */
    public boolean isRegistrationOpened() { return registrationOpened; }

    /**
     * Sets the registration opened flag.
     * @param registrationOpened The new registration opened status.
     */
    public void setRegistrationOpened(boolean registrationOpened) { this.registrationOpened = registrationOpened; }

    /**
     * Checks if the lottery processed flag is set.
     * @return The lottery processed status.
     */
    public boolean isLotteryProcessed() { return lotteryProcessed; }

    /**
     * Sets the lottery processed flag.
     * @param lotteryProcessed The new lottery processed status.
     */
    public void setLotteryProcessed(boolean lotteryProcessed) { this.lotteryProcessed = lotteryProcessed; }

    /**
     * Checks if the pending expired flag is set.
     * @return The pending expired status.
     */
    public boolean isPendingExpired() { return pendingExpired; }

    /**
     * Sets the pending expired flag.
     * @param pendingExpired The new pending expired status.
     */
    public void setPendingExpired(boolean pendingExpired) { this.pendingExpired = pendingExpired; }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        // manually write the Timestamp fields as milliseconds (long)
        out.writeLong(eventStartDateTime.toDate().getTime());
        out.writeLong(eventEndDateTime.toDate().getTime());
        out.writeLong(registrationStartDateTime.toDate().getTime());
        out.writeLong(registrationEndDateTime.toDate().getTime());

        // reoccurring end date can be null
        out.writeLong(reoccurringEndDateTime != null ? reoccurringEndDateTime.toDate().getTime() : -1L);

        // manually write the GeolocationMap
        // GeoPoint is NOT serializable, so we must decompose it.
        out.writeInt(geolocationMap.size());
        for (Map.Entry<String, GeoPoint> entry : geolocationMap.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeDouble(entry.getValue().getLatitude());
            out.writeDouble(entry.getValue().getLongitude());
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        // manually read the Timestamp fields and convert back to Firebase Timestamp
        long start = in.readLong();
        eventStartDateTime = new Timestamp(new java.util.Date(start));
        long end = in.readLong();
        eventEndDateTime = new Timestamp(new java.util.Date(end));
        long regStart = in.readLong();
        registrationStartDateTime = new Timestamp(new java.util.Date(regStart));
        long regEnd = in.readLong();
        registrationEndDateTime = new Timestamp(new java.util.Date(regEnd));

        // reoccurring end date can be null
        long reoccurringEnd = in.readLong();
        reoccurringEndDateTime = reoccurringEnd != -1L ? new Timestamp(new java.util.Date(reoccurringEnd)) : null;

        // manually read the GeolocationMap
        int mapSize = in.readInt();
        geolocationMap = new HashMap<>();
        for (int i = 0; i < mapSize; i++) {
            String key = in.readUTF();
            double lat = in.readDouble();
            double lon = in.readDouble();
            geolocationMap.put(key, new GeoPoint(lat, lon));
        }
    }
}