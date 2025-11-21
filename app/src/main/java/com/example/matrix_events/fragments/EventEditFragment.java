package com.example.matrix_events.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.matrix_events.R;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Poster;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.managers.PosterManager;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * for editing an events poster
 * Handles selecting a new image from the gallery, displaying it, and uploading it to Firebase.
 */
public class EventEditFragment extends Fragment {
    private static final String TAG = "EventEditFragment";
    private static final String ARG_EVENT = "event";
    private static final String ARG_EVENT_ID = "event_id";

    private Event event;
    private Uri posterUri = null;
    private ImageView eventPosterImage;
    private MaterialSwitch geolocationTrackingSwitch;

    // Launcher for selecting an image from the gallery
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    posterUri = uri;
                    // Show preview of selected image
                    if (isAdded() && getContext() != null) {
                        Glide.with(this)
                                .load(posterUri)
                                .into(eventPosterImage);
                        Toast.makeText(requireContext(), "Poster selected!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    public EventEditFragment() {
        // Required empty public constructor
    }

    /**
     * Creates a new instance of EventEditFragment with the given event
     */
    public static EventEditFragment newInstance(Event event) {
        EventEditFragment fragment = new EventEditFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);
        // Pass ID since it might be lost during Event serialization it was rawr
        if (event != null) {
            args.putString(ARG_EVENT_ID, event.getId());
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable(ARG_EVENT);
            // Restore the ID if it was lost it was even harder rawwr
            if (event != null) {
                String eventId = getArguments().getString(ARG_EVENT_ID);
                if (eventId != null) {
                    event.setId(eventId);
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_edit, container, false);
        
        // Initialize Views
        Button backButton = view.findViewById(R.id.back_btn);
        Button uploadPosterButton = view.findViewById(R.id.upload_poster_btn);
        Button confirmChangesButton = view.findViewById(R.id.confirm_changes_btn);
        eventPosterImage = view.findViewById(R.id.event_poster_image);
        geolocationTrackingSwitch = view.findViewById(R.id.geolocation_tracking_switch);

        // Load existing poster if available
        if (event != null && event.getPoster() != null && event.getPoster().getImageUrl() != null) {
            Glide.with(requireContext())
                    .load(event.getPoster().getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(eventPosterImage);
        } else {
            eventPosterImage.setImageResource(R.drawable.placeholder);
        }

        // Initialize Geolocation Switch
        if (geolocationTrackingSwitch != null && event != null) {
            geolocationTrackingSwitch.setChecked(Boolean.TRUE.equals(event.isGeolocationTrackingEnabled()));
        }

        // Set Click Listeners
        if (backButton != null) {
            backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        uploadPosterButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        confirmChangesButton.setOnClickListener(v -> uploadPosterThenUpdateEvent());
        
        return view;
    }

    /**
     * Uploads the selected poster image and updates the event in Firestore.
     */
    private void uploadPosterThenUpdateEvent() {
        // Update geolocation tracking status
        if (event != null && geolocationTrackingSwitch != null) {
            event.setGeolocationTrackingEnabled(geolocationTrackingSwitch.isChecked());
        }

        if (posterUri != null) {
            PosterManager.getInstance().uploadPosterImage(posterUri, event.getId(),
                    new PosterManager.PosterUploadCallback() {
                        @Override
                        public void onSuccess(Poster poster) {
                            // Update event with new poster details
                            poster.setEventId(event.getId());
                            event.setPoster(poster);
                            EventManager.getInstance().updateEvent(event);
                            
                            if (isAdded() && getContext() != null) {
                                Toast.makeText(requireContext(), "Event updated successfully!", Toast.LENGTH_SHORT).show();
                                getParentFragmentManager().popBackStack();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Poster upload failed", e);
                            if (isAdded() && getContext() != null) {
                                Toast.makeText(requireContext(), "Poster upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
             // Just update the event details (e.g. geolocation tracking) without changing poster
             if (event != null) {
                 EventManager.getInstance().updateEvent(event);
                 Toast.makeText(requireContext(), "Event updated successfully!", Toast.LENGTH_SHORT).show();
                 getParentFragmentManager().popBackStack();
             } else {
                 getParentFragmentManager().popBackStack();
             }
        }
    }
}
