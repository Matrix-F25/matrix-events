package com.example.matrix_events.entities;

import com.example.matrix_events.database.DBObject;
import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Event extends DBObject implements Serializable {
    private String name;
    private String description;
    private Profile organizer;
    private Geolocation location;
    private Timestamp eventStartDateTime;
    private Timestamp eventEndDateTime;
    private int eventCapacity;
    private int waitlistCapacity;
    private Timestamp registrationStartDateTime;
    private Timestamp registrationEndDateTime;
    private boolean isReoccurring;
    private Timestamp reoccurringEndDateTime;       // can be null if not reoccurring
    private ReoccurringType reoccurringType;        // can be null if not reoccurring
    private boolean geolocationTrackingEnabled;
    private Poster poster;

    public Event() {}       // Required for Firestore

    public Event(String name, String description, Profile organizer, Geolocation location,
                 Timestamp eventStartDateTime, Timestamp eventEndDateTime, int eventCapacity,
                 int waitlistCapacity, Timestamp registrationStartDateTime,
                 Timestamp registrationEndDateTime, boolean isReoccurring,
                 Timestamp reoccurringEndDateTime, ReoccurringType reoccurringType,
                 boolean geolocationTrackingEnabled, Poster poster) {
        this.name = name;
        this.description = description;
        this.organizer = organizer;
        this.location = location;
        this.eventStartDateTime = eventStartDateTime;
        this.eventEndDateTime = eventEndDateTime;
        this.eventCapacity = eventCapacity;
        this.waitlistCapacity = waitlistCapacity;
        this.registrationStartDateTime = registrationStartDateTime;
        this.registrationEndDateTime = registrationEndDateTime;
        this.isReoccurring = isReoccurring;
        this.reoccurringEndDateTime = reoccurringEndDateTime;
        this.reoccurringType = reoccurringType;
        this.geolocationTrackingEnabled = geolocationTrackingEnabled;
        this.poster = poster;
    }

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

    public int getEventCapacity() {
        return eventCapacity;
    }

    public void setEventCapacity(int eventCapacity) {
        this.eventCapacity = eventCapacity;
    }

    public int getWaitlistCapacity() {
        return waitlistCapacity;
    }

    public void setWaitlistCapacity(int waitlistCapacity) {
        this.waitlistCapacity = waitlistCapacity;
    }

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

    public boolean isReoccurring() {
        return isReoccurring;
    }

    public void setReoccurring(boolean reoccurring) {
        isReoccurring = reoccurring;
    }

    public Timestamp getReoccurringEndDateTime() {
        return reoccurringEndDateTime;
    }

    public void setReoccurringEndDateTime(Timestamp reoccurringEndDateTime) {
        this.reoccurringEndDateTime = reoccurringEndDateTime;
    }

    public ReoccurringType getReoccurringType() {
        return reoccurringType;
    }

    public void setReoccurringType(ReoccurringType reoccurringType) {
        this.reoccurringType = reoccurringType;
    }

    public boolean isGeolocationTrackingEnabled() {
        return geolocationTrackingEnabled;
    }

    public void setGeolocationTrackingEnabled(boolean geolocationTrackingEnabled) {
        this.geolocationTrackingEnabled = geolocationTrackingEnabled;
    }

    public Poster getPoster() {
        return poster;
    }

    public void setPoster(Poster poster) {
        this.poster = poster;
    }
}
