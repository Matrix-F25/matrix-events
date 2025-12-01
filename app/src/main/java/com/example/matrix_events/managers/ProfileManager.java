package com.example.matrix_events.managers;

import android.net.Uri;
import android.util.Log;

import com.example.matrix_events.database.DBConnector;
import com.example.matrix_events.database.DBListener;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.mvc.Model;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference profileStorageRef = storage.getReference("profiles"); // Profile Folder in Firebase Storage

    // Singleton
    private static ProfileManager manager = new ProfileManager();
    /**
     * Gets the singleton instance of the ProfileManager.
     *
     * @return The single, static instance of ProfileManager.
     */
    public static ProfileManager getInstance() { return manager; }

    // Callback Interface
    public interface ProfileImageUploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(Exception e);
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
     * Upload profile image and update profile's imageUrl in Firestore.
     * fileName uses deviceId to keep it unique per user (replace if re-uploading).
     */
    public void uploadProfilePicture(Uri imageUri, Profile profile, ProfileImageUploadCallback callback) {
        if (imageUri == null || profile == null) {
            callback.onFailure(new IllegalArgumentException("Missing image or profile"));
            return;
        }
        // Ensure Profile has an ID
        if (profile.getId() == null || profile.getId().isEmpty()) {
            callback.onFailure(new IllegalStateException("Profile ID not set"));
            return;
        }

        // Permanent Filename per User for Replacement
        String fileName = "profile_" + profile.getDeviceId() + ".jpg";

        // If profile already has a stored file name, delete the old file safely
        String previousFile = profile.getProfilePictureFileName();
        if (previousFile != null && !previousFile.isEmpty() && !previousFile.equals(fileName)) {
            StorageReference oldRef = profileStorageRef.child(previousFile);
            oldRef.delete().addOnSuccessListener(a -> {
                Log.d(TAG, "Old Profile Picture Deleted.");
            }).addOnFailureListener(e -> {
                Log.w(TAG, "Failed to Delete Old Profile Picture.", e);
            });
        }

        StorageReference ref = profileStorageRef.child(fileName);

        UploadTask uploadTask = ref.putFile(imageUri);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) throw task.getException();
            return ref.getDownloadUrl();
        }).addOnSuccessListener(uri -> {
            String downloadUrl = uri.toString();
            profile.setProfilePictureUrl(downloadUrl);
            profile.setProfilePictureFileName(fileName);
            updateProfile(profile); // Update Firestore via DBConnector
            callback.onSuccess(downloadUrl);
        }).addOnFailureListener(callback::onFailure);
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
        profiles.clear();
        profiles.addAll(objects);
        // Notify views of profile changes
        notifyViews();
    }
}