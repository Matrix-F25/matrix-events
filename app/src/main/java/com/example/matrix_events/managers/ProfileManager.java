package com.example.matrix_events.managers;

import android.util.Log;

import com.example.matrix_events.database.DBConnector;
import com.example.matrix_events.database.DBListener;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.mvc.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all user profile data and operations within the application.
 * This class follows the singleton pattern to provide a single point of access to profile data.
 * It connects to the Firestore 'profiles' collection, maintains a local cache of {@link Profile} objects,
 * and, as a {@link Model}, notifies registered views of any data changes.
 */
public class ProfileManager extends Model implements DBListener<Profile> {
    private static final String TAG = "ProfileManager";

    private List<Profile> profiles = new ArrayList<>();
    private final DBConnector<Profile> connector = new DBConnector<Profile>("profiles", this, Profile.class);

    // Singleton
    private static ProfileManager manager = new ProfileManager();
    /**
     * Gets the singleton instance of the ProfileManager.
     *
     * @return The single, static instance of ProfileManager.
     */
    public static ProfileManager getInstance() {
        return manager;
    }

    // Profile getters

    /**
     * Retrieves the local cache of all user profiles.
     *
     * @return A list of all {@link Profile} objects currently held by the manager.
     */
    public List<Profile> getProfiles() {
        return profiles;
    }

    /**
     * Finds and retrieves a profile by its unique Firestore document ID.
     *
     * @param id The Firestore document ID of the profile.
     * @return The {@link Profile} object with the matching ID, or {@code null} if no profile is found.
     */
    public Profile getProfileByDBID(String id) {
        for (Profile profile : profiles) {
            if (profile.getId().equals(id)) {
                return profile;
            }
        }
        return null;
    }

    /**
     * Finds and retrieves a profile by the user's unique device ID.
     *
     * @param deviceId The device ID associated with the profile.
     * @return The {@link Profile} object with the matching device ID, or {@code null} if no profile is found.
     */
    public Profile getProfileByDeviceId(String deviceId) {
        for (Profile profile : profiles) {
            if (profile.getDeviceId().equals(deviceId)) {
                return profile;
            }
        }
        return null;
    }

    /**
     * Checks if a profile associated with a given device ID exists.
     *
     * @param deviceId The device ID to check for.
     * @return {@code true} if a profile with the specified device ID exists, {@code false} otherwise.
     */
    public boolean doesProfileExist(String deviceId) {
        return getProfileByDeviceId(deviceId) != null;
    }

    // Create, update, delete operations

    /**
     * Asynchronously creates a new profile in the Firestore database.
     *
     * @param profile The {@link Profile} object to create.
     */
    public void createProfile(Profile profile) {
        connector.createAsync(profile);
    }

    /**
     * Asynchronously updates an existing profile in the Firestore database.
     *
     * @param profile The {@link Profile} object with updated data. Its ID must be set.
     */
    public void updateProfile(Profile profile) {
        connector.updateAsync(profile);
    }

    /**
     * Asynchronously deletes a profile from the Firestore database.
     *
     * @param profile The {@link Profile} object to delete. Its ID must be set.
     */
    public void deleteProfile(Profile profile) {
        connector.deleteAsync(profile);
    }

    /**
     * Callback method invoked by {@link DBConnector} when the profile data changes in Firestore.
     * It updates the local profile cache and notifies all registered views of the change.
     *
     * @param objects The updated list of {@link Profile} objects from Firestore.
     */
    @Override
    public void readAllAsync_Complete(List<Profile> objects) {
        Log.d(TAG, "ProfileManager read all complete, notifying views");
        profiles = objects;
        // Notify views of profile changes
        notifyViews();
    }
    
}
