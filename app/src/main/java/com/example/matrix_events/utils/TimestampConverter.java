package com.example.matrix_events.utils;
import java.text.SimpleDateFormat;
import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.Locale;

public class TimestampConverter {
    public static String convertFirebaseTimestampToString(Timestamp firebaseTimestamp) {
        if (firebaseTimestamp == null) {
            return null;
        }

        Date date = firebaseTimestamp.toDate();
        String pattern = "MMM dd, yyyy 'at' h:mm a"; // Example: Oct 25, at 2025 2:30 PM
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(date);
    }
}
