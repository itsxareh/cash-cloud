package com.example.onlinebankingapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class NewPhoneNumber extends AppCompatActivity {
    private Spinner countryCodeSpinner;
    private EditText phoneNumber;
    private Button save;
    private boolean isPhoneNumberValid = false;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_phone_number);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        countryCodeSpinner = findViewById(R.id.countryCodeSpinner);
        phoneNumber = findViewById(R.id.newPhoneNumber);
        save = findViewById(R.id.save);
        save.setEnabled(false);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.country_codes, R.layout.country_item);
        adapter.setDropDownViewResource(R.layout.country_item);
        countryCodeSpinner.setAdapter(adapter);

        phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePhoneNumber(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateContinueButtonState();
            }
        });
        save.setOnClickListener(v -> {
            String countryCode = countryCodeSpinner.getSelectedItem().toString();
            String newNumber = phoneNumber.getText().toString().trim();
            if (isPhoneNumberValid) {
                checkIfPhoneExists(countryCode, newNumber);
            }
        });
    }
    private void validatePhoneNumber(String newNumber) {
        isPhoneNumberValid = newNumber.matches("[0-9]+");

        if (!isPhoneNumberValid) {
            phoneNumber.setError("Phone number should contain numbers only");
        } else {
            phoneNumber.setError(null);
        }
    }
    private void updateContinueButtonState() {
        save.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.darkpink));
        save.setEnabled(isPhoneNumberValid);
    }
    private void checkIfPhoneExists(String countryCode, String phoneNumber) {
        db.collection("users")
                .whereEqualTo("countryCode", countryCode)
                .whereEqualTo("phoneNumber", phoneNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            Utility.showToast(this, "Phone number already exists!");
                        } else {
                            sendOtpToPhoneNumber(phoneNumber, countryCode);
                        }
                    } else {
                        Utility.showToast(this, "Failed to check user existence: " + task.getException().getMessage());
                    }
                });
    }
    private void sendOtpToPhoneNumber(String phoneNumber, String countryCode){
        String completePhoneNumber = countryCode + phoneNumber;
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(completePhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(NewPhoneNumber.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                        PagePhoneVerification fragment = new PagePhoneVerification();
                        Bundle bundle = new Bundle();
                        bundle.putString("uid", mAuth.getCurrentUser().getUid());
                        bundle.putString("reason", "New Number");
                        bundle.putString("countryCode", countryCode);
                        bundle.putString("phoneNumber", phoneNumber);
                        bundle.putString("verificationId", verificationId);
                        bundle.putParcelable("resendingToken", forceResendingToken);

                        fragment.setArguments(bundle);
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();

                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

}