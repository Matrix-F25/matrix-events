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

    /**
     * Called when the activity is starting.
     * <p>
     * Sets up the UI, handles window insets for edge-to-edge display, and configures
     * the click listeners for authentication.
     * </p>
     * <p>
     * <b>Authentication Logic:</b>
     * <ul>
     * <li><b>Sign Up Button:</b> Checks if the current Device ID already exists in the database.
     * If it does, a Toast prompts the user to login. If not, it opens the registration form.</li>
     * <li><b>Login Button:</b> Checks if the current Device ID exists.
     * If it does, it navigates to {@link EventSearchActivity}. If not, a Toast prompts the user to sign up.</li>
     * </ul>
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied.
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
}