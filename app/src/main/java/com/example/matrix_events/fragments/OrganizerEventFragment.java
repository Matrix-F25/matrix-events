package com.example.matrix_events.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.matrix_events.R;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.utils.TimestampConverter;

public class OrganizerEventFragment extends Fragment implements com.example.matrix_events.mvc.View {

    private static final String TAG = "OrganizerEventFragment";
    View view = null;
    private Event event = null;

    public OrganizerEventFragment() {
        super(R.layout.fragment_organizer_event);
    }

    public static OrganizerEventFragment newInstance(Event event) {
        OrganizerEventFragment fragment = new OrganizerEventFragment();
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

        Button backButton = view.findViewById(R.id.org_event_back_button);
        backButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        Button waitlistButton = view.findViewById(R.id.org_event_waitlist_button);
        waitlistButton.setOnClickListener(v -> {
            EventEntrantListFragment fragment = EventEntrantListFragment.newInstance(event, EventEntrantListFragment.ListType.WAITING_LIST);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        Button pendingListButton = view.findViewById(R.id.org_event_pending_list_button);
        pendingListButton.setOnClickListener(v -> {
            EventEntrantListFragment fragment = EventEntrantListFragment.newInstance(event, EventEntrantListFragment.ListType.PENDING_LIST);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        Button acceptedListButton = view.findViewById(R.id.org_event_accepted_list_button);
        acceptedListButton.setOnClickListener(v -> {
            EventEntrantListFragment fragment = EventEntrantListFragment.newInstance(event, EventEntrantListFragment.ListType.ACCEPTED_LIST);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        Button declinedListButton = view.findViewById(R.id.org_event_declined_list_button);
        declinedListButton.setOnClickListener(v -> {
            EventEntrantListFragment fragment = EventEntrantListFragment.newInstance(event, EventEntrantListFragment.ListType.DECLINED_LIST);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        Button editButton = view.findViewById(R.id.org_event_edit_button);
        editButton.setOnClickListener(v -> {
            EventEditFragment fragment = EventEditFragment.newInstance(event);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        Button qrCodeButton = view.findViewById(R.id.org_event_qr_code_button);
        qrCodeButton.setOnClickListener(v -> {
            EventQRCodeFragment fragment = EventQRCodeFragment.newInstance(event);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main, fragment)
                    .addToBackStack(null)
                    .commit();
        });

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
        if (!isAdded() || getContext() == null) {
            Log.w(TAG, "Fragment not attached, skipping update");
            return;
        }

        event = EventManager.getInstance().getEventByDBID(event.getId());
        if (event != null) {
            render();
        }
    }

    public void render() {
        if (!isAdded() || getContext() == null) {
            Log.w(TAG, "Fragment not attached, skipping update");
            return;
        }

        Context context = getContext();
        if (context == null) {
            return;
        }

        // Display poster image if available
        ImageView posterImage = view.findViewById(R.id.org_event_poster_image);

        if (event.getPoster() != null && event.getPoster().getImageUrl() != null) {
            String posterUrl = event.getPoster().getImageUrl();
            Glide.with(context)
                    .load(posterUrl)
                    .placeholder(R.drawable.placeholder)        // optional
                    .error(R.drawable.placeholder)              // optional
                    .into(posterImage);
        } else {
            posterImage.setImageResource(R.drawable.placeholder);
        }

        // Event Title
        TextView eventTitleTextview = view.findViewById(R.id.org_event_title_textview);
        eventTitleTextview.setText(event.getName());

        // Event Description
        TextView eventDescriptionTextview = view.findViewById(R.id.org_event_description_textview);
        eventDescriptionTextview.setText(event.getDescription());

        // Event Location
        TextView eventLocation = view.findViewById(R.id.org_event_location_textview);
        eventLocation.setText(event.getLocation());

        // Event Start Date/Time
        TextView eventStartDateTextview = view.findViewById(R.id.org_event_start_date_textview);
        eventStartDateTextview.setText(TimestampConverter.convertFirebaseTimestampToString(event.getEventStartDateTime()));

        // Event End Date/Time
        TextView eventEndDateTextview = view.findViewById(R.id.org_event_end_date_textview);
        eventEndDateTextview.setText(TimestampConverter.convertFirebaseTimestampToString(event.getEventEndDateTime()));

        // Event Reoccurring Details
        TextView eventReoccurringTextview = view.findViewById(R.id.org_reoccuring_textview);
        if (event.isReoccurring()) {
            String output = String.valueOf(event.getReoccurringType()) + ", until " + TimestampConverter.convertFirebaseTimestampToString(event.getReoccurringEndDateTime());
            eventReoccurringTextview.setText(output);
        } else {
            eventReoccurringTextview.setText("Event is not reoccurring");
        }

        // Registration Open Date/Time
        TextView registrationOpenTextview = view.findViewById(R.id.org_registration_open_textview);
        registrationOpenTextview.setText(TimestampConverter.convertFirebaseTimestampToString(event.getRegistrationStartDateTime()));

        // Registration Close Date/Time
        TextView registrationClosesTextview = view.findViewById(R.id.org_registration_closes_textview);
        registrationClosesTextview.setText(TimestampConverter.convertFirebaseTimestampToString(event.getRegistrationEndDateTime()));

        // Max Waitlist Capacity
        TextView maxWaitlistCapTextview = view.findViewById(R.id.org_event_max_waitlist_cap_textview);
        if (event.getWaitlistCapacity() != null) {
            maxWaitlistCapTextview.setText(String.valueOf(event.getWaitlistCapacity()));
        } else {
            maxWaitlistCapTextview.setText("No limit");
        }

        // Max Event Capacity
        TextView maxEventCapTextview = view.findViewById(R.id.org_event_max_cap_textview);
        maxEventCapTextview.setText(String.valueOf(event.getEventCapacity()));

        // Current Waitlist Size
        int waitListSize = event.getWaitList().size();
        TextView currentWaitlistTextview = view.findViewById(R.id.org_event_current_waitlist_textview);
        if (event.getWaitlistCapacity() != null) {
            currentWaitlistTextview.setText(waitListSize + "/" + event.getWaitlistCapacity());
        } else {
            currentWaitlistTextview.setText(String.valueOf(waitListSize));
        }

        // Current Accepted Size
        int acceptedListSize = event.getAcceptedList().size();
        TextView currentAcceptedListTextview = view.findViewById(R.id.org_event_current_accepted_textview);
        currentAcceptedListTextview.setText(acceptedListSize + "/" + event.getEventCapacity());
    }
}
