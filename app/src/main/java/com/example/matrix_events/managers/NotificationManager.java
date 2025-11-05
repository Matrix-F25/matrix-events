package com.example.matrix_events.managers;

import android.util.Log;

import com.example.matrix_events.database.DBConnector;
import com.example.matrix_events.database.DBListener;
import com.example.matrix_events.entities.Notification;
import com.example.matrix_events.mvc.Model;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager extends Model implements DBListener<Notification> {
    private static final String TAG = "NotificationManager";

    private List<Notification> notifications = new ArrayList<>();
    private final DBConnector<Notification> connector = new DBConnector<Notification>("notifications", this, Notification.class);

    // Singleton
    private static NotificationManager manager = new NotificationManager();
    public static NotificationManager getInstance() {
        return manager;
    }

    // Notification getters
    public List<Notification> getAllNotifications() { return notifications; };

    public Notification getNotificationByID(String id) {
        for (Notification notification : notifications) {
            if (notification.getId().equals(id)) {
                return notification;
            }
        }
        return null;
    }

    public List<Notification> getProfileNotifications(String deviceID) {
        List<Notification> profileNotifications = new ArrayList<>();
        for (Notification notification : notifications) {
            if (notification.getReceiver().getDeviceId().equals(deviceID)) {
                profileNotifications.add(notification);
            }
        }
        return profileNotifications;
    }

    // Create, update, delete operations for organizers and admins
    public void createNotification(Notification notification) { connector.createAsync(notification); }
    public void updateNotification(Notification notification) { connector.updateAsync(notification); }
    public void deleteNotification(Notification notification) { connector.deleteAsync(notification); }

    @Override
    public void readAllAsync_Complete(List<Notification> objects) {
        notifications = objects;
        // Notify views of notification changes
        notifyViews();
    }
}