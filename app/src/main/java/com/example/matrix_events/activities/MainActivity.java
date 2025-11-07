package com.example.matrix_events.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;
import com.example.matrix_events.fragments.SignUpFragment;
import com.example.matrix_events.managers.ProfileManager;

/**
 * The main entry point of the application, serving as the initial login/sign-up screen.
 * This activity determines user authentication based on the device's unique Android ID.
 * It provides options for users to either "log in" if their device is already registered
 * or "sign up" to create a new profile.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * This method initializes the user interface, sets up listeners for the login and sign-up buttons,
     * and handles the core logic for user authentication. It checks if a profile already exists
     * for the current device and guides the user to the appropriate action (login or sign-up).
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
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

        ProfileManager profileManager = ProfileManager.getInstance();

        Button loginButton = findViewById(R.id.login_button);
        Button signUpButton = findViewById(R.id.signup_button);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the click event for the Sign-Up button.
             * It checks if a profile already exists for the device. If not, it displays the
             * {@link SignUpFragment}. If a profile exists, it informs the user via a Toast message.
             *
             * @param v The view that was clicked.
             */
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
            /**
             * Handles the click event for the Login button.
             * It checks if a profile exists for the device. If so, it navigates the user to the
             * {@link EventSearchActivity}. If not, it prompts the user to sign up first.
             *
             * @param v The view that was clicked.
             */
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
}