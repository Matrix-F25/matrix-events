package com.example.matrix_events.unit.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.ProfileArrayAdapter;
import com.example.matrix_events.entities.Profile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Instrumented Unit Tests for {@link ProfileArrayAdapter}.
 * <p>
 * This suite verifies the view binding and interaction logic for the Profile list adapter.
 * It specifically checks the "Cancel" button logic, which is conditional based on the
 * adapter's configuration.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
public class ProfileArrayAdapterTest {

    private Context context;
    private ArrayList<Profile> testData;
    private Profile testProfile;
    private static final String TEST_DEVICE_ID = "device_12345";

    @Before
    public void setUp() {
        // Use Material Theme context to ensure layout inflation works for all components
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context = new ContextThemeWrapper(targetContext, com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar);

        testData = new ArrayList<>();

        // Create a dummy profile
        testProfile = new Profile("Alice Tester", "alice@example.com", "555-0100", TEST_DEVICE_ID);
        testData.add(testProfile);
    }

    /**
     * Test A: Verify Basic View Binding.
     * <p>
     * Ensures the Profile Name is set correctly on the TextView.
     * </p>
     */
    @Test
    public void testViewBinding() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            // Initialize adapter (cancel disabled for basic test)
            ProfileArrayAdapter adapter = new ProfileArrayAdapter(context, testData, false, null);
            View view = adapter.getView(0, null, new LinearLayout(context));

            TextView nameView = view.findViewById(R.id.item_profile_list_name_textview);

            assertNotNull("Name TextView should exist", nameView);
            assertEquals("Alice Tester", nameView.getText().toString());
        });
    }

    /**
     * Test B: Verify Cancel Button Disabled State.
     * <p>
     * <b>Scenario:</b> Adapter initialized with {@code cancelEnabled = false}.<br>
     * <b>Expected:</b> Cancel button is INVISIBLE.
     * </p>
     */
    @Test
    public void testCancelButtonDisabled() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            ProfileArrayAdapter adapter = new ProfileArrayAdapter(context, testData, false, null);
            View view = adapter.getView(0, null, new LinearLayout(context));

            ImageButton cancelButton = view.findViewById(R.id.item_profile_list_cancel_button);

            assertNotNull("Cancel button should exist in layout", cancelButton);
            // Verify default state from your code logic (View.INVISIBLE)
            assertEquals("Cancel button should be INVISIBLE when disabled", View.INVISIBLE, cancelButton.getVisibility());
        });
    }

    /**
     * Test C: Verify Cancel Button Enabled State.
     * <p>
     * <b>Scenario:</b> Adapter initialized with {@code cancelEnabled = true}.<br>
     * <b>Expected:</b> Cancel button is VISIBLE.
     * </p>
     */
    @Test
    public void testCancelButtonEnabled() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            // Pass a dummy listener so it's not null (though adapter handles null check implicitly via logic flow)
            ProfileArrayAdapter adapter = new ProfileArrayAdapter(context, testData, true, id -> {});
            View view = adapter.getView(0, null, new LinearLayout(context));

            ImageButton cancelButton = view.findViewById(R.id.item_profile_list_cancel_button);

            assertNotNull("Cancel button should exist", cancelButton);
            assertEquals("Cancel button should be VISIBLE when enabled", View.VISIBLE, cancelButton.getVisibility());
        });
    }

    /**
     * Test D: Verify Cancel Interaction.
     * <p>
     * <b>Scenario:</b> User clicks the visible cancel button.<br>
     * <b>Expected:</b> The listener callback is fired with the correct Device ID.
     * </p>
     */
    @Test
    public void testCancelInteraction() {
        AtomicBoolean callbackFired = new AtomicBoolean(false);

        // Define listener to capture the click event
        ProfileArrayAdapter.Listener listener = deviceId -> {
            callbackFired.set(true);
            assertEquals("Callback should return the correct Device ID", TEST_DEVICE_ID, deviceId);
        };

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            // Initialize enabled adapter
            ProfileArrayAdapter adapter = new ProfileArrayAdapter(context, testData, true, listener);
            View view = adapter.getView(0, null, new LinearLayout(context));

            ImageButton cancelButton = view.findViewById(R.id.item_profile_list_cancel_button);

            // Perform Click
            cancelButton.performClick();
        });

        assertTrue("Cancel listener should be triggered", callbackFired.get());
    }
}