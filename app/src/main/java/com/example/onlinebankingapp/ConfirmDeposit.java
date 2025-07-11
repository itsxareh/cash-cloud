package com.example.onlinebankingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class ConfirmDeposit extends AppCompatActivity {
    private FirebaseFirestore db;
    private String walletAccountId;
    private String savingsAccountId;

    Button depositBtn;
    TextView destinationText, sourceText, amountText, sourceDescriptionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.confirm_deposit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        String sourceDescription = intent.getStringExtra("sourceDescription");
        String source = intent.getStringExtra("source");
        String destination = intent.getStringExtra("destination");
        String phoneNumber = intent.getStringExtra("phoneNumber");
        String countryCode = intent.getStringExtra("countryCode");
        double depositAmount = intent.getDoubleExtra("amount", 0.00);
        walletAccountId = intent.getStringExtra("walletAccountId");
        savingsAccountId = intent.getStringExtra("savingsAccountId");

        depositBtn = findViewById(R.id.depositBtn);
        destinationText = findViewById(R.id.destination);
        sourceDescriptionText = findViewById(R.id.sourceDescriptionText);
        sourceText = findViewById(R.id.source);
        amountText = findViewById(R.id.amountText);

        amountText.setText(String.format(Locale.getDefault(), "%.2f", depositAmount));
        sourceDescriptionText.setText(sourceDescription);
        sourceText.setText(source);
        destinationText.setText(destination);

        depositBtn.setOnClickListener(v -> {
            final DialogPlus dialogPlus = DialogPlus.newDialog(this)
                    .setContentHolder(new ViewHolder(R.layout.dialogdeposited))
                    .setExpanded(true, 800)
                    .create();
            View myview = dialogPlus.getHolderView();
            final Button done = myview.findViewById(R.id.done);
            final TextView amount = myview.findViewById(R.id.amountText);
            final Button receipt = myview.findViewById(R.id.viewReceipt);

            amount.setText(String.format(Locale.getDefault(), "%.2f", depositAmount));

            updateBalancesAndTransactions(depositAmount);

            dialogPlus.show();

            receipt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ConfirmDeposit.this, MainActivity.class));
                }
            });
            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ConfirmDeposit.this, MainActivity.class));
                }
            });
        });
    }

    private void updateBalancesAndTransactions(double depositAmount) {
        DocumentReference walletAccountRef = db.collection("accounts").document(walletAccountId);
        DocumentReference savingsAccountRef = db.collection("accounts").document(savingsAccountId);

        db.runTransaction(transaction -> {
            // Read the wallet account balance
            Log.d("FirestoreTransaction", "Reading wallet account balance...");
            DocumentSnapshot walletSnapshot = transaction.get(walletAccountRef);
            double currentWalletBalance = walletSnapshot.getDouble("balance");
            Log.d("FirestoreTransaction", "Current wallet balance: " + currentWalletBalance);

            // Read the savings account balance
            Log.d("FirestoreTransaction", "Reading savings account balance...");
            DocumentSnapshot savingsSnapshot = transaction.get(savingsAccountRef);
            double currentSavingsBalance = savingsSnapshot.getDouble("balance");
            Log.d("FirestoreTransaction", "Current savings balance: " + currentSavingsBalance);

            // Perform the balance updates
            double newWalletBalance = currentWalletBalance - depositAmount;
            double newSavingsBalance = currentSavingsBalance + depositAmount;

            transaction.update(walletAccountRef, "balance", newWalletBalance);
            transaction.update(savingsAccountRef, "balance", newSavingsBalance);
            Log.d("FirestoreTransaction", "Updated balances: new wallet balance = " + newWalletBalance + ", new savings balance = " + newSavingsBalance);

            // Create the wallet transaction document
            String walletTransactionId = UUID.randomUUID().toString();
            Map<String, Object> walletTransaction = new HashMap<>();
            walletTransaction.put("amount", -depositAmount);
            walletTransaction.put("dateTime", Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila")).getTimeInMillis());
            walletTransaction.put("type", "Withdrawal");
            walletTransaction.put("reference", walletTransactionId);
            walletTransaction.put("description", "Transfer to savings");

            // Create the savings transaction document
            String savingsTransactionId = UUID.randomUUID().toString();
            Map<String, Object> savingsTransaction = new HashMap<>();
            savingsTransaction.put("amount", depositAmount);
            savingsTransaction.put("dateTime", Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila")).getTimeInMillis());
            savingsTransaction.put("type", "Deposit");
            savingsTransaction.put("reference", savingsTransactionId);
            savingsTransaction.put("description", "Transfer from wallet");

            // Set the transactions in the transactions collection
            CollectionReference transactionsRef = db.collection("transactions");
            transaction.set(transactionsRef.document(walletTransactionId), walletTransaction);
            Log.d("FirestoreTransaction", "Set wallet transaction with ID: " + walletTransactionId);
            transaction.set(transactionsRef.document(savingsTransactionId), savingsTransaction);
            Log.d("FirestoreTransaction", "Set savings transaction with ID: " + savingsTransactionId);

            // Update the transactions arrays in both accounts
            transaction.update(walletAccountRef, "transactions", FieldValue.arrayUnion(walletTransactionId));
            transaction.update(savingsAccountRef, "transactions", FieldValue.arrayUnion(savingsTransactionId));
            Log.d("FirestoreTransaction", "Updated transaction arrays in wallet and savings accounts.");

            return null;
        }).addOnSuccessListener(aVoid -> {
            runOnUiThread(() -> Utility.showToast(ConfirmDeposit.this, "Deposit successful."));
            Log.d("FirestoreTransaction", "Transaction successful.");
        }).addOnFailureListener(e -> {
            runOnUiThread(() -> Utility.showToast(ConfirmDeposit.this, "Deposit failed: " + e.getMessage()));
            Log.e("FirestoreTransaction", "Transaction failed: " + e.getMessage(), e);
        });
    }
}
