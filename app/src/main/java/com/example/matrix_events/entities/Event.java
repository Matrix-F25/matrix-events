package com.example.matrix_events.entities;

import androidx.annotation.NonNull;

import com.example.matrix_events.database.DBObject;
import com.example.matrix_events.managers.NotificationManager;
import com.example.matrix_events.managers.ProfileManager;
import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Event extends DBObject implements Serializable {
    private String name;
    private String description;
    private Profile organizer;
    private Geolocation location;
    private Timestamp eventStartDateTime;
    private Timestamp eventEndDateTime;
    private Integer eventCapacity;
    private Integer waitlistCapacity;               // can be null if no limit
    private Timestamp registrationStartDateTime;
    private Timestamp registrationEndDateTime;
    private Boolean isReoccurring = false;
    private Timestamp reoccurringEndDateTime;       // can be null if not reoccurring
    private ReoccurringType reoccurringType;        // can be null if not reoccurring
    private Boolean geolocationTrackingEnabled = true;
    private Poster poster;                          // can be null if no poster
    private List<String> waitList = new ArrayList<>();      // holds profile deviceId
    private List<String> pendingList = new ArrayList<>();
    private List<String> acceptedList = new ArrayList<>();
    private List<String> declinedList = new ArrayList<>();
    private boolean registrationOpened = false;
    private boolean lotteryProcessed = false;
    private boolean pendingExpired = false;

    public Event() {}       // Required for Firestore

    public Event(@NonNull String name, @NonNull String description, @NonNull Profile organizer,
                 @NonNull Geolocation location, @NonNull Timestamp eventStartDateTime, @NonNull Timestamp eventEndDateTime,
                 @NonNull Integer eventCapacity, Integer waitlistCapacity,
                 @NonNull Timestamp registrationStartDateTime, @NonNull Timestamp registrationEndDateTime,
                 @NonNull Boolean isReoccurring, Timestamp reoccurringEndDateTime, ReoccurringType reoccurringType,
                 @NonNull Boolean geolocationTrackingEnabled, Poster poster) {
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
        this.geolocationTrackingEnabled = geolocationTrackingEnabled;

        // Optional fields (nullable)
        this.waitlistCapacity = waitlistCapacity;              // null: no waitlist limit
        this.reoccurringEndDateTime = reoccurringEndDateTime;  // null: not reoccurring
        this.reoccurringType = reoccurringType;                // null: not reoccurring
        this.poster = poster;                                  // null: no poster
    }

    // Event date and time checks

    public boolean isBeforeRegistrationStart() {
        Timestamp now = Timestamp.now();
        // Check if registration is yet to open
        return registrationStartDateTime.compareTo(now) > 0;
    }

    public boolean isRegistrationOpen() {
        Timestamp now = Timestamp.now();
        // Check if registration is currently open
        return registrationStartDateTime.compareTo(now) <= 0 && registrationEndDateTime.compareTo(now) >= 0;
    }

    public boolean isRegistrationClosed() {
        Timestamp now = Timestamp.now();
        // Check if registration is closed
        return registrationEndDateTime.compareTo(now) < 0;
    }

    public boolean isBeforeEventStart() {
        Timestamp now = Timestamp.now();
        // Check if event is yet to start
        return eventStartDateTime.compareTo(now) > 0;
    }

    public boolean isEventOngoing() {
        Timestamp now = Timestamp.now();
        // Check if event is currently going on
        return eventStartDateTime.compareTo(now) <= 0 && eventEndDateTime.compareTo(now) >= 0;
    }

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

    public boolean inWaitList(String deviceId) {
        return waitList.contains(deviceId);
    }
    public void joinWaitList(String deviceID) {
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
    }
    public boolean leaveWaitlist(String deviceID) {
        return waitList.remove(deviceID);
    }

    public boolean inPendingList(String deviceId) {
        return pendingList.contains(deviceId);
    }

    public boolean inAcceptedList(String deviceId) {
        return acceptedList.contains(deviceId);
    }
    public void joinAcceptedList(String deviceId) {
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

    public boolean inDeclinedList(String deviceId) {
        return declinedList.contains(deviceId);
    }
    public void joinDeclinedList(String deviceId) {
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
            String message = "It's your lucky day! You have been "
                + "second chance selected for the " + name + " "
                + "event. Please accept or decline the invitation "
                + "at your earliest convenience.\n\n"
                + "This is an automated message.";
            Timestamp now = Timestamp.now();
            Notification notification = new Notification(sender, receiver, message, now);
            NotificationManager.getInstance().createNotification(notification);
        }
    }

    // Event getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Profile getOrganizer() {
        return organizer;
    }

    public void setOrganizer(Profile organizer) {
        this.organizer = organizer;
    }

    public Geolocation getLocation() {
        return location;
    }

    public void setLocation(Geolocation location) {
        this.location = location;
    }

    public Timestamp getEventStartDateTime() {
        return eventStartDateTime;
    }

    public void setEventStartDateTime(Timestamp eventStartDateTime) {
        this.eventStartDateTime = eventStartDateTime;
    }

    public Timestamp getEventEndDateTime() {
        return eventEndDateTime;
    }

    public void setEventEndDateTime(Timestamp eventEndDateTime) {
        this.eventEndDateTime = eventEndDateTime;
    }

    public Integer getEventCapacity() {
        return eventCapacity;
    }

    public void setEventCapacity(Integer eventCapacity) {
        this.eventCapacity = eventCapacity;
    }

    public Integer getWaitlistCapacity() {
        return waitlistCapacity;
    }

    public void setWaitlistCapacity(Integer waitlistCapacity) { this.waitlistCapacity = waitlistCapacity; }

    public Timestamp getRegistrationStartDateTime() {
        return registrationStartDateTime;
    }

    public void setRegistrationStartDateTime(Timestamp registrationStartDateTime) {
        this.registrationStartDateTime = registrationStartDateTime;
    }

    public Timestamp getRegistrationEndDateTime() {
        return registrationEndDateTime;
    }

    public void setRegistrationEndDateTime(Timestamp registrationEndDateTime) {
        this.registrationEndDateTime = registrationEndDateTime;
    }

    public Boolean isReoccurring() { return isReoccurring; }

    public void setReoccurring(Boolean reoccurring) { isReoccurring = reoccurring; }

    public Timestamp getReoccurringEndDateTime() { return reoccurringEndDateTime; }

    public void setReoccurringEndDateTime(Timestamp reoccurringEndDateTime) {
        this.reoccurringEndDateTime = reoccurringEndDateTime;
    }

    public ReoccurringType getReoccurringType() { return reoccurringType; }

    public void setReoccurringType(ReoccurringType reoccurringType) {
        this.reoccurringType = reoccurringType;
    }

    public Boolean isGeolocationTrackingEnabled() {
        return geolocationTrackingEnabled;
    }

    public void setGeolocationTrackingEnabled(Boolean geolocationTrackingEnabled) {
        this.geolocationTrackingEnabled = geolocationTrackingEnabled;
    }

    public Poster getPoster() { return poster; }

    public void setPoster(Poster poster) { this.poster = poster; }

    public List<String> getWaitList() { return waitList; }

    public void setWaitList(List<String> waitList) { this.waitList = waitList; }

    public List<String> getPendingList() { return pendingList; }

    public void setPendingList(List<String> pendingList) { this.pendingList = pendingList; }

    public List<String> getAcceptedList() { return acceptedList; }

    public void setAcceptedList(List<String> acceptedList) { this.acceptedList = acceptedList; }

    public List<String> getDeclinedList() { return declinedList; }

    public void setDeclinedList(List<String> declinedList) { this.declinedList = declinedList; }

    public boolean isRegistrationOpened() { return registrationOpened; }

    public void setRegistrationOpened(boolean registrationOpened) { this.registrationOpened = registrationOpened; }

    public boolean isLotteryProcessed() { return lotteryProcessed; }

    public void setLotteryProcessed(boolean lotteryProcessed) { this.lotteryProcessed = lotteryProcessed; }

    public boolean isPendingExpired() { return pendingExpired; }

    public void setPendingExpired(boolean pendingExpired) { this.pendingExpired = pendingExpired; }
}
