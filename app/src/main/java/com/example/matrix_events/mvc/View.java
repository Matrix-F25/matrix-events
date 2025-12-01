package com.example.matrix_events.mvc;

/**
 * An interface representing the "View" component in a Model-View-Controller (MVC) architecture.
 * <p>
 * Classes that implement this interface (typically Android {@link android.app.Activity}s or
 * {@link androidx.fragment.app.Fragment}s) are responsible for rendering the user interface.
 * </p>
 * <p>
 * This interface acts as the <b>Observer</b> in the Observer design pattern. Views register
 * themselves with a {@link Model} (the Subject) and are notified via the {@link #update()}
 * method whenever the underlying data changes.
 * </p>
 */
public interface View {

    /**
     * This method is called by the observed {@link Model} when its data has changed.
     * <p>
     * Implementations of this method should:
     * <ol>
     * <li>Query the model for the latest state (e.g., {@code eventManager.getEvents()}).</li>
     * <li>Refresh the UI elements (e.g., {@code adapter.notifyDataSetChanged()}) to reflect the changes.</li>
     * </ol>
     * </p>
     * <p>
     * <b>Thread Safety Note:</b> If the Model updates from a background thread (like a network callback),
     * this method might be invoked on that background thread. Android UI updates <b>must</b> happen
     * on the main thread. Implementers should ensure code execution is moved to the UI thread
     * (e.g., using {@code runOnUiThread()}) if modifying UI widgets.
     * </p>
     */
    void update();
}