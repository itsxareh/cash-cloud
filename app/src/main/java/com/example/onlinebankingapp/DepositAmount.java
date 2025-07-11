package com.example.onlinebankingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class DepositAmount extends AppCompatActivity {
    private EditText depositAmount;
    private Button continueBtn;
    private TextView currentBalance;
    private String countryCode, phoneNumber;
    private UserManager userManager;
    private String formatedWalletBalance = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.deposit_amount);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String sourceDescription = intent.getStringExtra("sourceDescription");
        String destination = intent.getStringExtra("destination");
        double walletBalance = intent.getDoubleExtra("walletBalance", 0.00);
        String walletAccountId = intent.getStringExtra("walletAccountId");
        String savingsAccountId = intent.getStringExtra("savingsAccountId");
        depositAmount = findViewById(R.id.depositAmount);
        continueBtn = findViewById(R.id.continueBtn);
        currentBalance = findViewById(R.id.currentBalance);
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        formatedWalletBalance = numberFormat.format(walletBalance);
        currentBalance.setText("You have ₱" + formatedWalletBalance + " in your wallet");

        userManager = UserManager.getInstance();
        loadUserPhoneNumber();

        continueBtn.setOnClickListener(v -> {
            String amount = depositAmount.getText().toString();
            if (!amount.isEmpty()) {
                double amountValue = Double.parseDouble(amount);
                if (amountValue < 1) {
                    Utility.showToast(DepositAmount.this, "Amount must be at least ₱1.00.");
                } else if (amountValue > walletBalance) {
                    Utility.showToast(DepositAmount.this, "Insufficient wallet balance.");
                } else {
                    Intent cintent = new Intent(DepositAmount.this, ConfirmDeposit.class);
                    cintent.putExtra("amount", amountValue);
                    cintent.putExtra("source", countryCode + " " + phoneNumber);
                    cintent.putExtra("sourceDescription", sourceDescription);
                    cintent.putExtra("destination", destination);
                    cintent.putExtra("walletAccountId", walletAccountId);
                    cintent.putExtra("savingsAccountId", savingsAccountId);
                    startActivity(cintent);
                }
            } else {
                Utility.showToast(DepositAmount.this, "Please enter an amount.");
            }
        });
    }

    private void loadUserPhoneNumber() {
        userManager.getUserData(new UserManager.UserDataCallback() {
            @Override
            public void onDataLoaded(Map<String, Object> userData) {
                if (userData != null && userData.containsKey("phoneNumber") && userData.containsKey("countryCode")) {
                    phoneNumber = (String) userData.get("phoneNumber");
                    countryCode = (String) userData.get("countryCode");
                    Log.d("dad", phoneNumber);
                }
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }
}
