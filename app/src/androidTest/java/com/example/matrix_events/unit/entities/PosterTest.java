package com.example.matrix_events.unit.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import com.example.matrix_events.entities.Poster;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Unit tests for the {@link Poster} entity class.
 * <p>
 * This class verifies the data holding capabilities of the Poster class,
 * its inheritance from {@link com.example.matrix_events.database.DBObject},
 * and its ability to be serialized.
 * </p>
 */
public class PosterTest {

    private final String TEST_URL = "https://firebasestorage.googleapis.com/v0/b/example/o/poster.jpg";
    private final String TEST_EVENT_ID = "event_12345";
    private final String TEST_FILENAME = "poster_image_001.jpg";

    /**
     * Tests that the default constructor creates a valid object instance.
     * This is required for Firestore's POJO mapping.
     */
    @Test
    public void testDefaultConstructor() {
        Poster poster = new Poster();
        assertNotNull("Poster instance should be created", poster);
        assertNull("Image URL should be null initially", poster.getImageUrl());
        assertNull("Event ID should be null initially", poster.getEventId());
        assertNull("File Name should be null initially", poster.getFileName());
    }

    /**
     * Tests that the parameterized constructor correctly assigns all fields.
     */
    @Test
    public void testParameterizedConstructor() {
        Poster poster = new Poster(TEST_URL, TEST_EVENT_ID, TEST_FILENAME);

        assertNotNull("Poster instance should be created", poster);
        assertEquals("Image URL should match", TEST_URL, poster.getImageUrl());
        assertEquals("Event ID should match", TEST_EVENT_ID, poster.getEventId());
        assertEquals("File Name should match", TEST_FILENAME, poster.getFileName());
    }

    /**
     * Tests all setters and getters to ensure data mutability works as expected.
     */
    @Test
    public void testSettersAndGetters() {
        Poster poster = new Poster();

        poster.setImageUrl(TEST_URL);
        poster.setEventId(TEST_EVENT_ID);
        poster.setFileName(TEST_FILENAME);

        assertEquals("Image URL should match set value", TEST_URL, poster.getImageUrl());
        assertEquals("Event ID should match set value", TEST_EVENT_ID, poster.getEventId());
        assertEquals("File Name should match set value", TEST_FILENAME, poster.getFileName());
    }

    /**
     * Tests functionality inherited from {@link com.example.matrix_events.database.DBObject}.
     * Verifies that setting the ID works and that the equals() method compares based on ID.
     */
    @Test
    public void testDBObjectInheritance() {
        Poster poster1 = new Poster(TEST_URL, TEST_EVENT_ID, TEST_FILENAME);
        Poster poster2 = new Poster("other_url", "other_event", "other_file");

        // 1. Set IDs
        String docId = "firestore_doc_id_123";
        poster1.setId(docId);
        poster2.setId(docId);

        // 2. Test Get ID
        assertEquals("ID should be retrieved correctly", docId, poster1.getId());

        // 3. Test Equality (DBObject checks Class type and ID)
        assertEquals("Posters with same ID should be equal", poster1, poster2);

        // 4. Test Inequality
        poster2.setId("different_id");
        assertNotEquals("Posters with different IDs should not be equal", poster1, poster2);
    }

    /**
     * Tests Serialization.
     * <p>
     * Since Poster only contains String fields (which are Serializable),
     * this should work out-of-the-box without custom readObject/writeObject methods.
     * This test ensures future changes don't break this property.
     * </p>
     */
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        Poster original = new Poster(TEST_URL, TEST_EVENT_ID, TEST_FILENAME);
        original.setId("test_id");

        // 1. Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(original);
        out.flush();
        byte[] bytes = bos.toByteArray();

        // 2. Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bis);
        Poster deserialized = (Poster) in.readObject();

        // 3. Verify
        assertNotNull(deserialized);
        assertNotSame("Deserialized object should be a new instance", original, deserialized);

        // Check DBObject fields
        assertEquals("ID should persist", original.getId(), deserialized.getId());

        // Check Poster fields
        assertEquals("Image URL should persist", original.getImageUrl(), deserialized.getImageUrl());
        assertEquals("Event ID should persist", original.getEventId(), deserialized.getEventId());
        assertEquals("File Name should persist", original.getFileName(), deserialized.getFileName());
    }
}