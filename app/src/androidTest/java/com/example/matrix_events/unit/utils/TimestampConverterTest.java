package com.example.matrix_events.unit.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.example.matrix_events.utils.TimestampConverter;
import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Unit tests for {@link TimestampConverter}.
 * <p>
 * Verifies the null handling and date formatting logic of the converter utility.
 * </p>
 */
public class TimestampConverterTest {

    private TimeZone defaultTimeZone;

    @Before
    public void setUp() {
        // Save the default timezone to restore it later
        defaultTimeZone = TimeZone.getDefault();
        // Set default timezone to UTC to ensure consistent string output across different test environments
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @After
    public void tearDown() {
        // Restore the original timezone to avoid side effects on other tests
        TimeZone.setDefault(defaultTimeZone);
    }

    /**
     * Test A: Verify Null Handling.
     * <p>
     * The converter should return null if the input Timestamp is null, avoiding NullPointerExceptions.
     * </p>
     */
    @Test
    public void testConvertNullTimestamp() {
        String result = TimestampConverter.convertFirebaseTimestampToString(null);
        assertNull("Should return null for null input", result);
    }

    /**
     * Test B: Verify Date Formatting.
     * <p>
     * Creates a specific date and verifies the output string matches the pattern:
     * "MMM dd, yyyy 'at' h:mm a"
     * </p>
     */
    @Test
    public void testConvertValidTimestamp() {
        // Create a known date: October 25, 2025 at 14:30:00 UTC
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2025);
        calendar.set(Calendar.MONTH, Calendar.OCTOBER); // Note: Months are 0-indexed
        calendar.set(Calendar.DAY_OF_MONTH, 25);
        calendar.set(Calendar.HOUR_OF_DAY, 14); // 2 PM
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date date = calendar.getTime();
        Timestamp timestamp = new Timestamp(date);

        // Expected format: "MMM dd, yyyy 'at' h:mm a"
        // Note: 14:30 is 2:30 PM
        String expected = "Oct 25, 2025 at 2:30 PM";

        String result = TimestampConverter.convertFirebaseTimestampToString(timestamp);

        assertEquals("Date format should match pattern", expected, result);
    }

    /**
     * Test C: Verify Single Digit Day/Hour Formatting.
     * <p>
     * Ensures that days like '5' and hours like '9' are handled correctly without leading zeros
     * if the pattern does not enforce them (e.g., 'd' vs 'dd', 'h' vs 'hh').
     * Pattern uses 'dd' (05) and 'h' (9).
     * </p>
     */
    @Test
    public void testConvertSingleDigitDate() {
        // Create date: January 5, 2025 at 09:05:00 UTC
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.JANUARY, 5, 9, 5);

        Timestamp timestamp = new Timestamp(calendar.getTime());

        // Pattern "MMM dd, yyyy 'at' h:mm a" -> "Jan 05, 2025 at 9:05 AM"
        String expected = "Jan 05, 2025 at 9:05 AM";

        String result = TimestampConverter.convertFirebaseTimestampToString(timestamp);

        assertEquals(expected, result);
    }
}