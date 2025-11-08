package com.example.matrix_events;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.matrix_events.activities.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class USTest {

    // Launches MainActivity before each test, which is the entry point of the app.
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Corresponds to US 01.04.03: As an entrant, I want to be able to opt-out of notifications.
     */
    @Test
    public void testNotificationOptOut() {
        // 1. Navigate to the user profile screen.
        // 2. Find the notification toggle/switch.
        // 3. Click the toggle to disable notifications.
        // 4. Verify that the change is saved (e.g., by navigating away and back, or checking a toast message).
    }

    /**
     * Corresponds to US 01.02.02: As an entrant, I want to be able to update my name, email, and phone number.
     */
    @Test
    public void testUpdateProfileInfo() {
        // 1. Navigate to the user profile screen.
        // 2. Click an "edit" button.
        // 3. Type new text into the name, email, and phone number fields.
        // 4. Click a "save" button.
        // 5. Verify that the new information is displayed on the profile screen.
    }

    /**
     * Corresponds to US 01.02.04: As an entrant, I want to be able to delete my profile.
     */
    @Test
    public void testDeleteProfile() {
        // 1. Navigate to the user profile screen.
        // 2. Find and click a "delete profile" button.
        // 3. Click "confirm" on a confirmation dialog.
        // 4. Verify that the user is returned to the main/signup screen.
    }

    /**
     * Corresponds to US 01.01.04: As an entrant, I want to be able to filter the event list.
     */
    @Test
    public void testEventFilterSearch() {
        // 1. Navigate to the Event Search screen.
        // 2. Find a search bar or filter button.
        // 3. Enter text or select a filter option.
        // 4. Verify that the list of events updates to show only matching results.
    }

    /**
     * Corresponds to US 01.04.01: As an entrant, I want to be notified when I am selected for an event.
     * Note: This is difficult to test via UI alone and may require checking system notifications using UiAutomator.
     */
    @Test
    public void testNotificationOnSelection() {
        // 1. As an entrant, sign up for an event.
        // 2. (In the background) As an organizer, select that entrant.
        // 3. Use UiAutomator to open the notification shade and verify the notification exists.
    }

    /**
     * Corresponds to US 01.04.02: As an entrant, I want to be notified when I am not selected.
     */
    @Test
    public void testNotificationOnNotSelected() {
        // Similar setup to the testNotificationOnSelection test.
    }

    /**
     * Corresponds to US 02.05.01: As an organizer, I want to be able to send a "you won!" notification.
     */
    @Test
    public void testOrganizerSendsWinNotification() {
        // 1. As an organizer, navigate to a specific event's dashboard.
        // 2. Select an entrant from a list.
        // 3. Click a "Send Win Notification" button.
        // 4. Verify a confirmation message appears (e.g., a Toast or Snackbar).
    }

    /**
     * Corresponds to US 01.07.01: As a user, I want to be identified by my device ID for login.
     */
    @Test
    public void testDeviceIdentification() {
        // 1. Ensure no user is currently signed in.
        // 2. Relaunch the app and click the "Login" button.
        // 3. Verify that the user is automatically logged in and taken to the main event screen.
    }

    /**
     * Corresponds to US 01.01.03: As an entrant, I want to see a list of events.
     */
    @Test
    public void testSeeListOfEvents() {
        // 1. Log in or navigate to the main event search screen.
        // 2. Wait for a moment for data to load.
        // 3. Check that the ListView (R.id.event_list) is displayed and contains items.
    }

    /**
     * Corresponds to US 01.02.01: As an entrant, I want to add my name, email, and phone number during signup.
     */
    @Test
    public void testAddProfileInfoOnSignup() {
        // 1. Start from a state where no user is signed in.
        // 2. Click the "Sign Up" button.
        // 3. Enter text into the name, email, and phone number fields.
        // 4. Click the final "Sign Up" button.
        // 5. Verify the user is taken to the main part of the app.
    }
}