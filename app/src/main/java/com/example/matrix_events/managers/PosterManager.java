package com.example.matrix_events.managers;

import android.net.Uri;
import android.util.Log;

import com.example.matrix_events.database.DBConnector;
import com.example.matrix_events.database.DBListener;
import com.example.matrix_events.entities.Poster;
import com.example.matrix_events.mvc.Model;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages event posters by handling both the image file uploads to Firebase Storage
 * and the metadata storage in Firestore. This class follows the singleton pattern for a single
 * point of access. As a {@link Model}, it notifies registered views of any data changes.
 */
public class PosterManager extends Model implements DBListener<Poster> {
    private static final String TAG = "PosterManager";

    private final List<Poster> posters = new ArrayList<>();
    private final DBConnector<Poster> connector = new DBConnector<Poster>("posters", this, Poster.class);

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
     * Asynchronously uploads a poster image to Firebase Storage.
     * Upon successful upload, it retrieves the download URL and creates a corresponding
     * {@link Poster} document in Firestore.
     *
     * @param imageUri The local URI of the image to be uploaded.
     * @param eventId  The ID of the event to which this poster belongs.
     * @param callback A callback to handle the success or failure of the upload process.
     */
    public void uploadPosterImage(Uri imageUri, String eventId, PosterUploadCallback callback) {
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

            // waiting for the poster to appear in Firebase with its ID
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                private int attempts = 0;
                private final int MAX_ATTEMPTS = 50; // 5 seconds max

                @Override
                public void run() {
                    // look for the poster in Firebase
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

    private Poster findPosterByEventIdAndFileName(String eventId, String fileName) {
        for (Poster poster : posters) {
            if (eventId.equals(poster.getEventId()) && fileName.equals(poster.getFileName())) {
                return poster;
            }
        }
        return null;
    }


    public void updatePosterImage(Uri imageUri, Poster poster, PosterUploadCallback callback) {
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
     * Gets the posters
     * @return The list of posters
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
     * Asynchronously deletes a poster document from the Firestore database.
     * Note: This does not delete the actual image file from Firebase Storage.
     *
     * @param poster The {@link Poster} object to delete. Its ID must be set.
     */
    public void deletePoster(Poster poster) {
        connector.deleteAsync(poster);
    }

    /**
     * Callback method invoked by {@link DBConnector} when poster data changes in Firestore.
     * It updates the local poster cache and notifies all registered views of the change.
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