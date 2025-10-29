package com.example.matrix_events.managers;

import android.util.Log;

import com.example.matrix_events.database.DBConnector;
import com.example.matrix_events.database.DBListener;
import com.example.matrix_events.entities.Profile;

import java.util.ArrayList;
import java.util.List;

public class ProfileManager implements DBListener<Profile> {
    private static final String TAG = "ProfileManager";

    private List<Profile> profiles;
    private final DBConnector<Profile> connector;
    private static NotificationManager manager = new NotificationManager();
    private ProfileManager() {
        profiles = new ArrayList<>();
        connector = new DBConnector<>("profiles", this, Profile.class);
    }
    public static NotificationManager getInstance() {
        return manager;
    }

    public void createProfile() {
        Profile p = new Profile("Connor's (the cool one) Profile", "Wicked cool description");
        connector.createAsync(p);
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    @Override
    public void createAsync_Complete(Profile object) {
        profiles.add(object);
        // TODO notify views
    }

    @Override
    public void readAllAsync_Complete(List<Profile> objects) {
        profiles = objects;
        Log.d(TAG, "read async complete");
        for (Profile p : objects) {
            Log.d(TAG, p.getId());
        }
        // TODO notify views
    }

    @Override
    public void deleteAsync_Complete(Profile object) {
        profiles.remove(object);
        // TODO notify views
    }

    @Override
    public void database_Changed() {
        connector.readAllAsync();
    }
}
