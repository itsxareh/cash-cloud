package com.example.onlinebankingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SendActivity extends AppCompatActivity {
    UserManager userManager;
    private Spinner countryCodeSpinner;
    private EditText phoneNumber, amount;
    private Button amount100, amount500, amount1000, amount2000, sendButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_send);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        sendButton = findViewById(R.id.sendButton);
        countryCodeSpinner = findViewById(R.id.countryCodeSpinner);
        phoneNumber = findViewById(R.id.recipientPhoneNumber);
        amount = findViewById(R.id.amount);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.country_codes, R.layout.country_item);
        adapter.setDropDownViewResource(R.layout.country_item);
        countryCodeSpinner.setAdapter(adapter);

        sendButton.setEnabled(false);

        amount100 = findViewById(R.id.amount100);
        amount500 = findViewById(R.id.amount500);
        amount1000 = findViewById(R.id.amount1000);
        amount2000 = findViewById(R.id.amount2000);

        amount100.setOnClickListener(v -> setAmounts("100.00"));
        amount500.setOnClickListener(v -> setAmounts("500.00"));
        amount1000.setOnClickListener(v -> setAmounts("1000.00"));
        amount2000.setOnClickListener(v -> setAmounts("2000.00"));

        userManager = UserManager.getInstance();
        sendButton.setOnClickListener(v -> {
            validateAndSendMoney();
        });

        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String amountText = s.toString();

                boolean isNotEmpty = !amountText.isEmpty();
                sendButton.setEnabled(isNotEmpty);
                if (isNotEmpty) {
                    sendButton.setBackgroundTintList(ContextCompat.getColorStateList(SendActivity.this, R.color.darkpink));
                } else {
                    sendButton.setBackgroundResource(0);
                }
            }
        });
    }

    private void setAmounts(String value) {
        amount.setText(value);
    }

    private void validateAndSendMoney() {
        String amountText = amount.getText().toString();
        String recipientNo = phoneNumber.getText().toString();
        String countryCode = countryCodeSpinner.getSelectedItem().toString();

        if (amountText.isEmpty() || recipientNo.isEmpty()) {
            Utility.showToast(SendActivity.this, "Please enter all required fields.");
            return;
        }

        double amountValue = Double.parseDouble(amountText);
        if (amountValue < 1) {
            Utility.showToast(SendActivity.this, "Amount must be at least ₱1.00.");
            return;
        }

        checkPhoneNumberInDatabase(countryCode, recipientNo, amountValue);
    }

    private void checkPhoneNumberInDatabase(String countryCode, String phoneNumber, double amount) {
        db.collection("users")
                .whereEqualTo("countryCode", countryCode)
                .whereEqualTo("phoneNumber", phoneNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String fullName = document.getString("fullName");
                        String userId = document.getId();
                        proceedToSendConfirm(fullName, countryCode, phoneNumber, amount, userId);
                    } else {
                        Utility.showToast(SendActivity.this, "Account number not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Utility.showToast(SendActivity.this, "Failed to check phone number: " + e.getMessage());
                });
    }

    private void proceedToSendConfirm(String fullName, String countryCode, String phoneNumber, double amount, String userId) {
        Intent intent = new Intent(SendActivity.this, SendConfirm.class);
        intent.putExtra("fullName", fullName);
        intent.putExtra("countryCode", countryCode);
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("amount", amount);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }
}
