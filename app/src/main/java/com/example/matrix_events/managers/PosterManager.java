package com.example.matrix_events.managers;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.matrix_events.database.DBConnector;
import com.example.matrix_events.database.DBListener;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Poster;
import com.example.matrix_events.mvc.Model;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages event posters by handling both the image file uploads to Firebase Storage
 * and the metadata storage in Firestore.
 * <p>
 * This class follows the <b>Singleton Pattern</b> for a single point of access.
 * It acts as a bridge between the binary data storage (images) and the structured database (document links).
 * </p>
 * <p>
 * As a {@link Model}, it maintains a local cache of {@link Poster} objects and notifies
 * registered views of any data changes.
 * </p>
 */
public class PosterManager extends Model implements DBListener<Poster> {
    private static final String TAG = "PosterManager";

    private final List<Poster> posters = new ArrayList<>();
    private final DBConnector<Poster> connector = new DBConnector<>("posters", this, Poster.class);

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference posterStorageRef = storage.getReference("posters");

    // Singleton
    private static final PosterManager manager = new PosterManager();

    /**
     * Gets the singleton instance of the PosterManager.
     *
     * @return The single, static instance of PosterManager.
     */
    public static PosterManager getInstance() {
        return manager;
    }

    /**
     * Asynchronously uploads a poster image to Firebase Storage and creates a metadata entry.
     * <p>
     * This method performs a multi-step process:
     * <ol>
     * <li>Uploads the raw image file to Firebase Storage.</li>
     * <li>Retrieves the public download URL.</li>
     * <li>Creates a {@link Poster} document in Firestore.</li>
     * <li>Polls the local cache until the new ID is assigned by the DB listener (resolving the race condition).</li>
     * </ol>
     * </p>
     *
     * @param imageUri The local URI of the image to be uploaded. Cannot be null.
     * @param eventId  The ID of the event to which this poster belongs. Cannot be null.
     * @param callback A callback to handle the success (returning the created Poster) or failure.
     */
    public void uploadPosterImage(@NonNull Uri imageUri, @NonNull String eventId, @NonNull PosterUploadCallback callback) {
        if (imageUri == null) {
            callback.onFailure(new IllegalArgumentException("No image selected"));
            return;
        }

        String fileName = "poster_" + System.currentTimeMillis() + ".jpg";
        StorageReference posterRef = posterStorageRef.child(fileName);

        UploadTask uploadTask = posterRef.putFile(imageUri);

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return posterRef.getDownloadUrl();
        }).addOnSuccessListener(downloadUri -> {
            Poster poster = new Poster(downloadUri.toString(), eventId, fileName);
            createPoster(poster);

            // Polling mechanism: waiting for the poster to appear in the local cache with its generated ID
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                private int attempts = 0;
                private final int MAX_ATTEMPTS = 50; // 5 seconds max (50 * 100ms)

                @Override
                public void run() {
                    // look for the poster in the synced list
                    Poster createdPoster = findPosterByEventIdAndFileName(eventId, fileName);

                    if (createdPoster != null && createdPoster.getId() != null && !createdPoster.getId().isEmpty()) {
                        // found with an ID
                        Log.d(TAG, "Poster created with ID: " + createdPoster.getId());
                        callback.onSuccess(createdPoster);
                    } else if (attempts < MAX_ATTEMPTS) {
                        // try again in 100ms
                        attempts++;
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this, 100);
                    } else {
                        // timeout
                        callback.onFailure(new Exception("Timeout waiting for poster ID assignment"));
                    }
                }
            }, 100);

        }).addOnFailureListener(callback::onFailure);
    }

    /**
     * Helper method to find a specific poster in the cache based on event ID and file name.
     * Used primarily by the polling logic in {@link #uploadPosterImage(Uri, String, PosterUploadCallback)}.
     *
     * @param eventId  The event ID.
     * @param fileName The unique file name.
     * @return The {@link Poster} if found, null otherwise.
     */
    @Nullable
    private Poster findPosterByEventIdAndFileName(String eventId, String fileName) {
        for (Poster poster : posters) {
            if (eventId.equals(poster.getEventId()) && fileName.equals(poster.getFileName())) {
                return poster;
            }
        }
        return null;
    }

    /**
     * Asynchronously updates the image file for an existing poster.
     * <p>
     * This overwrites the file in Firebase Storage and updates the download URL in the Firestore document.
     * </p>
     *
     * @param imageUri The new image URI. Cannot be null.
     * @param poster   The existing poster object to update. Cannot be null.
     * @param callback A callback to handle success or failure.
     */
    public void updatePosterImage(@NonNull Uri imageUri, @NonNull Poster poster, @NonNull PosterUploadCallback callback) {
        if (imageUri == null) {
            callback.onFailure(new IllegalArgumentException("No image selected"));
            return;
        }

        StorageReference posterRef = posterStorageRef.child(poster.getFileName());

        UploadTask uploadTask = posterRef.putFile(imageUri);

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return posterRef.getDownloadUrl();
        }).addOnSuccessListener(downloadUri -> {
            poster.setImageUrl(downloadUri.toString());
            updatePoster(poster);
            callback.onSuccess(poster);
        }).addOnFailureListener(callback::onFailure);
    }

    /**
     * Gets the local cache of posters.
     *
     * @return The list of {@link Poster} objects.
     */
    public List<Poster> getPosters() {
        return posters;
    }

    /**
     * Asynchronously creates a new poster document in the Firestore database.
     *
     * @param poster The {@link Poster} object to create.
     */
    public void createPoster(Poster poster) {
        connector.createAsync(poster);
    }

    /**
     * Asynchronously updates an existing poster document in the Firestore database.
     *
     * @param poster The {@link Poster} object with updated data. Its ID must be set.
     */
    public void updatePoster(Poster poster) {
        connector.updateAsync(poster);
    }

    /**
     * Asynchronously deletes a poster document and its associated image file.
     * <p>
     * This method ensures data consistency by:
     * <ol>
     * <li>Nullifying the poster reference in the associated {@link Event} via {@link EventManager}.</li>
     * <li>Deleting the actual image binary from Firebase Storage.</li>
     * <li>Deleting the poster metadata document from Firestore.</li>
     * </ol>
     * </p>
     *
     * @param poster The {@link Poster} object to delete. Its ID must be set.
     */
    public void deletePoster(@NonNull Poster poster) {

        String eventId = poster.getEventId();

        if (eventId != null) {
            Event event = EventManager.getInstance().getEventByDBID(eventId);

            // if the event still exists, set the poster to null to prevent broken links
            if (event != null) {
                event.setPoster(null);
                EventManager.getInstance().updateEvent(event);
            }

            // Delete from Storage
            if (poster.getImageUrl() != null) {
                StorageReference imageRef = storage.getReferenceFromUrl(poster.getImageUrl());
                imageRef.delete().addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Poster deleted successfully from Storage");
                }).addOnFailureListener(exception -> {
                    Log.e(TAG, "Error deleting poster from Storage", exception);
                });
            }

            // Delete from Firestore
            connector.deleteAsync(poster);
        }
    }

    /**
     * Callback method invoked by {@link DBConnector} when poster data changes in Firestore.
     * <p>
     * It updates the local poster cache and notifies all registered views of the change.
     * </p>
     *
     * @param objects The updated list of {@link Poster} objects from Firestore.
     */
    @Override
    public void readAllAsync_Complete(List<Poster> objects) {
        Log.d(TAG, "PosterManager read all complete, notifying views");
        posters.clear();
        posters.addAll(objects);
        notifyViews();
    }

    /**
     * A callback interface for handling the result of a poster upload operation.
     */
    public interface PosterUploadCallback {
        /**
         * Called when the poster image is successfully uploaded and its metadata
         * is saved to Firestore.
         *
         * @param poster The newly created {@link Poster} object, containing the download URL.
         */
        void onSuccess(Poster poster);

        /**
         * Called when the poster upload or Firestore operation fails.
         *
         * @param e The exception that occurred during the process.
         */
        void onFailure(Exception e);
    }
}