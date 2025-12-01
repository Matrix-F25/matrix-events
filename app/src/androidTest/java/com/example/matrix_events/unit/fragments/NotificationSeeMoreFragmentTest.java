package com.example.matrix_events.unit.fragments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.matrix_events.entities.Notification;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.fragments.NotificationSeeMoreFragment;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

/**
 * Instrumented UI Tests for {@link NotificationSeeMoreFragment}.
 * <p>
 * This suite verifies that the fragment correctly displays and formats data
 * based on the {@link Notification} object and the provided adapter type.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
public class NotificationSeeMoreFragmentTest {

    private Notification testNotification;
    private Profile sender;
    private Profile receiver;
    private String formattedDate;
    private String formattedTime;

    @Before
    public void setUp() {
        sender = new Profile("FCM Admin", "fcm@admin.com", "123", "fcm_id");
        receiver = new Profile("Entrant User", "entrant@user.com", "456", "entrant_id");

        // Set a fixed timestamp for predictable date formatting checks
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.DECEMBER, 1, 14, 30); // Dec 1, 2025, 2:30 PM

        Timestamp fixedTimestamp = new Timestamp(calendar.getTime());

        testNotification = new Notification(
                sender,
                receiver,
                "Your registration is confirmed. Please check in.",
                Notification.NotificationType.ORGANIZER,
                fixedTimestamp
        );

        // Calculate expected formatted strings for verification
        formattedDate = "Dec 1, 2025";
        formattedTime = "2:30 PM";
    }

    /**
     * Test A: Verify display and formatting in "Entrant" mode.
     * <p>
     * <b>Scenario:</b> Launch with "entrant" type.<br>
     * <b>Expected:</b> Title shows only sender name. Date and time chips show formatted text.
     * </p>
     */
    @Test
    public void testEntrantModeDisplay() {
        // Launch fragment using FragmentScenario
        FragmentScenario<NotificationSeeMoreFragment> scenario = FragmentScenario.launchInContainer(
                NotificationSeeMoreFragment.class,
                NotificationSeeMoreFragment.newInstance(testNotification, "entrant").getArguments(),
                com.google.android.material.R.style.Theme_MaterialComponents // Host theme (FIX)
        );

        String expectedTitle = "New message from: FCM Admin";

        // 1. Verify Title and Body are displayed
        onView(withText(expectedTitle)).check(matches(isDisplayed()));
        onView(withText(testNotification.getMessage())).check(matches(isDisplayed()));

        // 2. Verify Date and Time are correctly formatted and bound to chips
        onView(withText(formattedDate)).check(matches(isDisplayed()));
        onView(withText(formattedTime)).check(matches(isDisplayed()));
    }

    /**
     * Test B: Verify display in "Admin" mode.
     * <p>
     * <b>Scenario:</b> Launch with "admin" type.<br>
     * <b>Expected:</b> Title shows sender and receiver names (Full context).
     * </p>
     */
    @Test
    public void testAdminModeDisplay() {
        // Launch fragment using FragmentScenario
        FragmentScenario<NotificationSeeMoreFragment> scenario = FragmentScenario.launchInContainer(
                NotificationSeeMoreFragment.class,
                NotificationSeeMoreFragment.newInstance(testNotification, "admin").getArguments(),
                com.google.android.material.R.style.Theme_MaterialComponents // Host theme (FIX)
        );

        String expectedTitle = sender.getName() + " sent to " + receiver.getName();

        // 1. Verify Title (Admin format)
        onView(withText(expectedTitle)).check(matches(isDisplayed()));

        // 2. Verify Body (Content is the same)
        onView(withText(testNotification.getMessage())).check(matches(isDisplayed()));
    }
}