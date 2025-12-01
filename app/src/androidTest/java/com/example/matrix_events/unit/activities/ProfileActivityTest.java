package com.example.matrix_events.unit.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.provider.Settings;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.matrix_events.R;
import com.example.matrix_events.activities.ProfileActivity;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.managers.ProfileManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

/**
 * UI/Integration Tests for {@link ProfileActivity}.
 * <p>
 * This class uses Espresso to verify User Stories related to viewing and editing profile information.
 * It interacts with the real Firestore database to ensure the UI correctly reflects
 * data changes managed by {@link ProfileManager}.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
public class ProfileActivityTest {

    // Automatically launches the Activity before each test
    @Rule
    public ActivityScenarioRule<ProfileActivity> activityRule =
            new ActivityScenarioRule<>(ProfileActivity.class);

    private ProfileManager profileManager;
    private Profile testProfile;
    private String deviceId;

    /**
     * Sets up the test environment.
     * <p>
     * 1. Initializes the ProfileManager.<br>
     * 2. Retrieves the emulator's actual Device ID.<br>
     * 3. Creates a dummy Profile in Firestore so the Activity has data to display.
     * </p>
     */
    @Before
    public void setUp() throws InterruptedException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        profileManager = ProfileManager.getInstance();

        // Create a known profile to test against
        testProfile = new Profile("Espresso User", "espresso@test.com", "555-0000", deviceId);

        // We must wait for the creation to finish before running UI checks
        CountDownLatch latch = new CountDownLatch(1);

        // We assume createProfile is async; we just fire it.
        // In a real scenario, we might want a callback, but for this test,
        // we rely on the Activity's MVC observation to pick up the change eventually.
        // However, to be safe, we perform a synchronous-like wait if possible,
        // or just sleep briefly to let Firestore sync.
        profileManager.createProfile(testProfile);

        // Give Firestore a moment to sync before the test starts interacting with UI
        Thread.sleep(2000);
    }

    /**
     * User Story: View Profile Information.
     * <p>
     * <b>Scenario:</b> User opens the Profile screen.<br>
     * <b>Expected Result:</b> The user's name, email, and phone number are displayed in the corresponding input fields.
     * </p>
     */
    @Test
    public void testProfileDataDisplayed() {
        // Check if the Name field contains "Espresso User"
        onView(ViewMatchers.withId(R.id.profile_name))
                .check(matches(isDisplayed()))
                .check(matches(withText("Espresso User")));

        // Check if the Email field contains the email
        onView(withId(R.id.profile_email))
                .check(matches(withText("espresso@test.com")));

        // Check if the Phone field contains the phone
        onView(withId(R.id.profile_phone_number))
                .check(matches(withText("555-0000")));
    }

    /**
     * User Story: Update Profile Information.
     * <p>
     * <b>Scenario:</b> User edits their name and clicks the "Update" button.<br>
     * <b>Expected Result:</b> The UI retains the new value, and (implicitly) the database is updated.
     * </p>
     */
    @Test
    public void testUpdateProfile() throws InterruptedException {
        String newName = "Updated Espresso User";

        // 1. Clear existing text and type new name
        onView(withId(R.id.profile_name))
                .perform(clearText(), typeText(newName), closeSoftKeyboard());

        // 2. Click the Update button
        onView(withId(R.id.profile_update_button))
                .perform(click());

        // 3. Wait briefly for the update round-trip
        Thread.sleep(1000);

        // 4. Verify the UI still shows the new name (confirming it didn't revert)
        onView(withId(R.id.profile_name))
                .check(matches(withText(newName)));
    }

    /**
     * Edge Case: Update Profile with Empty Phone Number.
     * <p>
     * <b>Scenario:</b> User clears the phone number field completely and clicks Update.<br>
     * <b>Expected Result:</b> The system handles the empty string (converting to null internally per Activity logic)
     * and the UI displays an empty field without crashing or showing "null".
     * </p>
     */
    @Test
    public void testUpdateWithEmptyPhoneNumber() throws InterruptedException {
        // 1. Clear phone number
        onView(withId(R.id.profile_phone_number))
                .perform(clearText(), closeSoftKeyboard());

        // 2. Click Update
        onView(withId(R.id.profile_update_button))
                .perform(click());

        // 3. Wait for update
        Thread.sleep(1000);

        // 4. Verify field is visually empty
        onView(withId(R.id.profile_phone_number))
                .check(matches(withText("")));
    }

    /**
     * Edge Case: Update Profile with Empty Name.
     * <p>
     * <b>Scenario:</b> User clears the mandatory Name field and attempts to update.<br>
     * <b>Expected Result:</b> The system allows the update (or at least does not crash),
     * demonstrating the current behavior for empty mandatory fields.
     * </p>
     */
    @Test
    public void testUpdateWithEmptyName() throws InterruptedException {
        // 1. Clear name field
        onView(withId(R.id.profile_name))
                .perform(clearText(), closeSoftKeyboard());

        // 2. Click Update
        onView(withId(R.id.profile_update_button))
                .perform(click());

        // 3. Wait for update
        Thread.sleep(1000);

        // 4. Verify field is visually empty
        onView(withId(R.id.profile_name))
                .check(matches(withText("")));
    }

    /**
     * User Story: Navigate to Settings.
     * <p>
     * <b>Scenario:</b> User clicks the Settings gear icon.<br>
     * <b>Expected Result:</b> The Settings fragment container is displayed (verifying navigation logic).
     * </p>
     */
    @Test
    public void testNavigationToSettings() {
        // Check if settings button is visible and click it
        onView(withId(R.id.profile_settings_button))
                .check(matches(isDisplayed()))
                .perform(click());

        // Verify that the fragment container is visible
        // (This implies the fragment transaction occurred)
        onView(withId(R.id.fragment_container))
                .check(matches(isDisplayed()));
    }

    /**
     * Cleanup after tests.
     * <p>
     * Deletes the dummy profile from Firestore to prevent test data pollution.
     * </p>
     */
    @After
    public void tearDown() {
        if (testProfile != null) {
            // Need to set the ID because createProfile might not have set it on our local object immediately
            // Fetch the latest version to ensure we have the ID for deletion
            Profile p = profileManager.getProfileByDeviceId(deviceId);
            if (p != null) {
                profileManager.deleteProfile(p);
            }
        }
    }
}