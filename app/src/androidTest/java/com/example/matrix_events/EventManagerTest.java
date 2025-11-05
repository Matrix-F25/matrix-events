package com.example.matrix_events;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Geolocation;
import com.example.matrix_events.entities.Poster;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.entities.ReoccurringType;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.mvc.View;
import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class EventManagerTest {

    private EventManager eventManager;
    private CountDownLatch latch;
    private View testView;
    private Event testEvent;

    @Before
    public void setUp() {
        eventManager = EventManager.getInstance();
        latch = new CountDownLatch(1);
        testView = new View() {
            @Override
            public void update() {
                latch.countDown();
            }
        };
        eventManager.addView(testView);
    }

    @Test
    public void testCreateAndReadEvent() throws InterruptedException {
        // Create a new event with all required fields
        Profile organizer = new Profile("Test Organizer", "organizer@test.com", "1234567890", "test-device-id");
        Geolocation location = new Geolocation("Test Location", 45.0, -75.0);
        Timestamp now = new Timestamp(new Date());
        Poster poster = new Poster("http://example.com/poster.jpg");

        testEvent = new Event(
                "Test Event",
                "This is a test description.",
                organizer,
                location,
                now,
                now,
                100,
                20,
                now,
                now,
                false,
                null,
                null,
                true,
                poster
        );

        eventManager.createEvent(testEvent);

        // Give Firebase some time to process the write
        Thread.sleep(1000);

        // Wait for Firebase to update
        latch.await(10, TimeUnit.SECONDS);

        // Find the event in the manager
        Event foundEvent = null;
        for (Event e : eventManager.getEvents()) {
            if ("Test Event".equals(e.getName())) {
                foundEvent = e;
                testEvent.setId(e.getId()); // Set ID for cleanup
                break;
            }
        }

        // Assert that the event was found and its data is correct
        assertNotNull("Event should be found in EventManager", foundEvent);
        assertEquals("Event name should match", "Test Event", foundEvent.getName());
        assertEquals("Event description should match", "This is a test description.", foundEvent.getDescription());
        assertEquals("Organizer name should match", "Test Organizer", foundEvent.getOrganizer().getName());
    }

    @After
    public void tearDown() {
        // Clean up the created event
        if (testEvent != null && testEvent.getId() != null) {
            latch = new CountDownLatch(1);
            eventManager.deleteEvent(testEvent);
            try {
                // Wait for deletion to complete
                latch.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        eventManager.removeView(testView);
    }
}
