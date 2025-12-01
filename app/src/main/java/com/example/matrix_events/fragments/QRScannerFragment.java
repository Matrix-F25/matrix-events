package com.example.matrix_events.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.managers.EventManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class QRScannerFragment extends Fragment {

    private static final String TAG = "QRScannerFragment";

    private DecoratedBarcodeView barcodeView;
    private Button openEventButton;
    private Event currentValidEvent = null; // currently visible valid QR
    private boolean invalidToastShown = false;

    // Permission launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startScanning();
                } else {
                    Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_LONG).show();
                    getParentFragmentManager().popBackStack();
                }
            });

    public QRScannerFragment() {
        super(R.layout.fragment_qr_scanner);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barcodeView = view.findViewById(R.id.barcode_scanner);
        openEventButton = view.findViewById(R.id.open_event_button);
        openEventButton.setVisibility(View.GONE);

        openEventButton.setOnClickListener(v -> {
            if (currentValidEvent != null) {
                navigateToEvent(currentValidEvent);
            }
        });

        // Permission check
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startScanning() {
        if (barcodeView == null) return;

        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() == null) return;

                Event event = findEventByQRHash(result.getText());
                if (event != null) {
                    // Valid QR: show button and store current event
                    currentValidEvent = event;
                    openEventButton.setVisibility(View.VISIBLE);
                    invalidToastShown = false; // reset invalid toast flag
                } else {
                    // Invalid QR: hide button
                    currentValidEvent = null;
                    openEventButton.setVisibility(View.GONE);

                    if (!invalidToastShown) {
                        Toast.makeText(requireContext(), "Invalid QR code", Toast.LENGTH_SHORT).show();
                        invalidToastShown = true; // prevent multiple toasts
                    }
                }
            }
        });

        barcodeView.resume();
    }

    private Event findEventByQRHash(String qrHash) {
        List<Event> events = EventManager.getInstance().getEvents();
        for (Event event : events) {
            if (qrHash.equals(event.getQrCodeHash())) {
                return event;
            }
        }
        return null;
    }

    private void navigateToEvent(Event event) {
        EventDetailFragment fragment = EventDetailFragment.newInstance(event);

        int containerId = getParentFragmentManager().findFragmentById(R.id.scanner_container) != null
                ? R.id.scanner_container : R.id.main;

        getParentFragmentManager().beginTransaction()
                .replace(containerId, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        currentValidEvent = null;
        invalidToastShown = false;
        openEventButton.setVisibility(View.GONE);
        if (barcodeView != null) {
            barcodeView.resume();
            startScanning();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (barcodeView != null) barcodeView.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (barcodeView != null) barcodeView.pause();
        barcodeView = null;
    }
}
