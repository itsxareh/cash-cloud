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

import com.google.firebase.firestore.DocumentSnapshot;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class AllSetSavings extends AppCompatActivity {
    TextView openingDate, accountNumber, accountName;
    Button done;
    UserManager userManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_set_savings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        userManager = UserManager.getInstance();
        accountName = findViewById(R.id.accountName);
        accountNumber = findViewById(R.id.accountNumber);
        openingDate = findViewById(R.id.openingDate);

        loadUserData();

        done = findViewById(R.id.done);

        done.setOnClickListener(v -> {
            startActivity(new Intent(AllSetSavings.this, MainActivity.class));
            finish();
        });

    }
    private void loadUserData() {
        userManager.getUserData(new UserManager.UserDataCallback() {
            @Override
            public void onDataLoaded(Map<String, Object> userData) {
                if (userData != null && userData.containsKey("accounts") && userData.containsKey("fullName")) {
                    String fullName = userData.get("fullName").toString();
                    Map<String, Boolean> accounts = (Map<String, Boolean>) userData.get("accounts");
                    for (String accountId : accounts.keySet()) {
                        userManager.getAccountData(accountId, new UserManager.AccountDataCallback() {
                            @Override
                            public void onAccountLoaded(DocumentSnapshot accountData) {
                                if (accountData.exists() && "savings".equals(accountData.getString("accountType"))) {
                                    String accountNo = accountData.getString("accountNumber");
                                    Long openingDateLong = accountData.getLong("openingDate");
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));

                                    if (accountNo != null && openingDateLong != null) {
                                        accountName.setText(fullName);
                                        accountNumber.setText(accountNo);
                                        openingDate.setText(sdf.format(new Date(openingDateLong)));
                                    } else {
                                        accountName.setText(null);
                                        accountNumber.setText(null);
                                        openingDate.setText(null);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                accountNumber.setText("Error");
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                accountNumber.setText("Error");
            }
        });
    }
}