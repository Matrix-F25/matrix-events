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

public class PosterManager extends Model implements DBListener<Poster> {
    private static final String TAG = "PosterManager";

    private final List<Poster> posters = new ArrayList<>();
    private final DBConnector<Poster> connector = new DBConnector<Poster>("posters", this, Poster.class);

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference posterStorageRef = storage.getReference("posters");

    // Singleton
    private static final PosterManager manager = new PosterManager();
    public static PosterManager getInstance() {
        return manager;
    }

    // Firebase Storage upload
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
            callback.onSuccess(poster);
        }).addOnFailureListener(callback::onFailure);
    }

    // Create, update, delete operations for organizers and admins
    public void createPoster(Poster poster) {
        connector.createAsync(poster);
    }

    public void updatePoster(Poster poster) {
        connector.updateAsync(poster);
    }

    public void deletePoster(Poster poster) {
        connector.deleteAsync(poster);
    }

    @Override
    public void readAllAsync_Complete(List<Poster> objects) {
        Log.d(TAG, "PosterManager read all complete, notifying views");
        posters.clear();
        posters.addAll(objects);
        notifyViews();
    }

    public interface PosterUploadCallback {
        void onSuccess(Poster poster);
        void onFailure(Exception e);
    }
}
