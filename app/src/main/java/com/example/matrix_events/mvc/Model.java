package com.example.matrix_events.mvc;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract base class for the "Model" component in a Model-View-Controller (MVC) architecture.
 * <p>
 * This class implements the <b>Observer Pattern</b> (specifically the "Subject" role).
 * It provides the fundamental functionality for data models to manage a list of observers
 * ({@link View}s) and notify them of any changes to the model's state.
 * </p>
 * <p>
 * Concrete subclasses of {@code Model} should represent the actual data and business logic
 * of the application (e.g., a specific UserProfile or EventList) and call {@link #notifyViews()}
 * whenever that data is modified.
 * </p>
 */
public abstract class Model {
    private List<View> views = new ArrayList<>();

    /**
     * Registers a {@link View} to be notified of changes to this model.
     * <p>
     * The added view will have its {@code update()} method called whenever
     * {@link #notifyViews()} is invoked.
     * </p>
     *
     * @param v The {@link View} to be added as an observer. Cannot be null.
     */
    public void addView(@NonNull View v) {
        views.add(v);
    }

    /**
     * Unregisters a {@link View}, so it will no longer receive updates from this model.
     *
     * @param v The {@link View} to be removed from the list of observers. Cannot be null.
     */
    public void removeView(@NonNull View v) {
        views.remove(v);
    }

    /**
     * Notifies all registered {@link View} objects that the model's state has changed.
     * <p>
     * This method iterates through the list of observers and calls each one's {@code update()} method.
     * Subclasses must call this method strictly after their state has been updated to ensure
     * the UI reflects the most current data.
     * </p>
     */
    public void notifyViews() {
        for (View v : views) {
            v.update();
        }
    }
}