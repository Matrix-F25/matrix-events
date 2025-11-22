package com.example.matrix_events.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;

public class NotificationCreateFragment extends Fragment {

    public static final String REQUEST_KEY = "notificationRequest";
    public static final String KEY_MESSAGE = "notificationMessage";

    public NotificationCreateFragment() {
        super(R.layout.fragment_notification_create);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backButton = view.findViewById(R.id.notif_create_cancel_button);
        backButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        EditText messageEditText = view.findViewById(R.id.notif_create_edittext);

        Button sendButton = view.findViewById(R.id.notif_create_send_msg_button);
        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString();
            if (message.isEmpty()) {
                Toast.makeText(requireContext(), "Cannot send empty message!", Toast.LENGTH_LONG).show();
            }
            else {
                Bundle result = new Bundle();
                result.putString(KEY_MESSAGE, message);
                getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
                getParentFragmentManager().popBackStack();
            }
        });
    }
}
