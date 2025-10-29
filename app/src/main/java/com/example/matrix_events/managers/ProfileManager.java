package com.example.matrix_events.managers;

import android.util.Log;

import com.example.matrix_events.database.DBConnector;
import com.example.matrix_events.database.DBListener;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.mvc.Model;

import java.util.ArrayList;
import java.util.List;

public class ProfileManager extends Model implements DBListener<Profile> {
    private static final String TAG = "ProfileManager";

    private List<Profile> profiles = new ArrayList<>();
    private final DBConnector<Profile> connector = new DBConnector<Profile>("profiles", this, Profile.class);

    // Singleton
    private static ProfileManager manager = new ProfileManager();
    public static ProfileManager getInstance() {
        return manager;
    }

    // Profile getters
    public Profile getProfile(String id) {
        for (Profile profile : profiles) {
            if (profile.getId().equals(id)) {
                return profile;
            }
        }
        return null;
    }
    public List<Profile> getProfiles() {
        return profiles;
    }

    // Create, update, delete operations for organizers and admins
    public void createProfile(Profile profile) { connector.createAsync(profile); }
    public void updateProfile(Profile profile) {
        connector.updateAsync(profile);
    }
    public void deleteProfile(Profile profile) {
        connector.deleteAsync(profile);
    }

    @Override
    public void readAllAsync_Complete(List<Profile> objects) {
        Log.d(TAG, "ProfileManager read all complete, notifying views");
        profiles = objects;
        for (Profile p : objects) {
            Log.d(TAG, p.getId());
        }
        // Notify views of profile changes
        notifyViews();
    }
}