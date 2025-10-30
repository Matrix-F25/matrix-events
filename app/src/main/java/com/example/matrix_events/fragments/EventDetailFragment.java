package com.example.matrix_events.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.managers.EventManager;

public class EventDetailFragment extends Fragment {

    //idk whats happening here this is just temp

    private static final String ARG_EVENT_ID = "event_id";

    private String eventId;

    public static EventDetailFragment newInstance(String eventId) {
        EventDetailFragment fragment = new EventDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Event event = EventManager.getInstance().getEvent(eventId);

        if (event != null) {
            TextView eventName = view.findViewById(R.id.event_details_title_placeholder);
            TextView eventDescription = view.findViewById(R.id.event_description_placeholder);

            eventName.setText(event.getName());
            eventDescription.setText(event.getDescription());
        }
    }
}
