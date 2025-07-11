package com.example.onlinebankingapp;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TransactionsActivity extends AppCompatActivity {

    RecyclerView transactionRecyclerView;
    TransactionAdapter transactionAdapter;
    UserManager userManager;
    private List<AppTransaction> transactionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transactions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userManager = UserManager.getInstance();
        transactionRecyclerView = findViewById(R.id.transactionRecyclerView);
        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        transactionAdapter = new TransactionAdapter(transactionList, TransactionsActivity.this);
        transactionRecyclerView.setAdapter(transactionAdapter);

        loadUserData();
    }

    private void loadUserData() {
        userManager.getUserData(new UserManager.UserDataCallback() {
            @Override
            public void onDataLoaded(Map<String, Object> userData) {
                if (userData != null && userData.containsKey("accounts")) {
                    Map<String, Boolean> accounts = (Map<String, Boolean>) userData.get("accounts");
                    for (String accountId : accounts.keySet()) {
                        loadTransactions(accountId);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Utility.showToast(TransactionsActivity.this, "Failed to load user data: " + e.getMessage());
            }
        });
    }

    private void loadTransactions(String accountId) {
        transactionList.clear();
        transactionAdapter.notifyDataSetChanged();
        userManager.getAccountData(accountId, new UserManager.AccountDataCallback() {
            @Override
            public void onAccountLoaded(DocumentSnapshot accountData) {
                if (accountData.exists() && accountData.contains("transactions")) {
                    Object transactionsObj = accountData.get("transactions");
                    if (transactionsObj instanceof List) {
                        List<String> transactionIds = (List<String>) transactionsObj;
                        loadTransactionsByIds(transactionIds);
                    } else if (transactionsObj instanceof Map) {
                        List<String> transactionIds = new ArrayList<>(((Map<String, Object>) transactionsObj).keySet());
                        loadTransactionsByIds(transactionIds);
                    } else {
                        Utility.showToast(TransactionsActivity.this, "Transactions data is not in expected format.");
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Utility.showToast(TransactionsActivity.this, "Failed to get account data: " + e.getMessage());
            }
        });
    }

    private void loadTransactionsByIds(List<String> transactionIds) {
        if (transactionIds != null) {
            Collections.reverse(transactionIds);
            for (String transactionId : transactionIds) {
                userManager.getTransaction(transactionId, new UserManager.TransactionCallback() {
                    @Override
                    public void onTransactionLoaded(AppTransaction transaction) {
                        transactionList.add(transaction);
                        Collections.sort(transactionList);
                        transactionAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Utility.showToast(TransactionsActivity.this, "Failed to load transaction: " + e.getMessage());
                    }
                });
            }
        }
    }
}

