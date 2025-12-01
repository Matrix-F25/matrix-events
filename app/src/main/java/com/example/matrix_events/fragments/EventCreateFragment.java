package com.example.matrix_events.fragments;

import androidx.fragment.app.Fragment;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.matrix_events.R;
import com.example.matrix_events.entities.Poster;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.managers.PosterManager;
import com.example.matrix_events.managers.ProfileManager;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.entities.ReoccurringType;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.Timestamp;

import java.util.Calendar;
import java.util.List;

public class EventCreateFragment extends Fragment {

    View view = null;
    private EditText nameInput, descriptionInput, capacityInput, locationInput, waitlistCapacityInput;
    private MaterialSwitch isReoccurringSwitch, geolocationTrackingSwitch;
    private Spinner reoccurringTypeSpinner;
    private View reoccurringSection;
    private Timestamp eventStart, eventEnd, regStart, regEnd, reoccurringEndDateTime;
    private Uri posterUri = null;

    private Button backButton, createButton;
    private boolean isCreating = false;

    private static final String TAG = "EventCreateFragment";

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    posterUri = uri;
                    Toast.makeText(requireContext(), "Poster selected!", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_create, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        backButton = view.findViewById(R.id.back_btn);
        backButton.setOnClickListener(v -> {
            if (!isCreating) {
                getParentFragmentManager().popBackStack();
            } else {
                Toast.makeText(requireContext(), "Please wait, creating event...", Toast.LENGTH_SHORT).show();
            }
        });

        render();
    }

    public void render() {
        // Inputs
        nameInput = view.findViewById(R.id.event_name_input);
        descriptionInput = view.findViewById(R.id.event_description_input);
        capacityInput = view.findViewById(R.id.event_capacity_input);
        waitlistCapacityInput = view.findViewById(R.id.waitlist_capacity_input);
        locationInput = view.findViewById(R.id.event_location_input);

        // Show/hide reoccurring section
        isReoccurringSwitch = view.findViewById(R.id.is_reoccurring_switch);
        reoccurringSection = view.findViewById(R.id.reoccurring_section);
        isReoccurringSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            reoccurringSection.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Spinner setup using enum
        reoccurringTypeSpinner = view.findViewById(R.id.reoccurring_type_spinner);
        ArrayAdapter<ReoccurringType> typeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                ReoccurringType.values()
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reoccurringTypeSpinner.setAdapter(typeAdapter);

        // Pick reoccurring end date
        Button reoccurringEndDateButton = view.findViewById(R.id.reoccurring_end_date_btn);
        reoccurringEndDateButton.setOnClickListener(v -> pickDateTime(ts -> reoccurringEndDateTime = ts));

        geolocationTrackingSwitch = view.findViewById(R.id.geolocation_tracking_switch);

        // Pick registration start date
        Button regStartButton = view.findViewById(R.id.reg_start_date_btn);
        regStartButton.setOnClickListener(v -> pickDateTime(ts -> regStart = ts));

        // Pick registration end date
        Button regEndButton = view.findViewById(R.id.reg_end_date_btn);
        regEndButton.setOnClickListener(v -> pickDateTime(ts -> regEnd = ts));

        // Pick event start date
        Button startDateButton = view.findViewById(R.id.event_start_date_btn);
        startDateButton.setOnClickListener(v -> pickDateTime(ts -> eventStart = ts));

        // Pick event end date
        Button endDateButton = view.findViewById(R.id.event_end_date_btn);
        endDateButton.setOnClickListener(v -> pickDateTime(ts -> eventEnd = ts));

        // Upload poster
        Button uploadPosterButton = view.findViewById(R.id.upload_poster_btn);
        uploadPosterButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Create event
        createButton = view.findViewById(R.id.create_event_btn);
        createButton.setOnClickListener(v -> uploadPosterThenCreateEvent());
    }

    private interface TimestampCallback {
        void onTimestampSelected(Timestamp timestamp);
    }

