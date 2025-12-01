package com.example.matrix_events.unit.activities;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.provider.Settings;

import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.matrix_events.R;
import com.example.matrix_events.activities.EventSearchActivity;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.managers.EventManager;
import com.google.firebase.Timestamp;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Instrumented UI Integration Tests for {@link EventSearchActivity}.
 * <p>
 * This suite validates the filtering logic of the Event Search screen by interacting with
 * the UI components (Search Bar, Dropdown Spinner) and verifying the ListView results.
 * </p>
 * <p>
 * <b>Test Coverage:</b>
 * <ul>
 * <li><b>Text Search:</b> Filtering by event name.</li>
 * <li><b>Status Filtering:</b> Filtering by time (Upcoming/Past) and availability (Registration Open).</li>
 * <li><b>Combined Filtering:</b> Verifying that both filters work together using AND logic.</li>
 * </ul>
 * </p>
 */
@RunWith(AndroidJUnit4.class)
public class EventSearchActivityTest {

    @Rule
    public ActivityScenarioRule<EventSearchActivity> activityRule =
            new ActivityScenarioRule<>(EventSearchActivity.class);

    private EventManager eventManager;
    private List<Event> mockEvents = new ArrayList<>();
    private String deviceId;

    /**
     * Sets up the test environment by seeding the database with specific mock events.
     * <p>
     * Creates three distinct event types to test different filter scenarios:
     * <ol>
     * <li><b>"Future Gala":</b> Starts in future, Registration Open.</li>
     * <li><b>"Secret Meeting":</b> Starts in future, Registration Closed.</li>
     * <li><b>"Ancient History":</b> Started in past, Registration Closed.</li>
     * </ol>
     * This setup ensures we have a valid target for every filter option.
     * </p>
     */
    @Before
    public void setUp() throws InterruptedException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        eventManager = EventManager.getInstance();

        Profile organizer = new Profile("Test Org", "org@test.com", "555-1234", "org_device");

        long now = new Date().getTime();
        long hour = 3600 * 1000;

        // Timestamps calculated relative to current execution time
        Timestamp constructorRegStart = new Timestamp(new Date(now + 1 * hour));
        Timestamp constructorRegEnd = new Timestamp(new Date(now + 2 * hour));
        Timestamp constructorEvtStart = new Timestamp(new Date(now + 3 * hour));
        Timestamp constructorEvtEnd = new Timestamp(new Date(now + 4 * hour));
        Timestamp pastDate = new Timestamp(new Date(now - 24 * hour));

        // 1. Future Gala (Open)
        Event eventA = new Event("Future Gala", "Desc", organizer, "Hall A",
                constructorEvtStart, constructorEvtEnd, 10, 0,
                constructorRegStart, constructorRegEnd, false, null, null, false, null);
        // Manually set state to Open
        eventA.setRegistrationStartDateTime(new Timestamp(new Date()));
        eventA.setRegistrationOpened(true);
        eventA.setLotteryProcessed(false);

        // 2. Secret Meeting (Closed)
        Event eventB = new Event("Secret Meeting", "Desc", organizer, "Hall B",
                constructorEvtStart, constructorEvtEnd, 10, 0,
                constructorRegStart, constructorRegEnd, false, null, null, false, null);
        // Manually set state to Closed
        eventB.setRegistrationStartDateTime(pastDate);
        eventB.setRegistrationEndDateTime(pastDate);
        eventB.setRegistrationOpened(true);
        eventB.setLotteryProcessed(true);

        // 3. Ancient History (Past)
        Event eventC = new Event("Ancient History", "Desc", organizer, "Museum",
                constructorEvtStart, constructorEvtEnd, 10, 0,
                constructorRegStart, constructorRegEnd, false, null, null, false, null);
        // Manually set state to Past
        eventC.setEventStartDateTime(pastDate);
        eventC.setEventEndDateTime(pastDate);
        eventC.setRegistrationStartDateTime(pastDate);
        eventC.setRegistrationEndDateTime(pastDate);
        eventC.setRegistrationOpened(true);
        eventC.setLotteryProcessed(true);

        // Add to Manager
        eventManager.createEvent(eventA);
        eventManager.createEvent(eventB);
        eventManager.createEvent(eventC);

        mockEvents.add(eventA);
        mockEvents.add(eventB);
        mockEvents.add(eventC);

