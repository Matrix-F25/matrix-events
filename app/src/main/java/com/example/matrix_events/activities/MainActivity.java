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

public class MainActivity extends AppCompatActivity {

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
                Fragment signUpFragment = new SignUpFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main, signUpFragment).addToBackStack(null).commit();
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