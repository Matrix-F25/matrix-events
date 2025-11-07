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
import com.example.matrix_events.entities.Geolocation;
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

public class EventCreateFragment extends Fragment {

    private EditText nameInput, descriptionInput, capacityInput, locationInput, waitlistCapacityInput;
    private Button startDateButton, endDateButton, regStartButton, regEndButton, createButton, uploadPosterButton, reoccurringEndDateButton, backButton;
    private MaterialSwitch isReoccurringSwitch, geolocationTrackingSwitch;
    private Spinner reoccurringTypeSpinner;
    private View reoccurringSection;
    private Timestamp eventStart, eventEnd, regStart, regEnd, reoccurringEndDateTime;
    private Uri posterUri = null;

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

        View view = inflater.inflate(R.layout.fragment_event_create, container, false);

        nameInput = view.findViewById(R.id.event_name_input);
        descriptionInput = view.findViewById(R.id.event_description_input);
        capacityInput = view.findViewById(R.id.event_capacity_input);
        waitlistCapacityInput = view.findViewById(R.id.waitlist_capacity_input);
        locationInput = view.findViewById(R.id.event_location_input);

        isReoccurringSwitch = view.findViewById(R.id.is_reoccurring_switch);
        reoccurringSection = view.findViewById(R.id.reoccurring_section);
        reoccurringTypeSpinner = view.findViewById(R.id.reoccurring_type_spinner);
        reoccurringEndDateButton = view.findViewById(R.id.reoccurring_end_date_btn);
        geolocationTrackingSwitch = view.findViewById(R.id.geolocation_tracking_switch);

        regStartButton = view.findViewById(R.id.reg_start_date_btn);
        regEndButton = view.findViewById(R.id.reg_end_date_btn);
        startDateButton = view.findViewById(R.id.event_start_date_btn);
        endDateButton = view.findViewById(R.id.event_end_date_btn);

        uploadPosterButton = view.findViewById(R.id.upload_poster_btn);
        createButton = view.findViewById(R.id.create_event_btn);
        backButton = view.findViewById(R.id.back_btn);

        // Show/hide reoccurring section
        isReoccurringSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            reoccurringSection.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Spinner setup using enum
        ArrayAdapter<ReoccurringType> typeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                ReoccurringType.values()
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reoccurringTypeSpinner.setAdapter(typeAdapter);

        // Pick reoccurring end date
        reoccurringEndDateButton.setOnClickListener(v -> pickDateTime(ts -> reoccurringEndDateTime = ts));

        regStartButton.setOnClickListener(v -> pickDateTime(ts -> regStart = ts));
        regEndButton.setOnClickListener(v -> pickDateTime(ts -> regEnd = ts));
        startDateButton.setOnClickListener(v -> pickDateTime(ts -> eventStart = ts));
        endDateButton.setOnClickListener(v -> pickDateTime(ts -> eventEnd = ts));

        uploadPosterButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        createButton.setOnClickListener(v -> uploadPosterThenCreateEvent());
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
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

            String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            Profile organizer = ProfileManager.getInstance().getProfileByDeviceId(deviceId);
            Geolocation location = new Geolocation(locationName, -123.3656, 48.4284);

            // temp fix for asynch shenanigans, sets aside eventId so poster doesn't grab null because firebase takes forever to create id for event
            String eventId = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("events").document().getId();

            // Create the base event first (without poster)
            Event newEvent = new Event(
                    name,
                    description,
                    organizer,
                    location,
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

            newEvent.setId(eventId);

            // upload the poster after event creation (using its id)
            if (posterUri != null) {
                PosterManager.getInstance().uploadPosterImage(posterUri, eventId,
                        new PosterManager.PosterUploadCallback() {
                            @Override
                            public void onSuccess(Poster poster) {
                                poster.setEventId(eventId);
                                newEvent.setPoster(poster);

                                // poster is not null
                                EventManager.getInstance().createEvent(newEvent);
                                Toast.makeText(requireContext(), "Event and poster uploaded!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "Poster upload failed", e);
                                Toast.makeText(requireContext(), "Poster upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                // poster is null
                EventManager.getInstance().createEvent(newEvent);
                Toast.makeText(requireContext(), "Event created successfully!", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error creating event: ", e);
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
