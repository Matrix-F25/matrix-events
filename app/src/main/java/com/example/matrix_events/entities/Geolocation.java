package com.example.matrix_events.entities;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Represents a geographical location with a name, latitude, and longitude.
 * This class is used to store location data for events.
 * It implements {@link Serializable} to be passable between Android components.
 */
public class Geolocation implements Serializable {
    private String name;
    private double latitude;
    private double longitude;

    /**
     * Default constructor required for Firestore data mapping.
     */
    public Geolocation() {}

    /**
     * Constructs a new Geolocation object.
     *
     * @param name      The descriptive name of the location (e.g., "Main Hall"). Cannot be null.
     * @param longitude The longitude coordinate of the location.
     * @param latitude  The latitude coordinate of the location.
     */
    public Geolocation(@NonNull String name, double longitude, double latitude) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    };

    /**
     * Gets the name of the location.
     *
     * @return The location name string.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the location.
     *
     * @param name The new location name string.
     */
    public void setName(@NonNull String name) {
        this.name = name;
    }

    /**
     * Gets the latitude of the location.
     *
     * @return The latitude coordinate as a double.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude of the location.
     *
     * @param latitude The new latitude coordinate as a double.
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets the longitude of the location.
     *
     * @return The longitude coordinate as a double.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude of the location.
     *
     * @param longitude The new longitude coordinate as a double.
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}