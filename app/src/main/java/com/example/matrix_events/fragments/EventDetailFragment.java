package com.example.matrix_events.fragments;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.matrix_events.R;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.utils.TimestampConverter;

public class EventDetailFragment extends Fragment implements com.example.matrix_events.mvc.View {

    View view = null;
    private Event event = null;
    private Boolean isAdmin;

    public EventDetailFragment() {
        super(R.layout.fragment_event_detail);
    }

    public static EventDetailFragment newInstance(Event event, boolean isAdmin) {
        EventDetailFragment fragment = new EventDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        args.putBoolean("isAdmin", isAdmin);
        fragment.setArguments(args);
        return fragment;
    }

    public static EventDetailFragment newInstance(Event event) {
        return newInstance(event, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
            isAdmin = getArguments().getBoolean("isAdmin", false);
        }
        assert event != null;

        Button backButton = view.findViewById(R.id.event_back_button);
        backButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
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
        event = EventManager.getInstance().getEventByDBID(event.getId());
        if (event != null) {
            render();
        }
    }

    public void render() {
        // Display poster image if available
        ImageView posterImage = view.findViewById(R.id.event_poster_image);

        if (event.getPoster() != null && event.getPoster().getImageUrl() != null) {
            String posterUrl = event.getPoster().getImageUrl();
            Glide.with(requireContext())
                    .load(posterUrl)
                    .placeholder(R.drawable.placeholder) // optional
                    .error(R.drawable.placeholder)             // optional
                    .into(posterImage);
        } else {
            posterImage.setImageResource(R.drawable.placeholder);
        }

        // Organizer Name
        TextView organizer = view.findViewById(R.id.event_organizer_name_textview);
        if (event.getOrganizer() != null) {
            organizer.setText(event.getOrganizer().getName());
        }

        // Event Title
        TextView eventTitleTextview = view.findViewById(R.id.event_title_textview);
        eventTitleTextview.setText(event.getName());

        // Event Description
        TextView eventDescriptionTextview = view.findViewById(R.id.event_description_textview);
        eventDescriptionTextview.setText(event.getDescription());

        // Event Location
        TextView eventLocation = view.findViewById(R.id.event_location_textview);
        eventLocation.setText(event.getLocation().getName());

        // Event Start Date/Time
        TextView eventStartDateTextview = view.findViewById(R.id.event_start_date_textview);
        eventStartDateTextview.setText(TimestampConverter.convertFirebaseTimestampToString(event.getEventStartDateTime()));

        // Event End Date/Time
        TextView eventEndDateTextview = view.findViewById(R.id.event_end_date_textview);
        eventEndDateTextview.setText(TimestampConverter.convertFirebaseTimestampToString(event.getEventEndDateTime()));

        // Event Reoccurring Details
        TextView eventReoccurringTextview = view.findViewById(R.id.reoccuring_textview);
        if (event.isReoccurring()) {
            String output = String.valueOf(event.getReoccurringType()) + ", until " + TimestampConverter.convertFirebaseTimestampToString(event.getReoccurringEndDateTime());
            eventReoccurringTextview.setText(output);
        } else {
            eventReoccurringTextview.setText("Event is not reoccurring");
        }

        // Registration Open Date/Time
        TextView registrationOpenTextview = view.findViewById(R.id.registration_open_textview);
        registrationOpenTextview.setText(TimestampConverter.convertFirebaseTimestampToString(event.getRegistrationStartDateTime()));

        // Registration Close Date/Time
        TextView registrationClosesTextview = view.findViewById(R.id.registration_closes_textview);
        registrationClosesTextview.setText(TimestampConverter.convertFirebaseTimestampToString(event.getRegistrationEndDateTime()));

        // Max Waitlist Capacity
        TextView maxWaitlistCapTextview = view.findViewById(R.id.event_max_waitlist_cap_textview);
        if (event.getWaitlistCapacity() != null) {
            maxWaitlistCapTextview.setText(String.valueOf(event.getWaitlistCapacity()));
        } else {
            maxWaitlistCapTextview.setText("No limit");
        }

        // Max Event Capacity
        TextView maxEventCapTextview = view.findViewById(R.id.event_max_cap_textview);
        maxEventCapTextview.setText(String.valueOf(event.getEventCapacity()));

        // Current Waitlist Size
        int waitListSize = event.getWaitList().size();
        TextView currentWaitlistTextview = view.findViewById(R.id.current_waitlist_textview);
        if (event.getWaitlistCapacity() != null) {
            currentWaitlistTextview.setText(waitListSize + "/" + event.getWaitlistCapacity());
        } else {
            currentWaitlistTextview.setText(String.valueOf(waitListSize));
        }

        // Current Accepted Size
        int acceptedListSize = event.getAcceptedList().size();
        TextView currentAcceptedListTextview = view.findViewById(R.id.current_accepted_textview);
        currentAcceptedListTextview.setText(acceptedListSize + "/" + event.getEventCapacity());

        // RENDER BASED ON USER/EVENT STATUS:

        TextView listStatusTextview = view.findViewById(R.id.list_status_textview);
        TextView messageTextview = view.findViewById(R.id.message_textview); // Make sure this ID exists in your XML
        Button acceptButton = view.findViewById(R.id.accept_button);
        Button declineButton = view.findViewById(R.id.decline_button);
        Button waitlistButton = view.findViewById(R.id.event_waitlist_join_button);
        View buttonBar = view.findViewById(R.id.button_bar);


        String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Default UI state: hide all action buttons
        acceptButton.setVisibility(View.GONE);
        declineButton.setVisibility(View.GONE);
        waitlistButton.setVisibility(View.GONE);

        if (isAdmin) {
            buttonBar.setVisibility(View.GONE);
        } else {
            buttonBar.setVisibility(View.VISIBLE);
        }

        // Handle UI based on the event's lifecycle

        if (event.isBeforeRegistrationStart()) {
            // State: Registration has not opened yet
            listStatusTextview.setText("Registration Not Open");
            messageTextview.setText("Registration for this event has not opened yet.");

        }
        else if (event.isRegistrationOpen()) {
            // State: Registration is currently open for joining the waitlist
            messageTextview.setText("Registration is open! You can join the waitlist.");
            waitlistButton.setVisibility(View.VISIBLE);

            if (event.inWaitList(deviceId)) {
                listStatusTextview.setText("On the Waitlist");
                waitlistButton.setText("Leave Waitlist");
                waitlistButton.setOnClickListener(v -> {
                    event.leaveWaitList(deviceId);
                    EventManager.getInstance().updateEvent(event);
                });
            } else {
                listStatusTextview.setText("Not on the Waitlist");
                waitlistButton.setText("Join Waitlist");
                waitlistButton.setOnClickListener(v -> {
                    if (event.getOrganizer().getDeviceId().equals(deviceId)) {
                        Toast.makeText(requireContext(), "You may not enter your own event!", Toast.LENGTH_LONG).show();
                    }
                    else {
                        event.joinWaitList(deviceId);
                        EventManager.getInstance().updateEvent(event);
                    }
                });
            }

        }
        else if (event.isRegistrationClosed() && event.isBeforeEventStart()) {
            // State: Registration is closed, but event hasn't started. Users can accept/decline invitations.
            if (event.inPendingList(deviceId)) {
                listStatusTextview.setText("You've Been Selected!");
                messageTextview.setText("Please respond to your invitation to attend.");
                acceptButton.setVisibility(View.VISIBLE);
                declineButton.setVisibility(View.VISIBLE);

                acceptButton.setOnClickListener(v -> {
                    event.joinAcceptedList(deviceId);
                    EventManager.getInstance().updateEvent(event);
                });
                declineButton.setOnClickListener(v -> {
                    event.joinDeclinedList(deviceId);
                    EventManager.getInstance().updateEvent(event);
                });
            }
            else if (event.inAcceptedList(deviceId)) {
                listStatusTextview.setText("Accepted");
                messageTextview.setText("You have successfully accepted the invitation. See you there!");
            }
            else if (event.inDeclinedList(deviceId)) {
                listStatusTextview.setText("Declined");
                messageTextview.setText("You have declined the invitation.");
            }
            else if (event.inWaitList(deviceId)) {
                listStatusTextview.setText("On the Waitlist");
                messageTextview.setText("You were not selected in the initial lottery. A spot may open up if others decline.");
            }
            else {
                // For users not on any list
                listStatusTextview.setText("Registration Closed");
                messageTextview.setText("The registration period for this event has ended.");
            }

        } else {
            // This final block handles both Ongoing and Completed events, as the lists are fixed.

            if (event.inAcceptedList(deviceId)) {
                listStatusTextview.setText("Accepted");
            }
            else if (event.inDeclinedList(deviceId)) {
                listStatusTextview.setText("Declined");
            }
            else if (event.inWaitList(deviceId)) {
                listStatusTextview.setText("Not Selected");
            }
            else {
                listStatusTextview.setText("Registration Closed");
            }

            // Set message text based on the event's progress
            if (event.isEventOngoing()) {
                messageTextview.setText("This event is currently in progress.");
            } else {
                // The event instance is no longer ongoing
                if (event.isReoccurring()) {
                    // For a reoccurring event, check if the entire series is over.
                    if (event.isEventComplete()) {
                        messageTextview.setText("This event series has ended.");
                    } else {
                        messageTextview.setText("This event has ended, but the series is still reoccurring.");
                    }
                } else {
                    // For a non-reoccurring event, it has simply ended.
                    messageTextview.setText("This event has ended.");
                }
            }
        }
    }
}
