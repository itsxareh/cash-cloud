package com.example.onlinebankingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WalletFragment extends Fragment {

    Button cashInButton, sendButton;
    ImageButton bankServiceButton, cardsServiceButton, savingsServiceButton, showBalanceButton,
            stocksServiceButton, loadServiceButton, billsButton, cryptoButton, moreButton;
    TextView balanceAmount, balanceText, seeAllButton;
    RecyclerView transactionRecyclerView;
    TransactionAdapter transactionAdapter;
    UserManager userManager;
    private List<AppTransaction> transactionList = new ArrayList<>();
    private boolean isBalanceVisible = true;
    private String currentBalance = "";

    public WalletFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.wallet_fragment, container, false);

        showBalanceButton = rootView.findViewById(R.id.showBalanceButton);
        cashInButton = rootView.findViewById(R.id.cashInButton);
        sendButton = rootView.findViewById(R.id.sendButton);
        cardsServiceButton = rootView.findViewById(R.id.cardsServiceButton);
        savingsServiceButton = rootView.findViewById(R.id.savingsServiceButton);
        loadServiceButton = rootView.findViewById(R.id.loadServiceButton);

        balanceAmount = rootView.findViewById(R.id.balanceAmount);
        balanceText = rootView.findViewById(R.id.balanceText);
        seeAllButton = rootView.findViewById(R.id.seeAllButton);

        transactionRecyclerView = rootView.findViewById(R.id.transactionRecyclerView);
        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        transactionAdapter = new TransactionAdapter(transactionList, getContext());
        transactionRecyclerView.setAdapter(transactionAdapter);

        cashInButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), CashInActivity.class)));
        sendButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), SendActivity.class)));

        seeAllButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), TransactionsActivity.class)));
        userManager = UserManager.getInstance();

        loadUserData();

        cardsServiceButton.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                mainActivity.getViewPager().setCurrentItem(4);
            }
        });
        savingsServiceButton.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                mainActivity.getViewPager().setCurrentItem(1);
            }
        });
        loadServiceButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), ShopActivity.class)));
        showBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBalanceVisible) {
                    balanceAmount.setText("••••••••");
                    showBalanceButton.setImageResource(R.drawable.baseline_visibility_off_24);
                } else {
                    balanceAmount.setText(currentBalance);
                    showBalanceButton.setImageResource(R.drawable.baseline_remove_red_eye_24);
                }
                isBalanceVisible = !isBalanceVisible;
            }
        });

        if (getActivity().getIntent().getBooleanExtra("updateWallet", false)) {
            getActivity().getIntent().removeExtra("updateWallet");
            transactionAdapter.notifyDataSetChanged();
        }

        return rootView;
    }

    private void loadUserData() {
        userManager.getUserData(new UserManager.UserDataCallback() {
            @Override
            public void onDataLoaded(Map<String, Object> userData) {
                if (userData != null && userData.containsKey("accounts")) {
                    Map<String, Boolean> accounts = (Map<String, Boolean>) userData.get("accounts");
                    for (String accountId : accounts.keySet()) {
                        userManager.getAccountData(accountId, new UserManager.AccountDataCallback() {
                            @Override
                            public void onAccountLoaded(DocumentSnapshot accountData) {
                                if (accountData.exists() && "wallet".equals(accountData.getString("accountType"))) {
                                    Double balance = accountData.getDouble("balance");
                                    if (balance != null) {
                                        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                                        numberFormat.setMinimumFractionDigits(2);
                                        numberFormat.setMaximumFractionDigits(2);
                                        currentBalance = numberFormat.format(balance);
                                        balanceAmount.setText(currentBalance);
                                    } else {
                                        currentBalance = "0.00";
                                        balanceAmount.setText(currentBalance);
                                    }
                                    loadTransactions(accountId);
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                balanceAmount.setText("Error");
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                balanceAmount.setText("Error");
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
                        if (transactionIds != null) {
                            Collections.reverse(transactionIds);
                            int count = 0;
                            for (String transactionId : transactionIds) {
                                if (count >= 3) {
                                    break;
                                }
                                userManager.getTransaction(transactionId, new UserManager.TransactionCallback() {
                                    @Override
                                    public void onTransactionLoaded(AppTransaction transaction) {
                                        transactionList.add(transaction);
                                        Collections.sort(transactionList);
                                        transactionAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Utility.showToast(getActivity(), "Failed to load transaction: " + e.getMessage());
                                    }
                                });
                                count++;
                            }
                        }
                    } else if (transactionsObj instanceof Map) {
                        Map<String, Object> transactionMap = (Map<String, Object>) transactionsObj;
                        List<String> transactionIds = new ArrayList<>(transactionMap.keySet());
                        Collections.reverse(transactionIds);
                        int count = 0;
                        for (String transactionId : transactionIds) {
                            if (count >= 3) {
                                break;
                            }
                            userManager.getTransaction(transactionId, new UserManager.TransactionCallback() {
                                @Override
                                public void onTransactionLoaded(AppTransaction transaction) {
                                    transactionList.add(transaction);
                                    Collections.sort(transactionList);
                                    transactionAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Utility.showToast(getActivity(), "Failed to load transaction: " + e.getMessage());
                                }
                            });
                            count++;
                        }
                    } else {
                        Utility.showToast(getActivity(), "Unexpected data type for transactions field");
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Utility.showToast(getActivity(), "Failed to get account data: " + e.getMessage());
            }
        });
    }
}
