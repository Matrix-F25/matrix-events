package com.example.matrix_events.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.ProfileArrayAdapter;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Notification;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.managers.NotificationManager;
import com.example.matrix_events.managers.ProfileManager;
import com.google.firebase.Timestamp;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventEntrantListFragment extends Fragment implements com.example.matrix_events.mvc.View, ProfileArrayAdapter.Listener {

    public enum ListType {
        WAITING_LIST,
        PENDING_LIST,
        ACCEPTED_LIST,
        DECLINED_LIST
    }
    View view;
    private Event event;
    private ListType listType;
    private ArrayList<Profile> profileArray;
    private ProfileArrayAdapter profileAdapter;
    private ActivityResultLauncher<Intent> downloadCSVLauncher;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public EventEntrantListFragment() {
        super(R.layout.fragment_event_entrant_list);
    }

    public static EventEntrantListFragment newInstance(Event event, ListType listType) {
        EventEntrantListFragment fragment = new EventEntrantListFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        args.putSerializable("listType", listType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        downloadCSVLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            writeCSVToURI(uri);
                        } else {
                            // handle case where URI is null (e.g. user canceled after initial selection)
                            Toast.makeText(requireContext(), "File location selection canceled.", Toast.LENGTH_SHORT).show();
                        }
                    } else if (result.getResultCode() != Activity.RESULT_CANCELED) {
                        // handle other non-OK results
                        Toast.makeText(requireContext(), "File creation failed or canceled.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
            listType = (ListType) getArguments().getSerializable("listType");
        }
        assert event != null;
        assert listType != null;

        boolean cancelEnabled = listType == ListType.PENDING_LIST;

        profileArray = new ArrayList<>();
        profileAdapter = new ProfileArrayAdapter(requireContext(), profileArray, cancelEnabled, this);
        ListView profileListView = view.findViewById(R.id.ent_list_listview);
        profileListView.setAdapter(profileAdapter);

        Button backButton = view.findViewById(R.id.ent_list_back_button);
        backButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        Button messageButton = view.findViewById(R.id.ent_list_message_button);
        messageButton.setOnClickListener(v -> {
            if (profileArray.isEmpty()) {
                Toast.makeText(requireContext(), "No entrants in list!", Toast.LENGTH_LONG).show();
            }
            else {
                NotificationCreateFragment fragment = new NotificationCreateFragment();
                getParentFragmentManager().beginTransaction()
                        .add(R.id.main, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        getParentFragmentManager().setFragmentResultListener(NotificationCreateFragment.REQUEST_KEY, this, ((requestKey, result) -> {
            String message = result.getString(NotificationCreateFragment.KEY_MESSAGE);
            if (message != null) {
                createNotification(message);
            }
        }));

        Button downloadCSVButton = view.findViewById(R.id.ent_list_download_button);
        downloadCSVButton.setOnClickListener(v -> {
            Log.d("DEBUG", "Starting list CSV download");
            if (profileArray.isEmpty()) {
                Toast.makeText(requireContext(), "No entrants in list!", Toast.LENGTH_LONG).show();
            }
            else {
                createCSVFile();
            }
        });

        update();

        // observe event manager and profile manager
        EventManager.getInstance().addView(this);
        ProfileManager.getInstance().addView(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventManager.getInstance().removeView(this);
        ProfileManager.getInstance().removeView(this);
        executorService.shutdownNow();
    }

    private void createCSVFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");

        String timestamp = String.valueOf(System.currentTimeMillis());
        String filename = "Entrants_" + listType.toString() + "_" + timestamp + ".csv";

        intent.putExtra(Intent.EXTRA_TITLE, filename);
        downloadCSVLauncher.launch(intent);
    }

    private void writeCSVToURI(Uri uri) {
        final ArrayList<Profile> dataToWrite = new ArrayList<>(profileArray);
        final Context applicationContext = requireContext().getApplicationContext();

        executorService.execute(() -> {
            try {
                try (OutputStream outputStream = applicationContext.getContentResolver().openOutputStream(uri)) {
                    if (outputStream == null) {
                        throw new Exception("Failed to open output stream.");
                    }

                    StringBuilder csvContent = new StringBuilder();
                    csvContent.append("Name,Email,Phone Number\n");

                    for (Profile profile : dataToWrite) {
                        String name = escapeCSVField(profile.getName());
                        String email = escapeCSVField(profile.getEmail());
                        String phone = escapeCSVField(profile.getPhoneNumber());

                        csvContent.append(name).append(",")
                                .append(email).append(",")
                                .append(phone).append("\n");
                    }

                    outputStream.write(csvContent.toString().getBytes());
                    mainThreadHandler.post(() -> {
                        Toast.makeText(applicationContext, "Export successful!", Toast.LENGTH_LONG).show();
                    });

                }
            } catch (Exception e) {
                Log.e("EventEntrantList", "Error writing CSV file", e);
                mainThreadHandler.post(() -> {
                    Toast.makeText(applicationContext, "Error saving file: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private String escapeCSVField(String field) {
        if (field == null) {
            return "";
        }
        // if the field contains a comma, newline, or double quote, wrap it in double quotes.
        if (field.contains(",") || field.contains("\n") || field.contains("\"")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    @Override
    public void update() {
        event = EventManager.getInstance().getEventByDBID(event.getId());
        if (event != null) {
            render();
        }
    }

    @Override
    public void cancelProfile(String deviceID) {
        event.joinDeclinedList(deviceID);
        EventManager.getInstance().updateEvent(event);
        Toast.makeText(requireContext(), "Entrant successfully removed!", Toast.LENGTH_LONG).show();
    }

    public void createNotification(String message) {
        Profile sender = event.getOrganizer();
        Timestamp now = Timestamp.now();
        for (Profile receiver : profileArray) {
            Notification notification = new Notification(sender, receiver, message, Notification.NotificationType.ORGANIZER, now);
            NotificationManager.getInstance().createNotification(notification);
        }
        Toast.makeText(requireContext(), "Message sent to entrants!", Toast.LENGTH_LONG).show();
    }

    public void render() {
        TextView listTitleTextview = view.findViewById(R.id.ent_list_title_textview);
        ArrayList<String> profileStringArray = new ArrayList<>();
        switch (listType) {
            case WAITING_LIST: {
                listTitleTextview.setText("Waiting List");
                profileStringArray = (ArrayList<String>) event.getWaitList();
                break;
            }
            case PENDING_LIST: {
                listTitleTextview.setText("Pending List");
                profileStringArray = (ArrayList<String>) event.getPendingList();
                break;
            }
            case ACCEPTED_LIST: {
                listTitleTextview.setText("Accepted List");
                profileStringArray = (ArrayList<String>) event.getAcceptedList();
                break;
            }
            case DECLINED_LIST: {
                listTitleTextview.setText("Declined List");
                profileStringArray = (ArrayList<String>) event.getDeclinedList();
                break;
            }
        }

        profileArray.clear();
        for (String deviceID : profileStringArray) {
            Profile profile = ProfileManager.getInstance().getProfileByDeviceId(deviceID);
            if (profile != null) {
                profileArray.add(profile);
            }
        }
        profileAdapter.notifyDataSetChanged();
    }
}