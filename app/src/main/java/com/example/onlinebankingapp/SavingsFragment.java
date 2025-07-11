package com.example.onlinebankingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SavingsFragment extends Fragment {
    private static final String TAG = "SavingsFragment";

    Button startSavingButton;
    RelativeLayout mySavingsLayout;
    ImageButton showBalanceButton;
    TextView balanceAmount, balanceText, accountNumber, savingsBalance, myAccountText;
    UserManager userManager;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    private boolean isBalanceVisible = true;
    private String currentBalance = "";
    private String saccountNumber = "";

    public SavingsFragment () {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.savings_fragment, container, false);

        mySavingsLayout = rootView.findViewById(R.id.mySavingLayout);
        accountNumber = rootView.findViewById(R.id.accountNumber);
        savingsBalance = rootView.findViewById(R.id.savingsBalance);
        myAccountText = rootView.findViewById(R.id.myaccountText);
        showBalanceButton = rootView.findViewById(R.id.showBalanceButton);
        startSavingButton = rootView.findViewById(R.id.startSavingButton);
        balanceAmount = rootView.findViewById(R.id.balanceAmount);
        balanceText = rootView.findViewById(R.id.savingsText);
        userManager = UserManager.getInstance();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        showBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBalanceVisible) {
                    balanceAmount.setText("••••••••");
                    accountNumber.setText("•••• •••• ••••");
                    savingsBalance.setText("••••••••");
                    showBalanceButton.setImageResource(R.drawable.baseline_visibility_off_24);
                } else {
                    accountNumber.setText(saccountNumber);
                    savingsBalance.setText(currentBalance);
                    balanceAmount.setText(currentBalance);
                    showBalanceButton.setImageResource(R.drawable.baseline_remove_red_eye_24);
                }
                isBalanceVisible = !isBalanceVisible;
            }
        });

        startSavingButton.setOnClickListener(v -> {
            final DialogPlus dialogPlus = DialogPlus.newDialog(getContext())
                    .setContentHolder(new ViewHolder(R.layout.dialogstartsavings))
                    .setExpanded(true, 1000)
                    .create();
            View myview = dialogPlus.getHolderView();
            final Button startSavings = myview.findViewById(R.id.startSavingButton);
            dialogPlus.show();

            startSavings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userId = mAuth.getCurrentUser().getUid();
                    db.collection("users").document(userId).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String countryCode = document.getString("countryCode");
                                String phoneNumber = document.getString("phoneNumber");
                                sendOtpToPhoneNumber(phoneNumber, countryCode);
                                dialogPlus.dismiss();
                            }
                        }
                    });
                }
            });
        });

        mySavingsLayout.setOnClickListener(v -> startActivity(new Intent(getActivity(), MySavingsAccount.class)));

        loadUserData();
        return rootView;
    }

    private void sendOtpToPhoneNumber(String phoneNumber, String countryCode) {
        String fullPhoneNumber = countryCode + phoneNumber;
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(fullPhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(getActivity())
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        PagePhoneVerification fragment = new PagePhoneVerification();
                        Bundle bundle = new Bundle();
                        bundle.putString("reason", "Start Savings");
                        bundle.putString("countryCode", countryCode);
                        bundle.putString("phoneNumber", phoneNumber);
                        bundle.putString("verificationId", verificationId);
                        bundle.putParcelable("resendingToken", forceResendingToken);

                        fragment.setArguments(bundle);
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void loadUserData() {
        Log.d(TAG, "Loading user data...");
        userManager.getUserData(new UserManager.UserDataCallback() {
            @Override
            public void onDataLoaded(Map<String, Object> userData) {
                Log.d(TAG, "User data loaded: " + userData);
                if (userData != null && userData.containsKey("accounts")) {
                    Map<String, Boolean> accounts = (Map<String, Boolean>) userData.get("accounts");
                    for (String accountId : accounts.keySet()) {
                        userManager.getAccountData(accountId, new UserManager.AccountDataCallback() {
                            @Override
                            public void onAccountLoaded(DocumentSnapshot accountData) {
                                Log.d(TAG, "Account data loaded: " + accountData.getData());
                                if (accountData.exists() && "savings".equals(accountData.getString("accountType"))) {
                                    Double balance = accountData.getDouble("balance");
                                    String storedAccountNumber = accountData.getString("accountNumber");
                                    startSavingButton.setEnabled(false);
                                    myAccountText.setVisibility(View.VISIBLE);
                                    mySavingsLayout.setVisibility(View.VISIBLE);
                                    mySavingsLayout.setEnabled(true);
                                    startSavingButton.setVisibility(View.GONE);
                                    if (storedAccountNumber != null && !storedAccountNumber.isEmpty()) {
                                        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                                        numberFormat.setMinimumFractionDigits(2);
                                        numberFormat.setMaximumFractionDigits(2);
                                        currentBalance = numberFormat.format(balance);
                                        balanceAmount.setText(currentBalance);
                                        savingsBalance.setText(currentBalance);
                                        saccountNumber = formatAccountNumber(storedAccountNumber);
                                        accountNumber.setText(saccountNumber);
                                        Log.d(TAG, "Savings account found: " + accountData.getString("userId"));
                                        Log.d(TAG, "Current balance: " + currentBalance);
                                        Log.d(TAG, "Account number: " + saccountNumber);
                                    } else {
                                        currentBalance = "0.00";
                                        balanceAmount.setText(currentBalance);
                                        Log.d(TAG, "No valid savings account number found");
                                    }
                                } else {
                                    Log.d(TAG, "Account is not a savings account or does not exist");
                                    myAccountText.setVisibility(View.GONE);
                                    mySavingsLayout.setVisibility(View.GONE);
                                    mySavingsLayout.setEnabled(false);
                                    startSavingButton.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                balanceAmount.setText("Error");
                                Log.e(TAG, "Failed to load account data", e);
                            }
                        });
                    }
                } else {
                    Log.d(TAG, "No accounts found for user");
                }
            }

            @Override
            public void onFailure(Exception e) {
                balanceAmount.setText("Error");
                Log.e(TAG, "Failed to load user data", e);
            }
        });
    }

    private String formatAccountNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return "";
        }
        StringBuilder maskedCardNumber = new StringBuilder("•••• •••• ");
        maskedCardNumber.append(cardNumber.substring(8).replaceAll("(.{4})", "$1 ").trim());
        return maskedCardNumber.toString().trim();
    }
}
