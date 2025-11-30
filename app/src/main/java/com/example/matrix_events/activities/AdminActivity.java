package com.example.matrix_events.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matrix_events.R;
import com.example.matrix_events.fragments.AdminNavigationBarFragment;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Go to the Organizer "My Events" Activity
        Button switchToOrganizerButton = findViewById(R.id.admin_switch_to_org_button);
        switchToOrganizerButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, OrganizerMyEventsActivity.class);
            startActivity(intent);
            finish();
        });

        // Admin Navigation Bar Fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.admin_navigation_bar_fragment, new AdminNavigationBarFragment())
                    .commit();
        }
    }
}
