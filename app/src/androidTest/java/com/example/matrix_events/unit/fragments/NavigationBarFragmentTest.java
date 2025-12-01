package com.example.matrix_events.unit.fragments;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

import com.example.matrix_events.R;
import com.example.matrix_events.activities.EntrantMyEventsActivity;
import com.example.matrix_events.activities.EventSearchActivity;
import com.example.matrix_events.activities.NotificationActivity;
import com.example.matrix_events.activities.ProfileActivity;
import com.example.matrix_events.activities.QRCodeActivity;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NavigationBarFragmentTest {

    // Launch QRCodeActivity before each test. You can start from any activity with a nav bar.
    @Rule
    public ActivityScenarioRule<QRCodeActivity> activityRule = new ActivityScenarioRule<>(QRCodeActivity.class);

    // Initialize Espresso-Intents before each test
    @Before
    public void setUp() {
        Intents.init();
    }

    // Release Espresso-Intents after each test
    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testFullNavigationBarNavigation() {
        // 1. Navigate to "Event Search" and verify
        onView(ViewMatchers.withId(R.id.nav_event_search)).perform(click());
        intended(hasComponent(EventSearchActivity.class.getName()));

        // 2. Navigate to "My Events" (Entrant) and verify
        onView(withId(R.id.nav_my_events)).perform(click());
        intended(hasComponent(EntrantMyEventsActivity.class.getName()));

        // 3. Navigate to "Notifications" and verify
        onView(withId(R.id.nav_notifications)).perform(click());
        intended(hasComponent(NotificationActivity.class.getName()));

        // 4. Navigate to "Profile" and verify
        onView(withId(R.id.nav_profile)).perform(click());
        intended(hasComponent(ProfileActivity.class.getName()));

        // 5. Navigate back to "QR Code" to complete the loop
        onView(withId(R.id.nav_qrcode)).perform(click());
        intended(hasComponent(QRCodeActivity.class.getName()));
    }
}
