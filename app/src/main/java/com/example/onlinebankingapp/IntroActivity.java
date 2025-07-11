package com.example.onlinebankingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class IntroActivity extends AppCompatActivity {
    private static final String TAG = "IntroActivity";
    private Button startButton, loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        startButton = findViewById(R.id.start_account_button);
        loginButton = findViewById(R.id.login_button);

        startButton.setOnClickListener(v -> {
            Log.d(TAG, "Start button clicked");
            navigateToFragment(new Page1SignUp());
        });

        loginButton.setOnClickListener(v -> {
            Log.d(TAG, "Login button clicked");
            startActivity(new Intent(IntroActivity.this, SignInActivity.class));
        });
    }

    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack("IntroActivity");
        transaction.commit();
        Log.d(TAG, "Navigated to fragment: " + fragment.getClass().getSimpleName());
    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack("IntroActivity", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            super.onBackPressed();
        }
    }

}
