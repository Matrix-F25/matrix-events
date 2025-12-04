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

/**
 * Activity responsible for hosting the QR Code scanning functionality.
 * <p>
 * This activity acts as a container for the {@link QRScannerFragment}, which handles the actual
 * camera and scanning logic. It manages the visibility of the bottom navigation bar, ensuring it is
 * visible during scanning but hidden when the user navigates deeper into a sub-fragment
 * (e.g., viewing event details after a successful scan).
 * </p>
 */
public class QRCodeActivity extends AppCompatActivity {

    private FragmentContainerView navigationBarContainer;

    /**
     * Called when the activity is starting.
     * <p>
     * Initializes the UI layout, applies window insets for edge-to-edge display, and performs two main tasks:
     * <ol>
     * <li>Loads the {@link NavigationBarFragment} and the {@link QRScannerFragment}.</li>
     * <li>Registers a {@link androidx.fragment.app.FragmentManager.OnBackStackChangedListener} to toggle
     * the navigation bar's visibility. If the back stack has entries (meaning a detail view is open),
     * the nav bar is hidden to provide more screen space.</li>
     * </ol>
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied.
     */
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

    /**
     * Hides the bottom navigation bar container.
     * <p>
     * Called when the user navigates away from the root scanner view to a detail view.
     * </p>
     */
    private void hideNavigationBar() {
        if (navigationBarContainer != null) {
            navigationBarContainer.setVisibility(View.GONE);
        }
    }

    /**
     * Shows the bottom navigation bar container.
     * <p>
     * Called when the user returns to the root scanner view.
     * </p>
     */
    private void showNavigationBar() {
        if (navigationBarContainer != null) {
            navigationBarContainer.setVisibility(View.VISIBLE);
        }
    }
}