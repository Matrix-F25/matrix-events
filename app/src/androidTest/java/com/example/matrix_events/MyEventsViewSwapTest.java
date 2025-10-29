package com.example.matrix_events;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.matrix_events.activities.EntrantMyEventsActivity;
import com.example.matrix_events.activities.OrganizerMyEventsActivity;

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

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MyEventsViewSwapTest {

    // Start with the Entrant view for the test
    @Rule
    public ActivityScenarioRule<EntrantMyEventsActivity> activityRule =
            new ActivityScenarioRule<>(EntrantMyEventsActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testEntrantAndOrganizerViewSwap() {
        // 1. From Entrant view, click the button to switch to Organizer view
        onView(withId(R.id.button_switch_to_org)).perform(click());

        // Verify that the OrganizerMyEventsActivity is now open
        intended(hasComponent(OrganizerMyEventsActivity.class.getName()));

        // 2. From Organizer view, click the button to switch back to Entrant view
        onView(withId(R.id.button_switch_to_entrant)).perform(click());

        // Verify that the EntrantMyEventsActivity is now open
        intended(hasComponent(EntrantMyEventsActivity.class.getName()));
    }
}

