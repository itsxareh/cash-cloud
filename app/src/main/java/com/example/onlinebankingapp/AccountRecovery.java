package com.example.onlinebankingapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Map;

public class AccountRecovery extends AppCompatActivity {
    EditText recoveryEmail;
    TextView verificationStatus;
    Button sendVerificationButton, changeEmailButton;
    UserManager userManager;
    private FirebaseAuth mAuth;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_recovery);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        userManager = UserManager.getInstance();
        recoveryEmail = findViewById(R.id.recoveryEmail);
        verificationStatus = findViewById(R.id.verificationStatus);
        sendVerificationButton = findViewById(R.id.sendVerificationButton);
        changeEmailButton = findViewById(R.id.changeEmailButton);

        loadUserData();

        sendVerificationButton.setOnClickListener(v -> sendEmailVerification());
        changeEmailButton.setOnClickListener(v -> changeEmailAddress());
    }

    private void loadUserData() {
        userManager.getUserData(new UserManager.UserDataCallback() {
            @Override
            public void onDataLoaded(Map<String, Object> userData) {
                if (userData != null && userData.containsKey("email")) {
                    userEmail = userData.get("email").toString();
                    recoveryEmail.setText(userEmail);
                    checkEmailVerification();
                } else {
                    recoveryEmail.setText("Unknown User");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AccountRecovery.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                recoveryEmail.setError("Error");
            }
        });
    }

    private void checkEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (user.isEmailVerified()) {
                verificationStatus.setTextColor(ContextCompat.getColor(this, R.color.moneygreen));
                verificationStatus.setText("VERIFIED");
                sendVerificationButton.setEnabled(false);
            } else {
                verificationStatus.setText("UNVERIFIED");
            }
        } else {
            verificationStatus.setText("No authenticated user found.");
        }
    }

    private void sendEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && !user.isEmailVerified()) {
            user.sendEmailVerification().addOnCompleteListener(this, verificationTask -> {
                if (verificationTask.isSuccessful()) {
                    verificationStatus.setText("Verification email sent to " + userEmail);
                    Toast.makeText(AccountRecovery.this, "Verification email sent.", Toast.LENGTH_SHORT).show();
                } else {
                    verificationStatus.setText("Failed to send verification email.");
                    Toast.makeText(AccountRecovery.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void changeEmailAddress() {
        String newEmail = recoveryEmail.getText().toString().trim();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null && !newEmail.isEmpty() && !newEmail.equals(userEmail)) {
            String phoneNumber = "+639212870742";
            String verificationCode = "111111";

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, phoneNumber);

            user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
                if (reauthTask.isSuccessful()) {
                    user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AccountRecovery.this, "Verification email sent to the new email address. Please verify to complete the update.", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("changeEmailAddress", "Failed to send verification email", task.getException());
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(AccountRecovery.this, "Failed to send verification email: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e("changeEmailAddress", "Re-authentication failed", reauthTask.getException());
                    String errorMessage = reauthTask.getException() != null ? reauthTask.getException().getMessage() : "Unknown error";
                    Toast.makeText(AccountRecovery.this, "Re-authentication failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No changes made to the email.", Toast.LENGTH_SHORT).show();
        }
    }
}
