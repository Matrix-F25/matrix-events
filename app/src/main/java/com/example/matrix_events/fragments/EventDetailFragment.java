package com.example.matrix_events.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.utils.TimestampConverter;

public class EventDetailFragment extends Fragment implements com.example.matrix_events.mvc.View {

    View view = null;
    private Event event = null;

    public EventDetailFragment() {
        super(R.layout.fragment_event_detail);
    }

    public static EventDetailFragment newInstance(Event event) {
        EventDetailFragment fragment = new EventDetailFragment();
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
        render();

        Button backButton = view.findViewById(R.id.event_back_button);
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Back Button Functionality
        Button waitlistButton = view.findViewById(R.id.event_waitlist_join_button);
        waitlistButton.setOnClickListener(v -> {
            Log.d("DEBUG", "waitlist button clicked");
        });

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
        assert event != null;
        render();
    }

    public void render() {
        // Event Title
        TextView eventTitleTextview = view.findViewById(R.id.event_title_textview);
        eventTitleTextview.setText(event.getName());

        // Event Description
        TextView eventDescriptionTextview = view.findViewById(R.id.event_description_textview);
        eventDescriptionTextview.setText(event.getDescription());

        // Organizer Name
        TextView organizer = view.findViewById(R.id.event_organizer_name_textview);
        if (event.getOrganizer() != null) {
            organizer.setText(event.getOrganizer().getName());
        }

        // Event Start Date/Time
        TextView eventStartDateTextview = view.findViewById(R.id.event_start_date_textview);
        eventStartDateTextview.setText(TimestampConverter.convertFirebaseTimestampToString(event.getEventStartDateTime()));

        // Event End Date/Time
        TextView eventEndDateTextview = view.findViewById(R.id.event_end_date_textview);
        eventEndDateTextview.setText(TimestampConverter.convertFirebaseTimestampToString(event.getEventEndDateTime()));

        // Registration Open Date/Time
        TextView registrationOpenTextview = view.findViewById(R.id.registration_open_textview);
        registrationOpenTextview.setText(TimestampConverter.convertFirebaseTimestampToString(event.getRegistrationStartDateTime()));

        // Registration Close Date/Time
        TextView registrationClosesTextview = view.findViewById(R.id.registration_closes_textview);
        registrationClosesTextview.setText(TimestampConverter.convertFirebaseTimestampToString(event.getRegistrationEndDateTime()));

        // Max Waitlist Capacity
        TextView maxWaitlistCapTextview = view.findViewById(R.id.event_max_waitlist_cap_textview);
        maxWaitlistCapTextview.setText(String.valueOf(event.getWaitlistCapacity()));

        // Max Event Capacity
        TextView maxEventCapTextview = view.findViewById(R.id.event_max_cap_textview);
        maxEventCapTextview.setText(String.valueOf(event.getEventCapacity()));

        // Waitlist Status
        TextView waitlistStatusTextview = view.findViewById(R.id.waitlist_status_textview);
        waitlistStatusTextview.setText("TODO");
    }

}
