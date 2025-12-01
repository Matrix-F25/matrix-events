package com.example.matrix_events.activities;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentContainerView;

import com.example.matrix_events.R;
import com.example.matrix_events.fragments.NavigationBarFragment;
import com.example.matrix_events.fragments.QRScannerFragment;

public class QRCodeActivity extends AppCompatActivity {

    private FragmentContainerView navigationBarContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qrcode);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        navigationBarContainer = findViewById(R.id.navigation_bar_fragment);

        // Load navigation bar
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, NavigationBarFragment.newInstance(R.id.nav_qrcode))
                .commit();

        // Load QR scanner fragment into scanner container
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.scanner_container, new QRScannerFragment())
                    .commit();
        }

        // Listen for back stack changes to show/hide navigation bar
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            // If navigated to event details (back stack has entries), hide nav bar
            // Else back to the scanner (back stack is empty), show nav bar
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                hideNavigationBar();
            } else {
                showNavigationBar();
            }
        });
    }

    private void hideNavigationBar() {
        if (navigationBarContainer != null) {
            navigationBarContainer.setVisibility(View.GONE);
        }
    }

    private void showNavigationBar() {
        if (navigationBarContainer != null) {
            navigationBarContainer.setVisibility(View.VISIBLE);
        }
    }
}