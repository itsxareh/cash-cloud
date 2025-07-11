package com.example.onlinebankingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.TimeUnit;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.integrity.IntegrityManager;
import com.google.android.play.core.integrity.IntegrityManagerFactory;
import com.google.android.play.core.integrity.IntegrityTokenResponse;
import com.google.android.play.core.integrity.IntegrityTokenRequest;
import com.google.android.play.core.integrity.model.IntegrityErrorCode;

import org.mindrot.jbcrypt.BCrypt;

public class SignInActivity extends AppCompatActivity {
    private Spinner countryCodeSpinner;
    private EditText loginPhoneNumber;
    private TextInputEditText loginPassword;
    private Button loginButton;
    private ProgressBar progressBar;
    private boolean isPhoneNumberValid = false;
    private boolean isPasswordValid = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginPhoneNumber = findViewById(R.id.loginPhoneNumber);
        loginPassword = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        countryCodeSpinner = findViewById(R.id.countryCodeSpinner);

        loginButton.setOnClickListener((v) -> loginUser());
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.country_codes, R.layout.country_item);
        adapter.setDropDownViewResource(R.layout.country_item);
        countryCodeSpinner.setAdapter(adapter);

        loginButton.setEnabled(false);

        loginPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePhoneNumber(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateLoginButtonState();
            }
        });

        loginPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateLoginButtonState();
            }
        });
    }

    private void loginUser() {
        String phoneNumber = loginPhoneNumber.getText().toString();
        String password = loginPassword.getText().toString();
        String countryCode = countryCodeSpinner.getSelectedItem().toString();

        checkUserInFirestore(phoneNumber, password, countryCode);
    }

    private void checkUserInFirestore(String phoneNumber, String password, String countryCode) {
        changeInProgress(true);

        db.collection("users")
                .whereEqualTo("countryCode", countryCode)
                .whereEqualTo("phoneNumber", phoneNumber)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        changeInProgress(false);
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String hashedPassword = document.getString("password");

                            if (BCrypt.checkpw(password, hashedPassword)) {
                                String userUID = document.getId();
                                sendOtpToPhoneNumber(phoneNumber, countryCode);
                            } else {
                                loginPassword.setText("");
                                Toast.makeText(SignInActivity.this, "Invalid phone number or password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            loginPassword.setText("");
                            Toast.makeText(SignInActivity.this, "Invalid phone number or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void navigateToMainActivity(String userUID) {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("userId", userUID);
        startActivity(intent);
    }
    private void sendOtpToPhoneNumber(String phoneNumber, String countryCode) {
        changeInProgress(true);
        String completePhoneNumber = countryCode + phoneNumber;
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(completePhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        // Automatically sign in with the received OTP
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        changeInProgress(false);
                        Toast.makeText(SignInActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        changeInProgress(false);

                        Bundle bundle = new Bundle();
                        bundle.putString("reason", "Sign in");
                        bundle.putString("countryCode", countryCode);
                        bundle.putString("phoneNumber", phoneNumber);
                        bundle.putString("verificationId", verificationId);
                        bundle.putParcelable("resendingToken", forceResendingToken);

                        PagePhoneVerification fragment = new PagePhoneVerification();
                        fragment.setArguments(bundle);
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        Log.d("FragmentTransaction", "Attempting to replace fragment in container with ID: " + R.id.fragment_container);
                        transaction.replace(R.id.fragment_container, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }


    private void validatePhoneNumber(String phoneNumber) {
        isPhoneNumberValid = phoneNumber.matches("[0-9]+");

        if (!isPhoneNumberValid) {
            loginPhoneNumber.setError("Phone number should contain numbers only");
        } else {
            loginPhoneNumber.setError(null);
        }
    }

    private void validatePassword(String password) {
        isPasswordValid = password.length() >= 8;
        if (!isPasswordValid) {
            loginPassword.setError("Password should be at least 8 characters long");
        } else {
            loginPassword.setError(null);
        }
    }

    private void updateLoginButtonState() {
        loginButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.darkpink));
        loginButton.setEnabled(isPhoneNumberValid && isPasswordValid);
    }

    void changeInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
        }
    }
}
