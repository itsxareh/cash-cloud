package com.example.onlinebankingapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;
import java.util.Map;

public class CashInActivity extends AppCompatActivity {
    UserManager userManager;

    Button cashCloudSavings, cashCloudCenter, debitCreditCard;
    ImageButton dobButton, bdoButton, grabPayButton, billEaseButton, landBankButton, dobubpButton, metroBankButton, paypalButton, gCashButton, mayaButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cash_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        userManager = UserManager.getInstance();


        cashCloudSavings = findViewById(R.id.cashCloudSavings);
        debitCreditCard = findViewById(R.id.debitCreditCard);

        bdoButton = findViewById(R.id.bdoButton);
        dobButton = findViewById(R.id.dobButton);
        grabPayButton = findViewById(R.id.grabPayButton);
        landBankButton = findViewById(R.id.landBankButton);
        billEaseButton = findViewById(R.id.billEaseButton);
        dobubpButton = findViewById(R.id.dobubpButton);
        metroBankButton = findViewById(R.id.metroBankButton);
        gCashButton = findViewById(R.id.gCashButton);
        mayaButton = findViewById(R.id.mayaButton);

        debitCreditCard.setOnClickListener(v -> {
            Intent intent = new Intent(CashInActivity.this, CashIn_Amount.class);
            intent.putExtra("paymentMethod", "Debit/Credit Card");
            startActivity(intent);
        });
//        paypalButton.setOnClickListener(v -> {
//            Intent intent = new Intent(CashInActivity.this, CashIn_Amount.class);
//            intent.putExtra("paymentMethod", "Paypal");
//            startActivity(intent);
//        });
        gCashButton.setOnClickListener(v -> {
            Intent intent = new Intent(CashInActivity.this, CashIn_Amount.class);
            intent.putExtra("paymentMethod", "GCash");
            startActivity(intent);
        });
        mayaButton.setOnClickListener(v -> {
            Intent intent = new Intent(CashInActivity.this, CashIn_Amount.class);
            intent.putExtra("paymentMethod", "Maya");
            startActivity(intent);
        });
        bdoButton.setOnClickListener(v -> {
            Intent intent = new Intent(CashInActivity.this, CashIn_Amount.class);
            intent.putExtra("paymentMethod", "BDO");
            startActivity(intent);
        });
        dobButton.setOnClickListener(v -> {
            Intent intent = new Intent(CashInActivity.this, CashIn_Amount.class);
            intent.putExtra("paymentMethod", "DOB");
            startActivity(intent);
        });
        grabPayButton.setOnClickListener(v -> {
            Intent intent = new Intent(CashInActivity.this, CashIn_Amount.class);
            intent.putExtra("paymentMethod", "GrabPay");
            startActivity(intent);
        });
        landBankButton.setOnClickListener(v -> {
            Intent intent = new Intent(CashInActivity.this, CashIn_Amount.class);
            intent.putExtra("paymentMethod", "LandBank");
            startActivity(intent);
        });
        billEaseButton.setOnClickListener(v -> {
            Intent intent = new Intent(CashInActivity.this, CashIn_Amount.class);
            intent.putExtra("paymentMethod", "BillEase");
            startActivity(intent);
        });
        dobubpButton.setOnClickListener(v -> {
            Intent intent = new Intent(CashInActivity.this, CashIn_Amount.class);
            intent.putExtra("paymentMethod", "DOBUBP");
            startActivity(intent);
        });
        metroBankButton.setOnClickListener(v -> {
            Intent intent = new Intent(CashInActivity.this, CashIn_Amount.class);
            intent.putExtra("paymentMethod", "MetroBank");
            startActivity(intent);
        });

    }

}