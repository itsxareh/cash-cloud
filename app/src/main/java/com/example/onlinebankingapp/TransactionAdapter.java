package com.example.onlinebankingapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private List<AppTransaction> transactionList;
    private Context context;
    private FirebaseFirestore db;

    public TransactionAdapter(List<AppTransaction> transactionList, Context context) {
        this.transactionList = transactionList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        AppTransaction transaction = transactionList.get(position);
        holder.transactionPeso.setText("₱");
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        holder.transactionAmount.setText(numberFormat.format(transaction.getAmount()));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
        holder.transactionDate.setText(sdf.format(new Date(transaction.getDate())));
        holder.transactionType.setText(transaction.getType());
        holder.transactionDescription.setText(transaction.getDescription());

        holder.itemView.setOnClickListener(v -> {
            handleTransactionClick(transaction);
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    private void handleTransactionClick(AppTransaction transaction) {
        String transactionType = transaction.getType();
        Intent intent = new Intent(context, ReceiptActivity.class);
        intent.putExtra("titlePage", transaction.getType());
        intent.putExtra("amount", transaction.getAmount());
        intent.putExtra("dateTime", transaction.getDate());
        intent.putExtra("description", transaction.getDescription());
        intent.putExtra("type", transactionType);
        intent.putExtra("reference", transaction.getReference());

        // Log the date for debugging
        Log.d("TransactionAdapter", "DATE TIME: " + transaction.getDescription());

        if ("Send money".equals(transactionType) || "Received money".equals(transactionType)) {
            String reference = transaction.getReference();
            intent.putExtra("reference", reference);

            db.collection("transactions")
                    .whereEqualTo("reference", reference)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            String userId = documentSnapshot.getString("userId");
                            getUserDetails(userId, transactionType, intent);
                        } else {
                            Utility.showToast(context, "Transaction details not found.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Utility.showToast(context, "Failed to retrieve transaction details: " + e.getMessage());
                    });
        } else {
            context.startActivity(intent);
        }
    }


    private void getUserDetails(String userId, String transactionType, Intent intent) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String fullName = documentSnapshot.getString("fullName");
                String countryCode = documentSnapshot.getString("countryCode");
                String phoneNumber = documentSnapshot.getString("phoneNumber");

                intent.putExtra("fullName", fullName);
                intent.putExtra("countryCode", countryCode);
                intent.putExtra("phoneNumber", phoneNumber);
                context.startActivity(intent);
            } else {
                Utility.showToast(context, "User details not found.");
            }
        }).addOnFailureListener(e -> {
            Utility.showToast(context, "Failed to retrieve user details: " + e.getMessage());
        });
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView transactionPeso, transactionAmount, transactionDate, transactionType, transactionDescription;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionPeso = itemView.findViewById(R.id.transactionPeso);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            transactionDate = itemView.findViewById(R.id.transactionDate);
            transactionType = itemView.findViewById(R.id.transactionType);
            transactionDescription = itemView.findViewById(R.id.transactionDescription);
        }
    }
}
