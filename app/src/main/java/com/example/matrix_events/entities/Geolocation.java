package com.example.matrix_events.entities;

import java.io.Serializable;

public class Geolocation implements Serializable {
    private String name;
    private double latitude;
    private double longitude;

    public Geolocation() {}     // Required for Firestore

    public Geolocation(String name, double longitude, double latitude) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    };

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}