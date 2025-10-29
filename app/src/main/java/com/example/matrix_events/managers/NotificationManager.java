package com.example.matrix_events.managers;

import android.util.Log;

import com.example.matrix_events.database.DBConnector;
import com.example.matrix_events.database.DBListener;
import com.example.matrix_events.entities.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager implements DBListener<Notification> {
    private static final String TAG = "NotificationManager";

    private List<Notification> notifications;
    private final DBConnector<Notification> connector;
    private static NotificationManager manager = new NotificationManager();
    private NotificationManager() {
        notifications = new ArrayList<>();
        connector = new DBConnector<>("notifications", this, Notification.class);
    }
    public static NotificationManager getInstance() {
        return manager;
    }

    public void createNotification() {
        Notification n = new Notification("Connor's (the cool one) Notification", "Wicked cool description");
        connector.createAsync(n);
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    @Override
    public void createAsync_Complete(Notification object) {
        notifications.add(object);
        // TODO notify views
    }

    @Override
    public void readAllAsync_Complete(List<Notification> objects) {
        notifications = objects;
        Log.d(TAG, "read async complete");
        for (Notification n : objects) {
            Log.d(TAG, n.getId());
        }
        // TODO notify views
    }

    @Override
    public void deleteAsync_Complete(Notification object) {
        notifications.remove(object);
        // TODO notify views
    }

    @Override
    public void database_Changed() {
        connector.readAllAsync();
    }
}
