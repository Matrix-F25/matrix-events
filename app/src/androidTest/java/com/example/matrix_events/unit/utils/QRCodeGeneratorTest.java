package com.example.matrix_events.unit.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.matrix_events.utils.QRCodeGenerator;
import com.google.zxing.WriterException;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented unit tests for {@link QRCodeGenerator}.
 * <p>
 * These tests verify the logic for generating unique hash strings and converting
 * those strings into valid Android Bitmap objects representing QR codes.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
public class QRCodeGeneratorTest {

    /**
     * Tests that {@link QRCodeGenerator#generateQRHash(String)} returns a valid string
     * of the expected length (32 characters hex string).
     */
    @Test
    public void testGenerateQRHash_Format() {
        String eventId = "event_123";
        String hash = QRCodeGenerator.generateQRHash(eventId);

        assertNotNull("Hash should not be null", hash);
        assertEquals("Hash length should be 32 characters (hex)", 32, hash.length());
        // Verify it contains only hex characters
        assertTrue("Hash should match hex pattern", hash.matches("[0-9a-fA-F]+"));
    }

    /**
     * Tests that {@link QRCodeGenerator#generateQRHash(String)} generates UNIQUE hashes
     * for the same input event ID.
     * <p>
     * The implementation appends a random UUID to the event ID before hashing,
     * so two calls should never result in the same hash.
     * </p>
     */
    @Test
    public void testGenerateQRHash_Uniqueness() {
        String eventId = "same_event_id";

        String hash1 = QRCodeGenerator.generateQRHash(eventId);
        String hash2 = QRCodeGenerator.generateQRHash(eventId);

        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals("Hashes should be unique even for the same event ID due to UUID salting", hash1, hash2);
    }

    /**
     * Tests that {@link QRCodeGenerator#generateQRCodeBitmap(String, int, int)} creates
     * a valid Bitmap with the requested dimensions.
     */
    @Test
    public void testGenerateQRCodeBitmap_Success() {
        String mockHash = "a1b2c3d4e5f67890a1b2c3d4e5f67890"; // 32 char hex
        int width = 512;
        int height = 512;

        try {
            Bitmap bitmap = QRCodeGenerator.generateQRCodeBitmap(mockHash, width, height);

            assertNotNull("Bitmap should be generated", bitmap);
            assertEquals("Bitmap width should match request", width, bitmap.getWidth());
            assertEquals("Bitmap height should match request", height, bitmap.getHeight());
            assertEquals("Bitmap config should be ARGB_8888", Bitmap.Config.ARGB_8888, bitmap.getConfig());

        } catch (WriterException e) {
            fail("Should not throw WriterException for valid input: " + e.getMessage());
        }
    }

    /**
     * Tests that the generated Bitmap actually contains distinct pixel data (Black and White),
     * confirming that the ZXing encoding logic ran.
     */
    @Test
    public void testGenerateQRCodeBitmap_PixelContent() throws WriterException {
        String mockHash = "test_hash_content";
        int size = 100;

        Bitmap bitmap = QRCodeGenerator.generateQRCodeBitmap(mockHash, size, size);

        boolean hasBlack = false;
        boolean hasWhite = false;

        // Sample pixels to ensure we have contrast (QR code logic)
        // Note: We loop somewhat broadly because borders might be white
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int pixel = bitmap.getPixel(x, y);
                if (pixel == Color.BLACK) {
                    hasBlack = true;
                } else if (pixel == Color.WHITE) {
                    hasWhite = true;
                }
            }
            // Optimization: break early if we found both
            if (hasBlack && hasWhite) break;
        }

        assertTrue("QR Code should contain black pixels", hasBlack);
        assertTrue("QR Code should contain white pixels", hasWhite);
    }

    /**
     * Tests behavior when invalid dimensions are provided.
     * ZXing typically throws an exception for 0 or negative dimensions.
     */
    @Test
    public void testGenerateQRCodeBitmap_InvalidDimensions() {
        String mockHash = "valid_hash";
        try {
            QRCodeGenerator.generateQRCodeBitmap(mockHash, 0, 0);
            fail("Should have thrown WriterException or IllegalArgumentException for 0 dimensions");
        } catch (Exception e) {
            // Expected behavior: ZXing throws IllegalArgumentException inside WriterException usually,
            // or creates a fail state. As long as it doesn't return a valid 0x0 bitmap silently.
            assertNotNull(e);
        }
    }
}