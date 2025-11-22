package com.example.matrix_events.entities;

/**
 * An enumeration representing the frequency of a reoccurring event.
 * This is used to specify whether an event repeats daily, weekly, or monthly.
 */
public enum ReoccurringType {
    /**
     * Represents an event that reoccurs every day.
     */
    Daily,

    /**
     * Represents an event that reoccurs every week on the same day.
     */
    Weekly,

    /**
     * Represents an event that reoccurs every month on the same date.
     */
    Monthly
}