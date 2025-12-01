package com.example.matrix_events.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.utils.QRCodeGenerator;
import com.google.zxing.WriterException;


public class EventQRCodeFragment extends Fragment {
    private static final String TAG = "EventQRCodeFragment";
    private Event event;

    public EventQRCodeFragment() {
        super(R.layout.fragment_event_qr_code);
    }

    public static EventQRCodeFragment newInstance(Event event) {
        EventQRCodeFragment fragment = new EventQRCodeFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }

        if (event == null || event.getQrCodeHash() == null) {
            Toast.makeText(requireContext(), "Error: Event QR code not available", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return;
        }

        Button backButton = view.findViewById(R.id.qr_back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        TextView eventNameTextView = view.findViewById(R.id.qr_event_name);
        eventNameTextView.setText(event.getName());

        ImageView qrCodeImageView = view.findViewById(R.id.qr_code_image);

        // Generate and display QR code
        try {
            Bitmap qrBitmap = QRCodeGenerator.generateQRCodeBitmap(event.getQrCodeHash(), 512, 512);
            qrCodeImageView.setImageBitmap(qrBitmap);
        } catch (WriterException e) {
            Log.e(TAG, "Error generating QR code", e);
            Toast.makeText(requireContext(), "Error generating QR code", Toast.LENGTH_SHORT).show();
        }
    }
}