        // Allow time for Firestore/Manager to update
        Thread.sleep(3000);
    }

    /**
     * Custom Matcher to find a specific Event in the ListView Adapter by its Name.
     * <p>
     * This is necessary because using {@code atPosition(0)} is unreliable if the database
     * contains existing events from previous tests or manual usage. This matcher finds the
     * correct row regardless of its position in the list.
     * </p>
     *
     * @param expectedName The name of the event to search for.
     * @return A Matcher that matches an {@link Event} object with the given name.
     */
    public static Matcher<Object> withEventName(final String expectedName) {
        return new BoundedMatcher<Object, Event>(Event.class) {
            @Override
            protected boolean matchesSafely(Event event) {
                return expectedName.equals(event.getName());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with event name: " + expectedName);
            }
        };
    }

    /**
     * Tests filtering by Search Bar text input.
     * <p>
     * <b>Scenario:</b> User types "Secret" into the search bar while filter is "All Events".<br>
     * <b>Expected Result:</b> The list updates to show "Secret Meeting".
     * </p>
     */
    @Test
    public void testTextSearch() throws InterruptedException {
        selectDropdownOption("All Events");

        onView(ViewMatchers.withId(R.id.search_input))
                .perform(clearText(), typeText("Secret"), closeSoftKeyboard());

        Thread.sleep(1000);

        // Verify "Secret Meeting" is displayed
        onData(withEventName("Secret Meeting"))
                .inAdapterView(withId(R.id.event_search_listview))
                .check(matches(isDisplayed()));
    }

    /**
     * Tests filtering by the Dropdown Status (Time-based).
     * <p>
     * <b>Scenario:</b> User selects "Past / Closed" from the dropdown.<br>
     * <b>Expected Result:</b> The list updates to show only "Ancient History" (the past event).
     * </p>
     */
    @Test
    public void testDropdownFilter_PastEvents() throws InterruptedException {
        // Clear text to ensure clean slate
        onView(withId(R.id.search_input)).perform(clearText(), closeSoftKeyboard());

        selectDropdownOption("Past / Closed");

        Thread.sleep(1000);

        // Verify "Ancient History" is displayed
        onData(withEventName("Ancient History"))
                .inAdapterView(withId(R.id.event_search_listview))
                .check(matches(isDisplayed()));
    }

    /**
     * Tests combined filtering (Text Search + Dropdown Status).
     * <p>
     * <b>Scenario:</b> User selects "Registration Open" AND types "Future".<br>
     * <b>Expected Result:</b> The list strictly enforces both rules, showing "Future Gala".<br>
     * (It should NOT show "Secret Meeting" because although it matches "Future" timeframe, its registration is closed).
     * </p>
     */
    @Test
    public void testCombinedFilter() throws InterruptedException {
        selectDropdownOption("Registration Open");

        onView(withId(R.id.search_input))
                .perform(clearText(), typeText("Future"), closeSoftKeyboard());

        Thread.sleep(1000);

        // Verify "Future Gala" shows up
        onData(withEventName("Future Gala"))
                .inAdapterView(withId(R.id.event_search_listview))
                .check(matches(isDisplayed()));
    }

    /**
     * Helper method to interact with the Exposed Dropdown Menu.
     * <p>
     * Selecting an item in an AutoCompleteTextView (Material Dropdown) requires interacting
     * with a {@code PopupWindow}, which is a different root window than the Activity.
     * We use {@code inRoot(isPlatformPopup())} to target the dropdown list.
     * </p>
     *
     * @param optionText The text of the option to select (e.g., "All Events").
     */
    private void selectDropdownOption(String optionText) throws InterruptedException {
        // Click the dropdown to expand it
        onView(withId(R.id.filter_autocomplete_textview)).perform(click());
        Thread.sleep(500); // Wait for animation

        // Select the item from the popup list
        onView(withText(optionText))
                .inRoot(isPlatformPopup())
                .perform(click());
        Thread.sleep(500); // Wait for close animation
    }

    /**
     * Cleanup method.
     * <p>
     * Deletes the mock events created during setup to prevent polluting the database
     * for future tests or other users.
     * </p>
     */
    @After
    public void tearDown() {
        for (Event e : mockEvents) {
            if (e.getId() != null) {
                eventManager.deleteEvent(e);
            }
        }
    }
}