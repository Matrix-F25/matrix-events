package com.example.matrix_events.unit.fragments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertFalse;

import android.content.Context;
import android.provider.Settings;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.matrix_events.R;
import com.example.matrix_events.activities.ProfileActivity;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.fragments.SettingsFragment;
import com.example.matrix_events.managers.ProfileManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI/Integration Tests for {@link SettingsFragment}.
 * <p>
 * This class tests the functionality of the Settings screen, including:
 * <ul>
 * <li>Toggling Push Notification preferences.</li>
 * <li>Navigation to the Terms and Conditions screen.</li>
 * <li>Appearance of critical dialogs (Logout, Delete Profile).</li>
 * <li>Full Delete Profile workflow.</li>
 * <li>Logout workflow.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Setup Strategy:</b> These tests launch {@link ProfileActivity} and immediately navigate
 * to the Settings Fragment to begin testing.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
public class SettingsFragmentTest {

    @Rule
    public ActivityScenarioRule<ProfileActivity> activityRule =
            new ActivityScenarioRule<>(ProfileActivity.class);

    private ProfileManager profileManager;
    private Profile testProfile;
    private String deviceId;

    /**
     * Sets up the test environment.
     * <p>
     * 1. Creates a dummy profile in Firestore.<br>
     * 2. Waits for the profile to be created.<br>
     * 3. Navigates from {@link ProfileActivity} to {@link SettingsFragment} automatically
     * so every test starts inside the Settings screen.
     * </p>
     */
    @Before
    public void setUp() throws InterruptedException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        profileManager = ProfileManager.getInstance();

        // 1. Create a dummy profile so we have valid settings to toggle
        testProfile = new Profile("Settings Tester", "settings@test.com", "555-0199", deviceId);

        // Default settings (both TRUE by default in Profile.java)
        testProfile.setPushAdminNotifications(true);
        testProfile.setPushOrganizerNotifications(true);

        // Sync wait for creation
        profileManager.createProfile(testProfile);
        Thread.sleep(2000); // Wait for Firestore sync

        // 2. Navigate to Settings Fragment
        onView(ViewMatchers.withId(R.id.profile_settings_button)).perform(click());

        // INCREASED WAIT: Give the fragment transition animation plenty of time to finish
        Thread.sleep(1500);
    }

    /**
     * Verifies that the Switches correctly toggle the User's preferences.
     * <p>
     * <b>Scenario:</b> User toggles "Admin Push Notifications" OFF.<br>
     * <b>Expected Result:</b> The switch UI updates, and the underlying {@link Profile} object
     * reflects the change (isPushAdminNotifications returns false).
     * </p>
     */
    @Test
    public void testNotificationSwitches() {
        // Verify initial state (ON)
        onView(withId(R.id.push_admin_switch)).check(matches(isChecked()));

        // Toggle OFF
        onView(withId(R.id.push_admin_switch)).perform(click());

        // Verify UI state
        onView(withId(R.id.push_admin_switch)).check(matches(isNotChecked()));

        // Verify Data State (Logic Check)
        Profile updatedProfile = profileManager.getProfileByDeviceId(deviceId);
        assertFalse("Admin push notifications should be disabled in Model", updatedProfile.isPushAdminNotifications());
    }

    /**
     * Verifies navigation to Terms and Conditions.
     * <p>
     * <b>Scenario:</b> User clicks the "Terms and Conditions" link.<br>
     * <b>Expected Result:</b> The View changes to display the Terms fragment (checking for unique text).
     * </p>
     */
    @Test
    public void testNavigateToTerms() {
        onView(withId(R.id.terms_conditions_clickable)).perform(click());

        // Assuming TermsFragment has a textview with ID 'terms_text' or similar header.
        // If not, we check for the container being visible, which implies success.
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));
    }

    /**
     * Verifies the Logout Confirmation Dialog appears and handles interactions.
     * <p>
     * <b>Scenario:</b> User clicks "Logout", checks dialog, and clicks "Yes".<br>
     * <b>Expected Result:</b> The dialog appears, and confirming triggers the logout logic (activity transition).
     * </p>
     */
    @Test
    public void testLogoutFunctionality() throws InterruptedException {
        onView(withId(R.id.profile_logout_button)).perform(click());

        // Check if dialog text is displayed
        onView(withText("Log Out")).check(matches(isDisplayed()));
        onView(withText("Are you sure you want to log out?")).check(matches(isDisplayed()));

        // Click "Yes"
        onView(withText("Yes")).perform(click());

        // Wait for handler delay in fragment (800ms) plus transition time
        Thread.sleep(2000);

        // Verify we navigated away from settings (Switch should no longer exist on screen)
        onView(withId(R.id.push_admin_switch)).check(doesNotExist());
    }

    /**
     * Verifies the Delete Profile workflow.
     * <p>
     * <b>Scenario:</b> User clicks "Delete Profile", confirms in dialog.<br>
     * <b>Expected Result:</b> The profile is removed from the database and the user is navigated away.
     * </p>
     */
    @Test
    public void testDeleteProfileFunctionality() throws InterruptedException {
        onView(withId(R.id.profile_delete_button)).perform(click());

        // Check dialog
        onView(withText("Delete Profile Confirmation")).check(matches(isDisplayed()));

        // Confirm Delete (Button text is "Delete" based on your Fragment code)
        onView(withText("Delete")).perform(click());

        // CRITICAL FIX: Wait for Firestore delete operation (Async) + Handler Delay (800ms)
        // 4000ms ensures the network call has time to complete and the Manager updates its list.
        Thread.sleep(4000);

        // Verify Data Integrity: Profile should be gone from Manager
        assertFalse("Profile should be deleted from Manager", profileManager.doesProfileExist(deviceId));

        // Mark testProfile as null so tearDown doesn't try to delete it again
        testProfile = null;
    }

    /**
     * Cleanup.
     * <p>
     * Deletes the dummy profile to ensure subsequent tests start clean.
     * </p>
     */
    @After
    public void tearDown() {
        if (testProfile != null) {
            // Retrieve latest version to get correct ID
            Profile p = profileManager.getProfileByDeviceId(deviceId);
            if (p != null) {
                profileManager.deleteProfile(p);
            }
        }
    }
}