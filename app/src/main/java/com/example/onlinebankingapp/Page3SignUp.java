package com.example.onlinebankingapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Page3SignUp extends Fragment {
    private Button saveButton;
    Long timeoutSeconds = 60L;
    private String countryCode, phoneNumber, password, firstName, lastName, email;
    ProgressBar progressBar;
    PhoneAuthProvider.ForceResendingToken resendingToken;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.signup_page3, container, false);

        if (getArguments() != null) {
            countryCode = getArguments().getString("countryCode");
            phoneNumber = getArguments().getString("phoneNumber");
            password = getArguments().getString("password");
            firstName = getArguments().getString("firstName");
            lastName = getArguments().getString("lastName");
            email = getArguments().getString("email");
        }
        String fullName = firstName + " " + lastName;

        saveButton = rootView.findViewById(R.id.saveButton);
        progressBar = rootView.findViewById(R.id.progressBar);

        saveButton.setOnClickListener(v -> sendOTP(phoneNumber, false));

        db = FirebaseFirestore.getInstance();

        return rootView;
    }

    private void sendOTP(String phoneNumber, boolean isResend) {
        setInProgress(true);
        String completePhoneNumber = countryCode + phoneNumber;
        String fullName = firstName + " " + lastName;

        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(completePhoneNumber)
                .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signInWithPhoneAuthCredential(phoneAuthCredential, fullName, email);
                        setInProgress(false);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Utility.showToast(getActivity(), "Verification failed: " + e.getMessage());
                        setInProgress(false);
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verificationId, forceResendingToken);
                        navigateToVerificationPage(verificationId);
                        resendingToken = forceResendingToken;
                        Utility.showToast(getActivity(), "OTP sent");
                        setInProgress(false);
                    }
                });
        if (isResend){
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        } else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, String fullName, String email) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Utility.showToast(getActivity(), "Verification successful!");
                        getParentFragmentManager().popBackStack();
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Utility.showToast(getActivity(), "Invalid OTP entered");
                        } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            Utility.showToast(getActivity(), "Invalid user");
                        } else {
                            Utility.showToast(getActivity(), "Verification failed: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void saveUserData(String userUID, String fullName, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("email", email);
        user.put("countryCode", countryCode);
        user.put("phoneNumber", phoneNumber);
        user.put("password", password);
        user.put("accounts", new HashMap<String, Boolean>());

        db.collection("users")
                .document(userUID)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Utility.showToast(getActivity(), "User information saved successfully!");
                    createDefaultAccounts(userUID);
                })
                .addOnFailureListener(e -> {
                    Utility.showToast(getActivity(), "Failed to save user information: " + e.getMessage());
                });
    }

    private void createDefaultAccounts(String userUID) {
        CollectionReference accountsCollection = db.collection("accounts");

        Map<String, Object> walletAccount = new HashMap<>();
        walletAccount.put("userId", userUID);
        walletAccount.put("accountType", "wallet");
        walletAccount.put("balance", 0.00);
        walletAccount.put("transactions", new HashMap<String, Boolean>());

        Map<String, Object> savingsAccount = new HashMap<>();
        savingsAccount.put("userId", userUID);
        savingsAccount.put("accountType", "savings");
        savingsAccount.put("balance", 0.00);
        savingsAccount.put("transactions", new HashMap<String, Boolean>());

        accountsCollection.add(walletAccount)
                .addOnSuccessListener(walletAccountDoc -> {
                    String walletAccountId = walletAccountDoc.getId();
                    updateUserAccountReference(userUID, walletAccountId);
                    Utility.showToast(getActivity(), "Wallet account created successfully!");
                })
                .addOnFailureListener(e -> {
                    Utility.showToast(getActivity(), "Failed to create wallet account: " + e.getMessage());
                });

        accountsCollection.add(savingsAccount)
                .addOnSuccessListener(savingsAccountDoc -> {
                    String savingsAccountId = savingsAccountDoc.getId();
                    updateUserAccountReference(userUID, savingsAccountId);
                    Utility.showToast(getActivity(), "Savings account created successfully!");
                })
                .addOnFailureListener(e -> {
                    Utility.showToast(getActivity(), "Failed to create savings account: " + e.getMessage());
                });
    }

    private void updateUserAccountReference(String userUID, String accountId) {
        DocumentReference userRef = db.collection("users").document(userUID);

        userRef.update("accounts." + accountId, true)
                .addOnSuccessListener(aVoid -> {
                    Utility.showToast(getActivity(), "User account reference updated successfully!");
                })
                .addOnFailureListener(e -> {
                    Utility.showToast(getActivity(), "Failed to update user account reference: " + e.getMessage());
                });
    }



    private void navigateToVerificationPage(String verificationId) {
        Bundle bundle = new Bundle();
        bundle.putString("reason", "Sign up");
        bundle.putString("verificationId", verificationId);
        bundle.putString("fullName", firstName + " " + lastName);
        bundle.putString("email", email);
        bundle.putString("countryCode", countryCode);
        bundle.putString("phoneNumber", phoneNumber);
        bundle.putString("password", password);

        PagePhoneVerification fragment = new PagePhoneVerification();
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    void setInProgress(boolean inProgress){
        if (inProgress){
            progressBar.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            saveButton.setVisibility(View.VISIBLE);
        }
    }
}
