package com.example.matrix_events.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.matrix_events.R;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Geolocation;
import com.example.matrix_events.entities.Poster;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.managers.PosterManager;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * for editing an events poster
 * Handles selecting a new image from the gallery, displaying it, and uploading it to Firebase.
 */
public class EventEditFragment extends Fragment implements com.example.matrix_events.mvc.View {
    private static final String TAG = "EventEditFragment";

    View view = null;
    ImageView posterImage;
    private Event event = null;
    private Uri posterUri = null;
    private MaterialSwitch geolocationTrackingSwitch;
    private EditText eventNameInput;
    private EditText eventDescriptionInput;
    private EditText eventLocationInput;


    // Launcher for selecting an image from the gallery
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    posterUri = uri;
                    // Show preview of selected image
                    if (isAdded() && getContext() != null) {
                        Glide.with(this)
                                .load(posterUri)
                                .into(posterImage);
                        Toast.makeText(requireContext(), "Poster selected!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    public EventEditFragment() {
        super(R.layout.fragment_event_edit);
    }

    /**
     * Creates a new instance of EventEditFragment with the given event
     */
    public static EventEditFragment newInstance(Event event) {
        EventEditFragment fragment = new EventEditFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }
        assert event != null;

        Button backButton = view.findViewById(R.id.back_btn);
        if (backButton != null) {
            backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        update();

        // observe event manager
        EventManager.getInstance().addView(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventManager.getInstance().removeView(this);
    }

    @Override
    public void update() {
        event = EventManager.getInstance().getEventByDBID(event.getId());
        if (event != null) {
            render();
        }
    }

    public void render() {
        // Display poster image if available
        posterImage = view.findViewById(R.id.event_poster_image);

        if (event.getPoster() != null && event.getPoster().getImageUrl() != null) {
            Glide.with(requireContext())
                    .load(event.getPoster().getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(posterImage);
        } else {
            posterImage.setImageResource(R.drawable.placeholder);
        }

        Button uploadPosterButton = view.findViewById(R.id.upload_poster_btn);
        Button confirmChangesButton = view.findViewById(R.id.confirm_changes_btn);
        Button deleteEventButton = view.findViewById(R.id.delete_event_btn);
        geolocationTrackingSwitch = view.findViewById(R.id.geolocation_tracking_switch);
        eventNameInput = view.findViewById(R.id.event_name_input);
        eventDescriptionInput = view.findViewById(R.id.event_description_input);
        eventLocationInput = view.findViewById(R.id.event_location_input);


        // Initialize Geolocation Switch
        if (geolocationTrackingSwitch != null) {
            geolocationTrackingSwitch.setChecked(Boolean.TRUE.equals(event.isGeolocationTrackingEnabled()));
        }

        // Initialize Event Name
        if (eventNameInput != null) {
            eventNameInput.setText(event.getName());
        }

        if (eventDescriptionInput != null) {
            eventDescriptionInput.setText(event.getDescription());
        }
        if (eventLocationInput != null && event.getLocation() != null) {
            eventLocationInput.setText(event.getLocation().getName());
        }

        if (deleteEventButton != null) {
            deleteEventButton.setOnClickListener(v -> deleteEvent());
        }

        uploadPosterButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        confirmChangesButton.setOnClickListener(v -> uploadPosterThenUpdateEvent());

    }

    /**
     * Uploads the selected poster image and updates the event in Firestore.
     */
    private void uploadPosterThenUpdateEvent() {

        if (eventNameInput != null) {
            event.setName(eventNameInput.getText().toString().trim());
        }
        if (eventDescriptionInput != null) {
            event.setDescription(eventDescriptionInput.getText().toString().trim());
        }
        if (eventLocationInput != null && event.getLocation() != null) {
            String newLocationName = eventLocationInput.getText().toString().trim();
            Geolocation currentLocation = event.getLocation();
            // Create new Geolocation object with new name but same coordinates
            Geolocation newGeolocation = new Geolocation(newLocationName, currentLocation.getLongitude(), currentLocation.getLatitude());
            event.setLocation(newGeolocation);
        }

        // Update geolocation tracking status
        if (geolocationTrackingSwitch != null) {
            event.setGeolocationTrackingEnabled(geolocationTrackingSwitch.isChecked());
        }

        // if poster is changed
        if (posterUri != null) {
            // if poster exists (updates)
            Poster currentPoster = event.getPoster();
            if (currentPoster != null) {
                // updating poster
                PosterManager.getInstance().updatePosterImage(posterUri, currentPoster,
                        new PosterManager.PosterUploadCallback() {
                            @Override
                            public void onSuccess(Poster poster) {
                                EventManager.getInstance().updateEvent(event);

                                if (isAdded() && getContext() != null) {
                                    Toast.makeText(requireContext(), "Event updated successfully!", Toast.LENGTH_SHORT).show();
                                    getParentFragmentManager().popBackStack();
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "Poster update failed", e);
                                if (isAdded() && getContext() != null) {
                                    Toast.makeText(requireContext(), "Poster update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
            // if poster doesn't exist (uploads)
            else {
                // creating poster
                PosterManager.getInstance().uploadPosterImage(posterUri, event.getId(),
                        new PosterManager.PosterUploadCallback() {
                            @Override
                            public void onSuccess(Poster poster) {
                                // poster has its Firestore ID
                                event.setPoster(poster);
                                EventManager.getInstance().updateEvent(event);

                                Toast.makeText(requireContext(), "Event and poster uploaded!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "Poster upload failed", e);
                                Toast.makeText(requireContext(), "Poster upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        } else {
            // Just update the event details (e.g. geolocation tracking) without changing poster
            EventManager.getInstance().updateEvent(event);
            Toast.makeText(requireContext(), "Event updated successfully!", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        }
    }
    /**
     * Deletes the current event from Firebase and navigates back.
     */
    private void deleteEvent() {
        if (event != null) {
            EventManager.getInstance().deleteEvent(event);
            Toast.makeText(requireContext(), "Event deleted successfully", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        }
    }
}
