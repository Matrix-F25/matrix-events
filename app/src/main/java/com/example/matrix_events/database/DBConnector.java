package com.example.matrix_events.database;


import android.util.Log;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class DBConnector<T extends DBObject> {
    private static final String TAG = "DBConnector";

    // Firestore collection reference
    private final CollectionReference collectionRef;

    // Generic constructor, connects to arbitrary collection in database
    public DBConnector(String collection, DBListener<T> listener, Class<T> objectType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        collectionRef = db.collection(collection);
        collectionRef
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "FireStore collection listener failed", e);
                        return;
                    }
                    if (snapshots == null) {
                        Log.w(TAG, "FireStore collection no snapshot data received");
                        return;
                    }
                    Log.d(TAG, "FireStore collection registered an update. Reading collection of " + snapshots.size() + " documents");

                    ArrayList<T> objectList = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : snapshots.getDocuments()) {
                        T object = documentSnapshot.toObject(objectType);
                        if (object != null) {
                            object.setId(documentSnapshot.getId());
                            objectList.add(object);
                        }
                    }
                    listener.readAllAsync_Complete(objectList);
                });
    }

    public void createAsync(T object) {
        Log.d(TAG, "Attempting to create document");
        collectionRef
                .add(object)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Document created with ID: " + documentReference.getId());
                    object.setId(documentReference.getId());
                    collectionRef.document(object.getId()).update("id", object.getId());
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error creating document", e));
    }

    public void updateAsync(T object) {
        Log.d(TAG, "Attempting to update document");
        if (object == null || object.getId().isEmpty()) {
            Log.w(TAG, "Cannot update object with null or empty ID");
            return;
        }
        collectionRef
                .document(object.getId())
                .set(object)
                .addOnSuccessListener(command -> {
                    Log.d(TAG, "Document with ID: " + object.getId() + " successfully updated");
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
    }

    public void deleteAsync(T object) {
        Log.d(TAG, "Attempting to delete document");
        if (object == null || object.getId().isEmpty()) {
            Log.w(TAG, "Cannot delete object with null or empty ID");
            return;
        }
        collectionRef
                .document(object.getId())
                .delete()
                .addOnSuccessListener(command -> {
                    Log.d(TAG, "Document with ID: " + object.getId() + " successfully deleted");
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
    }
}
