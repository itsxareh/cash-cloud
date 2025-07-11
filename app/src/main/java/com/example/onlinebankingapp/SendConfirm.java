package com.example.onlinebankingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class SendConfirm extends AppCompatActivity {
    private TextView recipientNameTextView, phoneNumberTextView, amountTextView, totalTextView;
    private Button confirmButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String referenceNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.send_confirm);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recipientNameTextView = findViewById(R.id.accountName);
        phoneNumberTextView = findViewById(R.id.accountNumber);
        amountTextView = findViewById(R.id.amount);
        totalTextView = findViewById(R.id.total);
        confirmButton = findViewById(R.id.confirmButton);

        String fullName = getIntent().getStringExtra("fullName");
        String countryCode = getIntent().getStringExtra("countryCode");
        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        double amount = getIntent().getDoubleExtra("amount", 0);
        String recipientUserId = getIntent().getStringExtra("userId");

        recipientNameTextView.setText(fullName);
        phoneNumberTextView.setText(countryCode + " " + phoneNumber);
        amountTextView.setText(String.format(Locale.getDefault(), "%.2f", amount));
        totalTextView.setText(String.format(Locale.getDefault(), "%.2f", amount));

        confirmButton.setOnClickListener(v -> sendMoney(amount, recipientUserId, fullName, countryCode, phoneNumber));
    }

    private void sendMoney(double amount, String recipientUserId, String fullName, String countryCode, String phoneNumber) {
        FirebaseUser user = mAuth.getCurrentUser();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
        long currentTimeMillis = calendar.getTimeInMillis();
        referenceNumber = UUID.randomUUID().toString();

        if (user != null) {
            String currentUserId = user.getUid();

            db.runTransaction((Transaction.Function<Void>) transaction -> {
                DocumentSnapshot currentUserSnapshot = transaction.get(db.collection("users").document(currentUserId));
                DocumentSnapshot recipientSnapshot = transaction.get(db.collection("users").document(recipientUserId));

                if (!currentUserSnapshot.exists() || !recipientSnapshot.exists()) {
                    throw new FirebaseFirestoreException("User data not found.", FirebaseFirestoreException.Code.NOT_FOUND);
                }

                Map<String, Object> accountsMap = (Map<String, Object>) currentUserSnapshot.get("accounts");
                if (accountsMap == null) {
                    throw new FirebaseFirestoreException("Accounts data not found.", FirebaseFirestoreException.Code.NOT_FOUND);
                }
                String currentUserWalletId = null;
                for (String accountId : accountsMap.keySet()) {
                    DocumentSnapshot accountSnapshot = transaction.get(db.collection("accounts").document(accountId));
                    if (accountSnapshot.exists() && "wallet".equals(accountSnapshot.getString("accountType"))) {
                        currentUserWalletId = accountId;
                        break;
                    }
                }

                if (currentUserWalletId == null) {
                    throw new FirebaseFirestoreException("Current user wallet not found.", FirebaseFirestoreException.Code.NOT_FOUND);
                }

                accountsMap = (Map<String, Object>) recipientSnapshot.get("accounts");
                if (accountsMap == null) {
                    throw new FirebaseFirestoreException("Accounts data not found.", FirebaseFirestoreException.Code.NOT_FOUND);
                }
                String recipientWalletId = null;
                for (String accountId : accountsMap.keySet()) {
                    DocumentSnapshot accountSnapshot = transaction.get(db.collection("accounts").document(accountId));
                    if (accountSnapshot.exists() && "wallet".equals(accountSnapshot.getString("accountType"))) {
                        recipientWalletId = accountId;
                        break;
                    }
                }

                if (recipientWalletId == null) {
                    throw new FirebaseFirestoreException("Recipient wallet not found.", FirebaseFirestoreException.Code.NOT_FOUND);
                }

                DocumentSnapshot currentUserWalletSnapshot = transaction.get(db.collection("accounts").document(currentUserWalletId));
                Double currentUserBalance = currentUserWalletSnapshot.getDouble("balance");
                if (currentUserBalance == null) {
                    throw new FirebaseFirestoreException("Current user balance not found.", FirebaseFirestoreException.Code.NOT_FOUND);
                }

                if (currentUserBalance < amount) {
                    throw new FirebaseFirestoreException("Insufficient funds.", FirebaseFirestoreException.Code.ABORTED);
                }

                DocumentSnapshot recipientWalletSnapshot = transaction.get(db.collection("accounts").document(recipientWalletId));
                Double recipientBalance = recipientWalletSnapshot.getDouble("balance");
                if (recipientBalance == null) {
                    recipientBalance = 0.0;
                }

                transaction.update(db.collection("accounts").document(currentUserWalletId), "balance", currentUserBalance - amount);
                transaction.update(db.collection("accounts").document(recipientWalletId), "balance", recipientBalance + amount);

                String currentUserTransactionId = db.collection("transactions").document().getId();
                Map<String, Object> currentUserTransaction = new HashMap<>();
                currentUserTransaction.put("amount", -amount);
                currentUserTransaction.put("dateTime", currentTimeMillis);
                currentUserTransaction.put("type", "Send money");
                currentUserTransaction.put("description", "to " + recipientSnapshot.getString("phoneNumber"));
                currentUserTransaction.put("userId", currentUserId);
                currentUserTransaction.put("reference", referenceNumber);

                DocumentReference currentUserTransactionRef = db.collection("transactions").document(currentUserTransactionId);
                transaction.set(currentUserTransactionRef, currentUserTransaction);

                transaction.update(db.collection("accounts").document(currentUserWalletId), "transactions", FieldValue.arrayUnion(currentUserTransactionId));

                String recipientTransactionId = db.collection("transactions").document().getId();
                Map<String, Object> recipientTransaction = new HashMap<>();
                recipientTransaction.put("amount", amount);
                recipientTransaction.put("dateTime", currentTimeMillis);
                recipientTransaction.put("type", "Received money");
                recipientTransaction.put("description", "from " + currentUserSnapshot.getString("phoneNumber"));
                recipientTransaction.put("userId", recipientUserId);
                recipientTransaction.put("reference", referenceNumber);

                DocumentReference recipientTransactionRef = db.collection("transactions").document(recipientTransactionId);
                transaction.set(recipientTransactionRef, recipientTransaction);

                transaction.update(db.collection("accounts").document(recipientWalletId), "transactions", FieldValue.arrayUnion(recipientTransactionId));

                return null;
            }).addOnSuccessListener(aVoid -> {
                Intent intent = new Intent(SendConfirm.this, ReceiptActivity.class);
                intent.putExtra("titlePage", "Send money");
                intent.putExtra("fullName", fullName);
                intent.putExtra("phoneNumber", phoneNumber);
                intent.putExtra("countryCode", countryCode);
                intent.putExtra("dateTime", currentTimeMillis);
                intent.putExtra("amount", amount);
                intent.putExtra("reference", referenceNumber);

                startActivity(intent);
                finish();
            }).addOnFailureListener(e -> {
                Utility.showToast(SendConfirm.this, "Transaction failed: " + e.getMessage());
                Log.e("SendConfirm", "Transaction failed", e);
            });
        }
    }
}
