package com.example.matrix_events.mvc;

/**
 * An interface representing the "View" component in a Model-View-Controller (MVC) architecture.
 * Classes that implement this interface are typically responsible for rendering the user interface
 * and displaying data from a {@link Model}. They act as observers to a model and are updated
 * whenever the model's state changes.
 */
public interface View {

    /**
     * This method is called by a {@link Model} when its data has changed.
     * Implementations of this method should query the model for the updated state
     * and refresh the UI to reflect the changes.
     */
    void update();
}