package com.example.onlinebankingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ReceiptActivity extends AppCompatActivity {
    TextView recipientName, recipientNo, amount, total, reference, dateTimeText, receiptTitle, successfullySent;
    Button okayButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receipt);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        receiptTitle = findViewById(R.id.receiptTitle);
        successfullySent = findViewById(R.id.successfullySent);
        recipientName = findViewById(R.id.recipientName);
        recipientNo = findViewById(R.id.recipientNo);
        amount = findViewById(R.id.amount);
        total = findViewById(R.id.total);
        dateTimeText = findViewById(R.id.datetimeText);
        reference = findViewById(R.id.reference);
        okayButton = findViewById(R.id.okayButton);

        if(getIntent() != null){
            String titlePage = getIntent().getStringExtra("titlePage");
            if(titlePage != null && titlePage.equals("Cash in")){
                receiptTitle.setText(titlePage);
                String description = getIntent().getStringExtra("description");

                successfullySent.setText("Successfully received");
                recipientName.setText(description);
                recipientNo.setText("Payment method");

            } else if (titlePage != null && titlePage.equals("Withdrawal")){
                receiptTitle.setText(titlePage);
                String description = getIntent().getStringExtra("description");

                successfullySent.setText("Successfully sent");
                recipientName.setText(description);
                recipientNo.setText("");
            } else if (titlePage != null && titlePage.equals("Deposit")){
                receiptTitle.setText(titlePage);
                String description = getIntent().getStringExtra("description");

                successfullySent.setText("Successfully received");
                recipientName.setText(description);
                recipientNo.setText("");
            } else if (titlePage != null && titlePage.equals("Received money")){
                receiptTitle.setText(titlePage);
                String description = getIntent().getStringExtra("description");

                successfullySent.setText("Received money");
                recipientName.setText(description);
                recipientNo.setText("Sender");
            } else {

                String fullName = getIntent().getStringExtra("fullName");
                String countryCode = getIntent().getStringExtra("countryCode");
                String phoneNumber = getIntent().getStringExtra("phoneNumber");

                receiptTitle.setText(titlePage);
                recipientName.setText(fullName);
                recipientNo.setText(countryCode + " " + phoneNumber);
            }
        }

        long dateTimeMillis = getIntent().getLongExtra("dateTime", 0);
        double sendAmount = getIntent().getDoubleExtra("amount", 0);
        String referenceNumber = getIntent().getStringExtra("reference");

        amount.setText("₱" + String.format(Locale.getDefault(), "%.2f", Math.abs(sendAmount)));
        total.setText("₱" + String.format(Locale.getDefault(), "%.2f", Math.abs(sendAmount)));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
        dateTimeText.setText(sdf.format(new Date(dateTimeMillis)));
        reference.setText(referenceNumber);

        okayButton.setOnClickListener(v -> {
            Intent intent = new Intent(ReceiptActivity.this, MainActivity.class);
            intent.putExtra("updateWallet", true);
            startActivity(intent);
        });
    }
}
