package com.example.matrix_events.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.matrix_events.R;
import com.example.matrix_events.entities.Notification;
import com.google.android.material.chip.Chip;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class NotificationSeeMore extends Fragment {

    private static final String ARG_NOTIFICATION = "notification";
    private Notification notification;

    public static NotificationSeeMore newInstance(Notification notification) {
        NotificationSeeMore fragment = new NotificationSeeMore();
        Bundle args = new Bundle();
        args.putSerializable(ARG_NOTIFICATION, notification);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            notification = (Notification) getArguments().getSerializable(ARG_NOTIFICATION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_see_more, container, false);

        if (notification != null) {
            TextView titleTextView = view.findViewById(R.id.text_message_title);
            TextView bodyTextView = view.findViewById(R.id.text_message_body);
            Chip dateChip = view.findViewById(R.id.chip_date);
            Chip timeChip = view.findViewById(R.id.chip_time);
            ImageButton closeButton = view.findViewById(R.id.button_close);

            String title = "New message from: " + notification.getSender().getName() + " (Organizer)";
            titleTextView.setText(title);
            bodyTextView.setText(notification.getMessage());

            Timestamp timestamp = notification.getTimestamp();
            if (timestamp != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                String formattedTime = timeFormat.format(timestamp.toDate());

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                String formattedDate = dateFormat.format(timestamp.toDate());

                dateChip.setText(formattedDate);
                timeChip.setText(formattedTime);
            }

            closeButton.setOnClickListener(v -> {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            });
        }

        return view;
    }
}
