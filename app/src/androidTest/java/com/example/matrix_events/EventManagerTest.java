package com.example.matrix_events;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Geolocation;
import com.example.matrix_events.entities.Poster;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.mvc.View;
import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class EventManagerTest {

    private EventManager eventManager;
    private CountDownLatch latch;
    private View testView;
    private Event testEvent;
    private String uniqueEventName;
    private volatile String expectedDescription; // Used by the listener to check for specific updates

    @Before
    public void setUp() {
        eventManager = EventManager.getInstance();
        latch = new CountDownLatch(1);
        uniqueEventName = "Test Event " + UUID.randomUUID().toString();

        testView = new View() {
            @Override
            public void update() {
                // This is called by EventManager whenever its data changes.
                // We check if an event with the specific name and description we expect is present.
                for (Event e : eventManager.getEvents()) {
                    if (uniqueEventName.equals(e.getName()) && expectedDescription.equals(e.getDescription())) {
                        // Our specific event state is here! Signal the test to continue.
                        testEvent.setId(e.getId()); // Always keep the ID updated for cleanup
                        latch.countDown();
                        break;
                    }
                }
            }
        };
        eventManager.addView(testView);
    }

    // Test that an event will be properly created in Firebase
    @Test
    public void testCreateAndReadEvent() throws InterruptedException {
        // Step 1: Define the initial state we expect to see.
        expectedDescription = "This is a test description.";

        // Step 2: Create a new event object.
        Profile organizer = new Profile("Test Organizer", "organizer@test.com", "1234567890", "test-device-id");
        Geolocation location = new Geolocation("Test Location", 45.0, -75.0);
        Timestamp now = new Timestamp(new Date());
        Poster poster = new Poster("http://example.com/poster.jpg");

        testEvent = new Event(
                uniqueEventName,
                expectedDescription,
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

        // Step 3: Tell the manager to create the event in Firebase.
        eventManager.createEvent(testEvent);

        // Step 4: Wait until our view's update() method confirms the event was created.
        boolean eventFound = latch.await(15, TimeUnit.SECONDS);
        assertTrue("Timeout: The created event never appeared in the manager.", eventFound);
    }

    @Test
    public void testUpdateEvent() throws InterruptedException {
        // --- Part 1: Create the initial event ---
        testCreateAndReadEvent(); // First, create the event and confirm it exists.

        // --- Part 2: Update multiple fields of the event ---
        latch = new CountDownLatch(1); // Reset latch for the update operation.

        // Define the new state we expect to see.
        String newName = "Updated Event Name";
        String newDescription = "This is an updated description.";
        int newCapacity = 500;
        Geolocation newLocation = new Geolocation("New Updated Location", 10.0, -10.0);

        // The listener will now wait for this new combination of name and description.
        uniqueEventName = newName;
        expectedDescription = newDescription;

        // Apply the changes to the local event object.
        testEvent.setName(newName);
        testEvent.setDescription(newDescription);
        testEvent.setEventCapacity(newCapacity);
        testEvent.setLocation(newLocation);

        // Tell the manager to update the event in Firebase.
        eventManager.updateEvent(testEvent);

        // Wait until our view's update() method confirms the event has been updated.
        boolean eventUpdated = latch.await(15, TimeUnit.SECONDS);
        assertTrue("Timeout: The updated event state never appeared in the manager.", eventUpdated);

        // --- Part 3: Verify all the changes --- 
        Event foundEvent = eventManager.getEvent(testEvent.getId());
        assertNotNull("Event could not be found in manager after update.", foundEvent);

        // Assert that all the edited features were changed properly.
        assertEquals("Event name was not updated correctly.", newName, foundEvent.getName());
        assertEquals("Event description was not updated correctly.", newDescription, foundEvent.getDescription());
        assertEquals("Event capacity was not updated correctly.", newCapacity, foundEvent.getEventCapacity());
        assertEquals("Event location name was not updated correctly.", newLocation.getName(), foundEvent.getLocation().getName());
    }

    @After
    public void tearDown() {
        // Clean up the created event from Firebase to keep the database clean.
        if (testEvent != null && testEvent.getId() != null) {
            eventManager.deleteEvent(testEvent);
        }
        eventManager.removeView(testView);
    }
}
