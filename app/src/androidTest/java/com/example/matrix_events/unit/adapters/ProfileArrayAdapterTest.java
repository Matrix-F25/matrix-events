package com.example.matrix_events.unit.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageButton;
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
 * Verifies that the adapter correctly binds data to the view and handles interactions.
 * Runs on an Android device/emulator to access real UI components.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
public class ProfileArrayAdapterTest {

    private Context context;
    private ArrayList<Profile> testData;
    private Profile profile1;

    @Before
    public void setUp() {
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // CRITICAL FIX: Use a Material Components Theme.
        // Since your layout likely uses MaterialButton or ShapeableImageView,
        // we wrap the context to ensure the required style attributes are present.
        context = new ContextThemeWrapper(targetContext, com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar);

        testData = new ArrayList<>();
        profile1 = new Profile("Adapter Test User", "adapter@test.com", "555-1234", "device_123");
        testData.add(profile1);
    }

    /**
     * Test A: Verify View Binding.
     * <p>
     * Ensures the adapter inflates the layout and sets the text correctly.
     * </p>
     */
    @Test
    public void testGetViewBinding() {
        ProfileArrayAdapter adapter = new ProfileArrayAdapter(context, testData, false, null);

        // Pass the themed context to the parent view as well to avoid warnings
        View view = adapter.getView(0, null, new android.widget.LinearLayout(context));

        TextView nameView = view.findViewById(R.id.item_profile_list_name_textview);
        assertNotNull("Name TextView should exist", nameView);
        assertEquals("Adapter Test User", nameView.getText().toString());
    }

    /**
     * Test B: Verify Cancel Button Visibility (Disabled).
     * <p>
     * Ensures the cancel button is hidden when the flag is false.
     * </p>
     */
    @Test
    public void testCancelButtonHidden() {
        ProfileArrayAdapter adapter = new ProfileArrayAdapter(context, testData, false, null);
        View view = adapter.getView(0, null, new android.widget.LinearLayout(context));

        ImageButton cancelButton = view.findViewById(R.id.item_profile_list_cancel_button);

        // Note: Our robust adapter handles null views gracefully, but for the test
        // we assert the button exists to prove the layout was inflated correctly.
        assertNotNull("Cancel button should exist in layout", cancelButton);
        assertEquals("Cancel button should be invisible", View.INVISIBLE, cancelButton.getVisibility());
    }

    /**
     * Test C: Verify Cancel Button Visibility (Enabled).
     * <p>
     * Ensures the cancel button is visible when the flag is true.
     * </p>
     */
    @Test
    public void testCancelButtonVisible() {
        ProfileArrayAdapter adapter = new ProfileArrayAdapter(context, testData, true, id -> {});
        View view = adapter.getView(0, null, new android.widget.LinearLayout(context));

        ImageButton cancelButton = view.findViewById(R.id.item_profile_list_cancel_button);
        assertNotNull("Cancel button should exist", cancelButton);
        assertEquals("Cancel button should be visible", View.VISIBLE, cancelButton.getVisibility());
    }

    /**
     * Test D: Verify Click Listener.
     * <p>
     * Ensures clicking the button triggers the callback with the correct Device ID.
     * </p>
     */
    @Test
    public void testCancelClickListener() {
        // Use AtomicBoolean to track if callback fired (thread-safe container)
        AtomicBoolean wasCalled = new AtomicBoolean(false);

        ProfileArrayAdapter.Listener listener = deviceId -> {
            wasCalled.set(true);
            assertEquals("device_123", deviceId);
        };

        ProfileArrayAdapter adapter = new ProfileArrayAdapter(context, testData, true, listener);
        View view = adapter.getView(0, null, new android.widget.LinearLayout(context));

        ImageButton cancelButton = view.findViewById(R.id.item_profile_list_cancel_button);
        assertNotNull("Cancel button should exist", cancelButton);

        cancelButton.performClick();

        assertTrue("Listener callback should have been triggered", wasCalled.get());
    }
}