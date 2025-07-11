package com.example.onlinebankingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class MySavingsAccount extends AppCompatActivity {
    private boolean isBalanceVisible = true;
    ImageButton showBalanceButton;
    UserManager userManager;
    Button depositAccount;
    TextView balanceAmount, accountNumber;
    private String walletAccountId = "";
    private String savingsAccountId = "";
    private String walletBalance = "";
    private String currentBalance = "";
    private String savingsNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.my_savings_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        userManager = UserManager.getInstance();
        depositAccount = findViewById(R.id.depositAccount);
        showBalanceButton = findViewById(R.id.showBalanceButton);
        balanceAmount = findViewById(R.id.balanceAmount);
        accountNumber = findViewById(R.id.accountNumber);

        showBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBalanceVisible) {
                    balanceAmount.setText("••••••••");
                    accountNumber.setText("••••••••");
                    showBalanceButton.setImageResource(R.drawable.baseline_visibility_off_24);
                } else {
                    balanceAmount.setText(currentBalance);
                    accountNumber.setText(savingsNumber);
                    showBalanceButton.setImageResource(R.drawable.baseline_remove_red_eye_24);
                }
                isBalanceVisible = !isBalanceVisible;
            }
        });

        depositAccount.setOnClickListener(v -> {
            final DialogPlus dialogPlus = DialogPlus.newDialog(this)
                    .setContentHolder(new ViewHolder(R.layout.dialogdepositmoney))
                    .setExpanded(true, 500)
                    .create();
            View myview = dialogPlus.getHolderView();
            final Button mywallet = myview.findViewById(R.id.mywallet);
            dialogPlus.show();

            mywallet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MySavingsAccount.this, DepositAmount.class);
                    double walletBalanceDouble = Double.parseDouble(walletBalance.replace(",", ""));
                    intent.putExtra("sourceDescription", "My Wallet");
                    intent.putExtra("destination", savingsNumber);
                    intent.putExtra("walletBalance", walletBalanceDouble);
                    intent.putExtra("walletAccountId", walletAccountId);
                    intent.putExtra("savingsAccountId", savingsAccountId);
                    Log.d("dad", savingsAccountId);
                    Log.d("dad", walletAccountId);
                    startActivity(intent);
                }
            });
        });

        loadUserData();
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
                                if (accountData.exists()) {
                                    String accountType = accountData.getString("accountType");
                                    Double balance = accountData.getDouble("balance");
                                    String storedAccountNumber = accountData.getString("accountNumber");

                                    if ("savings".equals(accountType)) {
                                        if (balance != null) {
                                            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                                            numberFormat.setMinimumFractionDigits(2);
                                            numberFormat.setMaximumFractionDigits(2);
                                            currentBalance = numberFormat.format(balance);
                                            balanceAmount.setText(currentBalance);
                                            accountNumber.setText(storedAccountNumber);
                                            savingsNumber = accountNumber.getText().toString();
                                            savingsAccountId = accountId;
                                        } else {
                                            currentBalance = "0.00";
                                            balanceAmount.setText(currentBalance);
                                        }
                                    } else if ("wallet".equals(accountType)) {
                                        if (balance != null) {
                                            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                                            numberFormat.setMinimumFractionDigits(2);
                                            numberFormat.setMaximumFractionDigits(2);
                                            walletBalance = numberFormat.format(balance);
                                            walletAccountId = accountId;
                                        } else {
                                            walletBalance = "0.00";
                                        }
                                    }
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
}