    private void pickDateTime(TimestampCallback callback) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, day) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                            (timeView, hour, minute) -> {
                                calendar.set(year, month, day, hour, minute);
                                callback.onTimestampSelected(new Timestamp(calendar.getTime()));
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true);
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    public void setLoading(boolean loading) {
        isCreating = loading;
        if (createButton != null) {
            createButton.setEnabled(!loading);
        }
        if (backButton != null) {
            backButton.setEnabled(!loading);
        }
    }

    private void uploadPosterThenCreateEvent() {
        try {
            // event input info
            String name = nameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String locationName = locationInput.getText().toString().trim();
            int capacity = Integer.parseInt(capacityInput.getText().toString().trim());
            Integer waitlistCapacity = null;

            if (!waitlistCapacityInput.getText().toString().trim().isEmpty()) {
                waitlistCapacity = Integer.parseInt(waitlistCapacityInput.getText().toString().trim());
            }

            boolean isReoccurring = isReoccurringSwitch.isChecked();
            boolean geolocationTrackingEnabled = geolocationTrackingSwitch.isChecked();

            ReoccurringType reoccurringType = null;
            if (isReoccurring) {
                String selected = reoccurringTypeSpinner.getSelectedItem().toString();
                reoccurringType = ReoccurringType.valueOf(selected);
            }

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) ||
                    eventStart == null || eventEnd == null || regStart == null || regEnd == null) {
                Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // start loading state
            setLoading(true);

            String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            Profile organizer = ProfileManager.getInstance().getProfileByDeviceId(deviceId);

            // create the base event first (without poster)
            Event newEvent = new Event(
                    name,
                    description,
                    organizer,
                    locationName,
                    eventStart,
                    eventEnd,
                    capacity,
                    waitlistCapacity,
                    regStart,
                    regEnd,
                    isReoccurring,
                    reoccurringEndDateTime,
                    reoccurringType,
                    geolocationTrackingEnabled,
                    null
            );

            // create event with poster if uploaded
            if (posterUri != null) {
                // get current event count before creating event
                final int eventCountBefore = EventManager.getInstance().getEvents().size();

                // create event first using DBConnector
                EventManager.getInstance().createEvent(newEvent);

                // wait for event to get its ID from Firestore
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                    private int attempts = 0;
                    private final int MAX_ATTEMPTS = 50; // 5 seconds max

                    @Override
                    public void run() {
                        // check if a new event has been added
                        List<Event> currentEvents = EventManager.getInstance().getEvents();

                        if (currentEvents.size() > eventCountBefore) {
                            // find the newest event by this organizer with matching details
                            Event createdEvent = null;
                            for (Event event : currentEvents) {
                                if (event.getId() != null && !event.getId().isEmpty() &&
                                        event.getName().equals(name) &&
                                        event.getOrganizer().getDeviceId().equals(deviceId)) {
                                    createdEvent = event;
                                    break;
                                }
                            }
                            if (createdEvent != null) {
                                // event has its ID
                                String eventId = createdEvent.getId();

                                PosterManager.getInstance().uploadPosterImage(posterUri, eventId,
                                        new PosterManager.PosterUploadCallback() {
                                            @Override
                                            public void onSuccess(Poster poster) {
                                                // poster has its Firestore ID
                                                Event latestEvent = EventManager.getInstance().getEventByDBID(eventId);
                                                if (latestEvent != null) {
                                                    latestEvent.setPoster(poster);
                                                    EventManager.getInstance().updateEvent(latestEvent);
                                                }

                                                // stop loading and close fragment
                                                setLoading(false);
                                                Toast.makeText(requireContext(), "Event and poster uploaded!", Toast.LENGTH_SHORT).show();
                                                getParentFragmentManager().popBackStack();
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                Log.e(TAG, "Poster upload failed", e);
                                                setLoading(false);
                                                Toast.makeText(requireContext(), "Poster upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            } else if (attempts < MAX_ATTEMPTS) {
                                // try again in 100ms
                                attempts++;
                                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this, 100);
                            } else {
                                // timeout
                                Log.e(TAG, "Timeout waiting for event ID");
                                setLoading(false);
                                Toast.makeText(requireContext(), "Error: Timeout creating event", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }, 100);
            } else {
                // null poster
                EventManager.getInstance().createEvent(newEvent);

                // wait before closing
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    setLoading(false);
                    Toast.makeText(requireContext(), "Event created successfully!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                    }, 500);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error creating event: ", e);
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
