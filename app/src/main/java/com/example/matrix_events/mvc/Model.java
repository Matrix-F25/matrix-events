package com.example.matrix_events.mvc;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract base class for the "Model" component in a Model-View-Controller (MVC) architecture.
 * This class provides the fundamental functionality for models to manage a list of observers (Views)
 * and notify them of any changes to the model's state. Subclasses of {@code Model} will
 * represent the actual data and business logic of the application.
 */
public abstract class Model {
    private List<View> views = new ArrayList<>();

    /**
     * Registers a {@link View} to be notified of changes to this model.
     * The added view will have its {@code update()} method called whenever {@code notifyViews()} is invoked.
     *
     * @param v The {@link View} to be added as an observer.
     */
    public void addView(View v) {
        views.add(v);
    }

    /**
     * Unregisters a {@link View}, so it will no longer receive updates from this model.
     *
     * @param v The {@link View} to be removed from the list of observers.
     */
    public void removeView(View v) {
        views.remove(v);
    }

    /**
     * Notifies all registered {@link View} objects that the model's state has changed.
     * This method iterates through the list of observers and calls each one's {@code update()} method.
     * Subclasses should call this method whenever their data changes to ensure the UI is refreshed.
     */
    public void notifyViews() {
        for (View v : views) {
            v.update();
        }
    }
}