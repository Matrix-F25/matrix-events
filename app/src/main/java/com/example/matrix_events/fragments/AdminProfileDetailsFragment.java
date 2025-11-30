package com.example.matrix_events.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.matrix_events.R;
import com.example.matrix_events.adapters.EventArrayAdapter;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.managers.EventManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class AdminProfileDetailsFragment extends Fragment implements com.example.matrix_events.mvc.View {
    private Profile profile;
    private static final String ARG_PROFILE = "profile";

    private ArrayList<Event> entrantEvents = new ArrayList<>();
    private ArrayList<Event> organizerEvents = new ArrayList<>();
    private ListView eventsListView;
    private EventArrayAdapter eventArrayAdapter;
    private TextView noEventsText;
    private int currentTabPosition = 0;

    public AdminProfileDetailsFragment() {
    }

    public static AdminProfileDetailsFragment newInstance(Profile profile) {
        AdminProfileDetailsFragment fragment = new AdminProfileDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PROFILE, profile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            profile = (Profile) getArguments().getSerializable(ARG_PROFILE);
        }

        EventManager.getInstance().addView(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (profile == null) return;

        ImageView profileImage = view.findViewById(R.id.detail_profile_image);
        TextView nameText = view.findViewById(R.id.detail_name);
        TextView emailText = view.findViewById(R.id.detail_email);
        TextView phoneText = view.findViewById(R.id.detail_phone);
        TextView joinedText = view.findViewById(R.id.detail_joined);

        eventsListView = view.findViewById(R.id.detail_events_listview);
        noEventsText = view.findViewById(R.id.empty_events_text);

        TabLayout eventTabLayout = view.findViewById(R.id.event_tabs);
        Button backButton = view.findViewById(R.id.back_button);

        // back button to get out of details view
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        nameText.setText(profile.getName());
        emailText.setText(profile.getEmail());

        // first check if the phone number is null or empty
        if (profile.getPhoneNumber() != null && !profile.getPhoneNumber().isEmpty()) {
            phoneText.setText(profile.getPhoneNumber()); // if not, set it
        } else {
            phoneText.setText("User did not provide a number"); // otherwise, show this message
        }

        // implement join date/time later (or idk, maybe I wont even do it)
        joinedText.setVisibility(View.GONE);

        // profile picture stuff
        // sets it to their profile picture if they have one
        if (profile.getProfilePictureUrl() != null && !profile.getProfilePictureUrl().isEmpty()) {
            Glide.with(this)
                    .load(profile.getProfilePictureUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_profile)
                    .into(profileImage);
        } else { // otherwise, use a default picture
            Glide.with(this)
                    .load(R.drawable.ic_profile)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileImage);
        }

        currentTabPosition = 0;
        update();

        eventTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabPosition = tab.getPosition();
                update();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventManager.getInstance().removeView(this);
    }

    @Override
    public void update() {

        entrantEvents.clear();
        organizerEvents.clear();

        // get the updated data
        List<Event> allEvents = EventManager.getInstance().getEvents();
        String deviceId = profile.getDeviceId();

        for (Event event : allEvents) {
            // check for organizer
            if (event.getOrganizer().getDeviceId().equals(deviceId)) {
                organizerEvents.add(event);
            }
            // check for entrant
            else {
                if (event.inWaitList(deviceId) || event.inPendingList(deviceId) ||
                        event.inAcceptedList(deviceId) || event.inDeclinedList(deviceId)) {
                    entrantEvents.add(event);
                }
            }
        }

        ArrayList<Event> eventsToShow;
        if (currentTabPosition == 0) {
            eventsToShow = entrantEvents;
        } else {
            eventsToShow = organizerEvents;
        }

        if (eventsToShow.isEmpty()) {

            // if there are no events, show a message
            eventsListView.setVisibility(View.GONE);
            noEventsText.setVisibility(View.VISIBLE);

            if (currentTabPosition == 0) {
                noEventsText.setText("No events joined.");
            } else {
                noEventsText.setText("No events organized.");
            }
        } else { // otherwise, show the events
            eventsListView.setVisibility(View.VISIBLE);
            noEventsText.setVisibility(View.GONE);

            eventArrayAdapter = new EventArrayAdapter(getContext(), eventsToShow, false, null) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView titleView = view.findViewById(R.id.title_textview);
                    Event event = getItem(position);
                    String profileId = profile.getDeviceId();

                    String status = "";
                    if (event != null) {
                        if (event.getOrganizer().getDeviceId().equals(profileId)) {
                            status = "";
                        } else if (event.inWaitList(profileId)) {
                            status = " (Waitlist)";
                        } else if (event.inPendingList(profileId)) {
                            status = " (Invited)";
                        } else if (event.inAcceptedList(profileId)) {
                            status = " (Accepted)";
                        } else if (event.inDeclinedList(profileId)) {
                            status = " (Declined)";
                        }

                        titleView.setText(event.getName() + status);
                    }
                    return view;
                }
            };
            eventsListView.setAdapter(eventArrayAdapter);
        }
    }
}