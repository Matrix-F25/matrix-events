package com.example.matrix_events.managers;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.matrix_events.database.DBConnector;
import com.example.matrix_events.database.DBListener;
import com.example.matrix_events.entities.Notification;
import com.example.matrix_events.mvc.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all notification-related data and operations within the application.
 * <p>
 * This class follows the <b>Singleton Pattern</b> to provide a centralized point of access
 * for notification data across the application. It acts as the bridge between the
 * Firestore database and the UI.
 * </p>
 * <p>
 * It extends {@link Model} to integrate with the MVC architecture, allowing Views to
 * subscribe to updates. It implements {@link DBListener} to receive real-time updates
 * from the Firestore 'notifications' collection.
 * </p>
 */
public class NotificationManager extends Model implements DBListener<Notification> {
    private static final String TAG = "NotificationManager";
    private List<Notification> notifications = new ArrayList<>();
    private final DBConnector<Notification> connector = new DBConnector<>("notifications", this, Notification.class);

    // Singleton
    private static final NotificationManager manager = new NotificationManager();

    /**
     * Gets the singleton instance of the NotificationManager.
     *
     * @return The single, static instance of NotificationManager.
     */
    public static NotificationManager getInstance() {
        return manager;
    }

    // Notification getters

    /**
     * Retrieves the local cache of all notifications.
     *
     * @return A list of all {@link Notification} objects currently held by the manager.
     */
    public List<Notification> getNotifications() { return notifications; }

    /**
     * Finds and retrieves a notification by its unique Firestore document ID.
     * <p>
     * This method is critical for the Push Notification deep linking feature.
     * </p>
     *
     * @param id The Firestore document ID of the notification. Cannot be null.
     * @return The {@link Notification} object with the matching ID, or {@code null} if no notification is found.
     */
    @Nullable
    public Notification getNotificationByDBID(@NonNull String id) {
        for (Notification notification : notifications) {
            if (notification.getId() != null && notification.getId().equals(id)) {
                return notification;
            }
        }
        return null;
    }

    /**
     * Filters and returns a list of notifications received by a specific user.
     *
     * @param deviceID The device ID of the user whose received notifications are to be retrieved. Cannot be null.
     * @return A list of {@link Notification} objects addressed to the specified user.
     * Returns an empty list if no matches are found.
     */
    @NonNull
    public List<Notification> getReceivedNotificationsByDeviceID(@NonNull String deviceID) {
        List<Notification> recvNotifications = new ArrayList<>();
        for (Notification notification : notifications) {
            // Check for null receiver to prevent crashes if data is malformed
            if (notification.getReceiver() != null && deviceID.equals(notification.getReceiver().getDeviceId())) {
                recvNotifications.add(notification);
            }
        }
        return recvNotifications;
    }

    /**
     * Filters and returns a list of notifications sent by a specific user.
     *
     * @param deviceID The device ID of the user whose sent notifications are to be retrieved. Cannot be null.
     * @return A list of {@link Notification} objects sent by the specified user.
     * Returns an empty list if no matches are found.
     */
    @NonNull
    public List<Notification> getSentNotificationsByDeviceID(@NonNull String deviceID) {
        List<Notification> sentNotifications = new ArrayList<>();
        for (Notification notification : notifications) {
            // Check for null sender to prevent crashes if data is malformed
            if (notification.getSender() != null && deviceID.equals(notification.getSender().getDeviceId())) {
                sentNotifications.add(notification);
            }
        }
        return sentNotifications;
    }

    // Create, update, delete operations

    /**
     * Asynchronously creates a new notification in the Firestore database.
     *
     * @param notification The {@link Notification} object to create. Cannot be null.
     */
    public void createNotification(@NonNull Notification notification) { connector.createAsync(notification); }

    /**
     * Asynchronously updates an existing notification in the Firestore database.
     * <p>
     * This is used by Entrants to mark a notification as read (soft delete).
     * </p>
     *
     * @param notification The {@link Notification} object with updated data. Its ID must be set. Cannot be null.
     */
    public void updateNotification(@NonNull Notification notification) { connector.updateAsync(notification); }

    /**
     * Asynchronously deletes a notification from the Firestore database.
     * <p>
     * This is typically used by Admins or system processes for hard deletion.
     * </p>
     *
     * @param notification The {@link Notification} object to delete. Its ID must be set. Cannot be null.
     */
    public void deleteNotification(@NonNull Notification notification) { connector.deleteAsync(notification); }

    /**
     * Callback method invoked by {@link DBConnector} when the notification data changes in Firestore.
     * <p>
     * This method updates the local cache with the fresh list of notifications and
     * immediately invokes {@link #notifyViews()} to trigger a UI refresh for all
     * observing Views.
     * </p>
     *
     * @param objects The updated list of {@link Notification} objects from Firestore.
     */
    @Override
    public void readAllAsync_Complete(@NonNull List<Notification> objects) {
        Log.d(TAG, "NotificationManager read all complete, notifying views");

        // Patch old notifications with missing types
        for (Notification n : objects) {
            // Check if type is missing (getTypeRaw returns the raw field value)
            if (n.getTypeRaw() == null) {
                Log.d(TAG, "Missing type for notification " + n.getId() + " - assigning default ORGANIZER");
                n.setType(Notification.NotificationType.ORGANIZER); // default
                connector.updateAsync(n); // Asynchronously save the patched object back
            }
        }

        notifications = objects;
        // Notify views of notification changes
        notifyViews();
    }
}
