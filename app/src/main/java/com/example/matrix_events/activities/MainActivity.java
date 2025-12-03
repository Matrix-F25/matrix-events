package com.example.matrix_events.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;
import com.example.matrix_events.fragments.SignUpFragment;
import com.example.matrix_events.managers.ProfileManager;

/**
 * The entry point activity for the Matrix Events application.
 * <p>
 * This activity serves as the initial authentication screen. It utilizes the Android Device ID
 * ({@link Settings.Secure#ANDROID_ID}) as a unique credential to verify user identity without
 * requiring a traditional username/password.
 * </p>
 * <p>
 * It provides two primary pathways:
 * <ul>
 * <li><b>Login:</b> Validates the device ID against the {@link ProfileManager}. If a profile exists, proceeds to the app.</li>
 * <li><b>Sign Up:</b> Checks if the device ID is new. If so, launches the {@link SignUpFragment}.</li>
 * </ul>
 * </p>
 */
public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPreferences";
    private static final String KEY_NOTIF_PERMISSION_ASKED = "notif_permission_asked";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        askForNotificationPermissionOnce();

        ProfileManager profileManager = ProfileManager.getInstance();

        Button loginButton = findViewById(R.id.login_button);
        Button signUpButton = findViewById(R.id.signup_button);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID); // getting the device ID

                if (profileManager.doesProfileExist(deviceId)) { // if the device ID exists in the database
                    Log.d("SignUp", "Account already exists. Showing toast");
                    Toast.makeText(MainActivity.this, "You already have an account! Please click \"Login\"", Toast.LENGTH_LONG).show();
                } else { // if the device ID does not exist in the database
                    Log.d("SignUp", "Account created successfully using device ID: " + deviceId);
                    Fragment fragment = new SignUpFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main, fragment).addToBackStack(null).commit();
                }
            }
        });

        // login press would navigate to search activity
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID); // getting the device ID

                if (profileManager.doesProfileExist(deviceId)) { // if the device ID exists in the database
                    Log.d("Login", "Account accessed successfully using device ID: " + deviceId);
                    Toast.makeText(MainActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), EventSearchActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                } else { // if the device ID does not exist in the database
                    Log.d("Login", "You do not have an account yet. Please sign up first!");
                    Toast.makeText(MainActivity.this, "You do not have an account yet. Please sign up first!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void askForNotificationPermissionOnce() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) return; // Not needed before Android 13+

        boolean alreadyAsked = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(KEY_NOTIF_PERMISSION_ASKED, false);
        if (alreadyAsked) return;

        // Show polite explanation dialog
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Enable Notifications?")
                .setMessage("We use notifications to let you know when organizers or admins send important updates. You can change this anytime in Settings.")
                .setPositiveButton("Allow", (dialog, which) -> {
                    // Mark that we asked
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .edit()
                            .putBoolean(KEY_NOTIF_PERMISSION_ASKED, true)
                            .apply();

                    // Now request the real OS permission
                    requestNotificationPermission();
                })
                .setNegativeButton("Not now", (dialog, which) -> {
                    // User said no — still mark as asked
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .edit()
                            .putBoolean(KEY_NOTIF_PERMISSION_ASKED, true)
                            .apply();
                })
                .show();
    }

    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            if (grantResults.length > 0 &&
                    grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Notifications enabled!", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Notifications disabled. You can enable them in Settings.", Toast.LENGTH_LONG).show();
            }
        }
    }
}