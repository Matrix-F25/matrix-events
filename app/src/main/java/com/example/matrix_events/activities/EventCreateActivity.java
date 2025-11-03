package com.example.matrix_events.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.matrix_events.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EventCreateActivity extends AppCompatActivity {

    // Views (IDs match your XML)
    private TextInputEditText inputEventName;    // @+id/input_event_name
    private TextInputEditText etDescription;     // @+id/etDescription
    private EditText etStartDate;                // @+id/editTextStartDate
    private EditText etStartTime;                // @+id/editTextStartTime
    private EditText etEndTime;                  // @+id/editTextEndTime
    private EditText etCapacity;                 // @+id/editTextEventCapacity
    private EditText etWaitlistCap;              // @+id/editTextWaitlistCapacity
    private EditText etRegStartDate;             // @+id/editTextRegistrationStartDate
    private EditText etRegEndDate;               // @+id/editTextRegistrationEndDate
    private EditText etLocation;                 // @+id/editTextLocation
    private Button btnCreateEvent;               // @+id/btn_createEvent_org

    private FirebaseFirestore db;

    // Formats
    private final SimpleDateFormat DATE = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat DATE_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.fragment_event_create);

        bindViews();
        db = FirebaseFirestore.getInstance();

        setupPickers();

        btnCreateEvent.setOnClickListener(v -> saveEvent());
    }

    private void bindViews() {
        inputEventName = findViewById(R.id.input_event_name);
        etDescription  = findViewById(R.id.etDescription);
        etStartDate    = findViewById(R.id.editTextStartDate);
        etStartTime    = findViewById(R.id.editTextStartTime);
        etEndTime      = findViewById(R.id.editTextEndTime);
        etCapacity     = findViewById(R.id.editTextEventCapacity);
        etWaitlistCap  = findViewById(R.id.editTextWaitlistCapacity);
        etRegStartDate = findViewById(R.id.editTextRegistrationStartDate);
        etRegEndDate   = findViewById(R.id.editTextRegistrationEndDate);
        etLocation     = findViewById(R.id.editTextLocation);
        btnCreateEvent = findViewById(R.id.btn_createEvent_org);
    }

    private void setupPickers() {
        // Start Date picker
        etStartDate.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) ->
                    etStartDate.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH),
                    cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });

        // Start Time picker (24h)   // help of chat gpt
        etStartTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, h, min) ->
                    etStartTime.setText(String.format(Locale.US, "%02d:%02d", h, min)),
                    9, 0, true).show();
        });

        // End Time picker (24h)    // help of chat gpt
        etEndTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, h, min) ->
                    etEndTime.setText(String.format(Locale.US, "%02d:%02d", h, min)),
                    10, 0, true).show();
        });

        // Registration Start / End as date pickers (optional fields)    // help of chat gpt
        etRegStartDate.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) ->
                    etRegStartDate.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH),
                    cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });

        etRegEndDate.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) ->
                    etRegEndDate.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH),
                    cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void saveEvent() {
        // Gather inputs
        String name         = textOf(inputEventName);
        String desc         = textOf(etDescription);
        String startDateStr = textOf(etStartDate);
        String startTimeStr = textOf(etStartTime);
        String endTimeStr   = textOf(etEndTime);
        String capStr       = textOf(etCapacity);
        String waitCapStr   = textOf(etWaitlistCap);
        String regStartStr  = textOf(etRegStartDate);
        String regEndStr    = textOf(etRegEndDate);
        String location     = textOf(etLocation);

        // Validate required
        if (TextUtils.isEmpty(name)) { toast("Please enter an event name"); return; }
        if (TextUtils.isEmpty(startDateStr) || TextUtils.isEmpty(startTimeStr)) { toast("Enter start date and time"); return; }
        if (TextUtils.isEmpty(endTimeStr)) { toast("Enter end time"); return; }
        if (TextUtils.isEmpty(capStr)) { toast("Enter event capacity"); return; }
        if (TextUtils.isEmpty(waitCapStr)) { waitCapStr = "0"; }

        // Parse numeric
        int capacity, waitlistCapacity;
        try {
            capacity = Integer.parseInt(capStr);
            waitlistCapacity = Integer.parseInt(waitCapStr);
        } catch (NumberFormatException e) {
            toast("Capacity values must be numbers");
            return;
        }

        // Parse date/times → millis
        Long startMillis = tryParseDateTime(startDateStr, startTimeStr);
        Long endMillis   = tryParseDateTime(startDateStr, endTimeStr);
        Long regStartMs  = tryParseDate(regStartStr);
        Long regEndMs    = tryParseDate(regEndStr);

        if (startMillis == null || endMillis == null) {
            toast("Invalid date/time. Use yyyy-MM-dd and HH:mm.");
            return;
        }
        if (endMillis < startMillis) {
            toast("End time must be after start time");
            return;
        }
        if (regStartMs != null && regEndMs != null && regEndMs < regStartMs) {
            toast("Registration end must be after start");
            return;
        }

        // Build Firestore doc
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("description", desc);
        data.put("startTime", startMillis);
        data.put("endTime", endMillis);
        data.put("capacity", capacity);
        data.put("waitlistCapacity", waitlistCapacity);
        data.put("registrationStart", regStartMs);
        data.put("registrationEnd", regEndMs);
        data.put("location", location);
        data.put("status", "OPEN");
        data.put("createdAt", System.currentTimeMillis());

        // UX: prevent double taps
        btnCreateEvent.setEnabled(false);
        CharSequence oldText = btnCreateEvent.getText();
        btnCreateEvent.setText("Saving…");

        db.collection("events")
                .add(data)
                .addOnSuccessListener(ref -> {
                    toast("Event created!");
                    finish(); // back to organizer screen
                })
                .addOnFailureListener(e -> {
                    btnCreateEvent.setEnabled(true);
                    btnCreateEvent.setText(oldText);
                    toast("Error: " + e.getMessage());
                });
    }

    // ---- Helpers ----  // help of chat GPT
    private Long tryParseDate(String s) {
        if (TextUtils.isEmpty(s)) return null;
        try { return DATE.parse(s).getTime(); }
        catch (ParseException e) { return null; }
    }
    private Long tryParseDateTime(String date, String time) {
        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) return null;
        try { return DATE_TIME.parse(date + " " + time).getTime(); }
        catch (ParseException e) { return null; }
    }

    private String textOf(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
    private String textOf(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
