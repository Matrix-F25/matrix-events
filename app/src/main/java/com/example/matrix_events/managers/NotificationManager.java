package com.example.matrix_events.managers;

import android.util.Log;

import com.example.matrix_events.database.DBConnector;
import com.example.matrix_events.database.DBListener;
import com.example.matrix_events.entities.Notification;
import com.example.matrix_events.mvc.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all notification-related data and operations within the application.
 * This class follows the singleton pattern to provide a centralized point of access
 * for notification data. It connects to the Firestore 'notifications' collection,
 * maintains a local cache of {@link Notification} objects, and notifies registered
 * views of any changes, adhering to the MVC pattern.
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
     *
     * @param id The Firestore document ID of the notification.
     * @return The {@link Notification} object with the matching ID, or {@code null} if no notification is found.
     */
    public Notification getNotificationByDBID(String id) {
        for (Notification notification : notifications) {
            if (notification.getId().equals(id)) {
                return notification;
            }
        }
        return null;
    }

    /**
     * Filters and returns a list of notifications received by a specific user.
     *
     * @param deviceID The device ID of the user whose received notifications are to be retrieved.
     * @return A list of {@link Notification} objects addressed to the specified user.
     */
    public List<Notification> getReceivedNotificationsByDeviceID(String deviceID) {
        List<Notification> recvNotifications = new ArrayList<>();
        for (Notification notification : notifications) {
            if (notification.getReceiver().getDeviceId().equals(deviceID)) {
                recvNotifications.add(notification);
            }
        }
        return recvNotifications;
    }

    /**
     * Filters and returns a list of notifications sent by a specific user.
     *
     * @param deviceID The device ID of the user whose sent notifications are to be retrieved.
     * @return A list of {@link Notification} objects sent by the specified user.
     */
    public List<Notification> getSentNotificationsByDeviceID(String deviceID) {
        List<Notification> sentNotifications = new ArrayList<>();
        for (Notification notification : notifications) {
            if (notification.getSender().getDeviceId().equals(deviceID)) {
                sentNotifications.add(notification);
            }
        }
        return sentNotifications;
    }

    // Create, update, delete operations

    /**
     * Asynchronously creates a new notification in the Firestore database.
     *
     * @param notification The {@link Notification} object to create.
     */
    public void createNotification(Notification notification) { connector.createAsync(notification); }

    /**
     * Asynchronously updates an existing notification in the Firestore database.
     *
     * @param notification The {@link Notification} object with updated data. Its ID must be set.
     */
    public void updateNotification(Notification notification) { connector.updateAsync(notification); }

    /**
     * Asynchronously deletes a notification from the Firestore database.
     *
     * @param notification The {@link Notification} object to delete. Its ID must be set.
     */
    public void deleteNotification(Notification notification) { connector.deleteAsync(notification); }

    /**
     * Callback method invoked by {@link DBConnector} when the notification data changes in Firestore.
     * It updates the local notification cache and notifies all registered views of the change.
     *
     * @param objects The updated list of {@link Notification} objects from Firestore.
     */
    @Override
    public void readAllAsync_Complete(List<Notification> objects) {
        Log.d(TAG, "NotificationManager read all complete, notifying views");

        // Patch old notifications with missing types
        for (Notification n : objects) {
            if (n.getTypeRaw() == null) {
                Log.d(TAG, "Missing type for notification " + n.getId() + " - assigning default ORGANIZER");
                n.setTypeEnum(Notification.NotificationType.ORGANIZER); // default
                connector.updateAsync(n);
            }
        }

        notifications = objects;
        // Notify views of notification changes
        notifyViews();
    }
}