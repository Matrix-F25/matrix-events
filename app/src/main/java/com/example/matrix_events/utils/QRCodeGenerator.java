package com.example.matrix_events.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Utility class for generating QR codes for events.
 */
public class QRCodeGenerator {

    /**
     * Generates a unique hash for an event's QR code.
     * This hash will be embedded in the QR code and used to identify the event.
     *
     * @param eventId The event's Firebase ID
     * @return A unique hash string
     */
    public static String generateQRHash(String eventId) {
        try {
            // Create a unique string combining eventId and a UUID
            String input = eventId + "_" + UUID.randomUUID().toString();

            // Hash it with SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            // Return first 32 characters for manageable size
            return hexString.toString().substring(0, 32);
        } catch (NoSuchAlgorithmException e) {
            // Fallback to UUID if SHA-256 not available
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    /**
     * Generates a QR code bitmap from a hash string.
     *
     * @param qrHash The hash string to encode in the QR code
     * @param width  The desired width of the QR code in pixels
     * @param height The desired height of the QR code in pixels
     * @return A Bitmap containing the QR code
     * @throws WriterException If QR code generation fails
     */
    public static Bitmap generateQRCodeBitmap(String qrHash, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrHash, BarcodeFormat.QR_CODE, width, height);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        return bitmap;
    }
}