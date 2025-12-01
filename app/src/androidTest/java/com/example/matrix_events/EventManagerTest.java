/* package com.example.matrix_events;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.matrix_events.entities.Event;
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
    private volatile String expectedDescription;
    private volatile boolean testingDeletion = false;

    @Before
    public void setUp() {
        eventManager = EventManager.getInstance();
        latch = new CountDownLatch(1);
        uniqueEventName = "Test Event " + UUID.randomUUID().toString();

        testView = new View() {
            @Override
            public void update() {
                if (testingDeletion) {
                    // For deletion to confirm the event is gone.
                    boolean eventFound = false;
                    for (Event e : eventManager.getEvents()) {
                        if (uniqueEventName.equals(e.getName())) {
                            eventFound = true;
                            break;
                        }
                    }
                    if (!eventFound) {
                        latch.countDown(); // Signal that deletion is confirmed.
                    }
                } else {
                    // For creation/update, confirm the event is present.
                    for (Event e : eventManager.getEvents()) {
                        if (uniqueEventName.equals(e.getName()) && expectedDescription.equals(e.getDescription())) {
                            testEvent.setId(e.getId());
                            latch.countDown();
                            break;
                        }
                    }
                }
            }
        };
        eventManager.addView(testView);
    }

    @Test
    public void testCreateAndReadEvent() throws InterruptedException {
        expectedDescription = "This is a test description.";
        Profile organizer = new Profile("Test Organizer", "organizer@test.com", "1234567890", "test-device-id");
        Geolocation location = new Geolocation("Test Location", 45.0, -75.0);
        testEvent = new Event(uniqueEventName, expectedDescription, organizer, location, new Timestamp(new Date()), new Timestamp(new Date()), 100, 20, new Timestamp(new Date()), new Timestamp(new Date()), false, null, null, true, new Poster("http://example.com/poster.jpg"));

        eventManager.createEvent(testEvent);

        boolean eventFound = latch.await(15, TimeUnit.SECONDS);
        assertTrue("Timeout: The created event never appeared in the manager.", eventFound);
    }

    @Test
    public void testUpdateEvent() throws InterruptedException {
        testCreateAndReadEvent(); // First, create the event.

        latch = new CountDownLatch(1); // Reset latch for the update operation.
        String newName = "Updated Event Name";
        String newDescription = "This is an updated description.";
        uniqueEventName = newName; // The listener will now look for the new name
        expectedDescription = newDescription;

        testEvent.setName(newName);
        testEvent.setDescription(newDescription);

        eventManager.updateEvent(testEvent);

        boolean eventUpdated = latch.await(15, TimeUnit.SECONDS);
        assertTrue("Timeout: The updated event state never appeared in the manager.", eventUpdated);

        Event foundEvent = eventManager.getEvent(testEvent.getId());
        assertNotNull("Event could not be found in manager after update.", foundEvent);
        assertEquals("Event name was not updated correctly.", newName, foundEvent.getName());
        assertEquals("Event description was not updated correctly.", newDescription, foundEvent.getDescription());
    }

    @Test
    public void testDeleteEvent() throws InterruptedException {
        // --- Part 1: Create the event so there is something to delete ---
        testCreateAndReadEvent();
        assertNotNull("Precondition failed: Test event ID is null before deletion.", testEvent.getId());

        // --- Part 2: Delete the event ---
        latch = new CountDownLatch(1); // Reset latch for the deletion operation.
        testingDeletion = true; // Switch listener to deletion confirmation mode.

        eventManager.deleteEvent(testEvent);

        // Wait until our listener confirms the event is no longer in the list.
        boolean eventDeleted = latch.await(15, TimeUnit.SECONDS);
        assertTrue("Timeout: The event was not removed from the manager's list.", eventDeleted);

        // --- Part 3: Verify the event is truly gone ---
        Event foundEvent = eventManager.getEvent(testEvent.getId());
        assertNull("The event was still found in the manager after it should have been deleted.", foundEvent);
    }

    @After
    public void tearDown() {
        // Clean up any test artifacts, if necessary.
        if (testEvent != null && testEvent.getId() != null && !testingDeletion) {
             // Avoid trying to delete something that was already deleted in the delete test.
            eventManager.deleteEvent(testEvent);
        }
        eventManager.removeView(testView);
    }
}
*/