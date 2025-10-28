package com.example.matrix_events.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matrix_events.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class EventSearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_search);

        // --- Start of Navigation Bar Logic ---
        navBarLogic();




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void navBarLogic() {
        // 1. Find the BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 2. Set the correct item as selected.
        bottomNavigationView.setSelectedItemId(R.id.nav_event_search);

        // 3. Set up the item selection listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // Prevent re-launching the same activity
            if (id == bottomNavigationView.getSelectedItemId()) {
                return true;
            }

            if (id == R.id.nav_qrcode) {
                startActivity(new Intent(getApplicationContext(), QRCodeActivity.class));
            } else if (id == R.id.nav_notifications) {
                startActivity(new Intent(getApplicationContext(), NotificationActivity.class));
            } else if (id == R.id.nav_event_search) {
                startActivity(new Intent(getApplicationContext(), EventSearchActivity.class));
            } else if (id == R.id.nav_my_events) {
                startActivity(new Intent(getApplicationContext(), EntrantMyEventsActivity.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }
            overridePendingTransition(0, 0);
            return true;
        });
    }
}