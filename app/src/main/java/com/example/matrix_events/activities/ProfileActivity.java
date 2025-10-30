package com.example.matrix_events.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matrix_events.R;
import com.example.matrix_events.fragments.NavigationBarFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    // Declarations
    private TextInputEditText profile_name;
    private TextInputEditText profile_email;
    private TextInputEditText profile_phone;
    private MaterialButton update_button;
    private ImageButton settings_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, NavigationBarFragment.newInstance(R.id.nav_profile))
                .commit();

        // Initializations
        profile_name = findViewById(R.id.profile_name);
        profile_email = findViewById(R.id.profile_email);
        profile_phone = findViewById(R.id.profile_phone);

        update_button = findViewById(R.id.profile_update_button);
        settings_button = findViewById(R.id.profile_settings_button);

        String name = profile_name.getText().toString();
        String email = profile_email.getText().toString();

        update_button.setOnClickListener() {

        }

        settings_button.setOnClickListener() {

        }
    }
}