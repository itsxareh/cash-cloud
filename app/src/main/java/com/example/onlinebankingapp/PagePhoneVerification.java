package com.example.onlinebankingapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.mindrot.jbcrypt.BCrypt;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class PagePhoneVerification extends Fragment {
    private EditText otpCode;
    private TextView resendText, phoneNumberText, countryCodeText;
    private Button verifyButton;
    Long timeoutSeconds = 60L;
    ProgressBar progressBar;
    private String uid, email, fullName, password, phoneNumber, countryCode, verificationId;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseFirestore db;
    private String reason = "";
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    private boolean isPhoneLogin = false;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        if (getArguments() != null) {
            setReason(getArguments().getString("reason"));
            uid = getArguments().getString("uid");
            fullName = getArguments().getString("fullName");
            email = getArguments().getString("email");
            password = getArguments().getString("password");
            phoneNumber = getArguments().getString("phoneNumber");
            countryCode = getArguments().getString("countryCode");
            verificationId = getArguments().getString("verificationId");
            resendingToken = getArguments().getParcelable("resendingToken");
        }
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                signInWithPhoneAuthCredential(credential);
                setInProgress(false);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(getActivity(), "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                setInProgress(false);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                PagePhoneVerification.this.verificationId = verificationId;
                PagePhoneVerification.this.resendingToken = forceResendingToken;
                setInProgress(false);
            }
        };
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_phone_verification_page, container, false);

        otpCode = rootView.findViewById(R.id.otpCode);
        verifyButton = rootView.findViewById(R.id.verifyButton);
        resendText = rootView.findViewById(R.id.resendButton);
        phoneNumberText = rootView.findViewById(R.id.phoneNumber);
        countryCodeText = rootView.findViewById(R.id.areaCode);
        progressBar = rootView.findViewById(R.id.progressBar);

        phoneNumberText.setText(phoneNumber);
        countryCodeText.setText(countryCode);
        verifyButton.setOnClickListener(v -> verifyCode());
        resendText.setOnClickListener(v -> resendVerificationCode());

        db = FirebaseFirestore.getInstance();
        return rootView;
    }

    private void verifyCode() {
        String code = otpCode.getText().toString().trim();
        if (code.length() != 6) {
            otpCode.setError("Enter valid code...");
            otpCode.requestFocus();
            return;
        }

        verifyVerificationCode(code);
    }

    private void verifyVerificationCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
        setInProgress(true);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        setInProgress(true);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    setInProgress(false);
                    if (task.isSuccessful()) {
                        String userUID = task.getResult().getUser().getUid();
                        handlePostVerify(userUID);
                        otpCode.setText("");
                    } else {
                        String message = "Something went wrong, please try again later.";
                        if (task.getException() != null) {
                            message = task.getException().getMessage();
                        }
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void handlePostVerify(String userUID) {
        if (reason.equals("Change Phone Number")) {
            navigateToChangeMobileNumber();
        } else if (reason.equals("New Number")) {
            updatePhoneNumber(uid);
        } else if (reason.equals("Start Savings")) {
            startSavingsAccount(mAuth.getCurrentUser().getUid());
        } else {
            checkAndSaveUserData(userUID);
        }
    }
//    private void linkPhoneNumberToEmailAccount(PhoneAuthCredential credential, String userUID) {
//        mAuth.getCurrentUser().linkWithCredential(credential)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        checkAndSaveUserData(userUID);
//                    } else {
//                        String errorMessage = "Failed to link phone number.";
//                        if (task.getException() != null) {
//                            errorMessage = task.getException().getMessage();
//                        }
//                        if (errorMessage.contains("already been linked")) {
//                            checkAndSaveUserData(mAuth.getCurrentUser().getUid());
//                        } else {
//                            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//    }


    private void checkAndSaveUserData(String userUID) {
        DocumentReference userRef = db.collection("users").document(userUID);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                navigateToMainActivity();
            } else {
                saveUserData(userUID);
            }
        });
    }

    private void saveUserData(String userUID) {
        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("email", email);
        user.put("countryCode", countryCode);
        user.put("phoneNumber", phoneNumber);
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        user.put("password", hashedPassword);
        user.put("accounts", new HashMap<String, Boolean>());

        db.collection("users")
                .document(userUID)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    createWalletAccount(userUID);
                })
                .addOnFailureListener(e -> {
                    setInProgress(false);
                    Utility.showToast(getActivity(), "Failed to save user information: " + e.getMessage());
                });
    }

    private void createWalletAccount(String userUID) {
        CollectionReference accountsCollection = db.collection("accounts");

        Map<String, Object> walletAccount = new HashMap<>();
        walletAccount.put("userId", userUID);
        walletAccount.put("accountType", "wallet");
        walletAccount.put("balance", 0.00);
        walletAccount.put("transactions", new HashMap<String, Boolean>());

        Task<DocumentReference> walletTask = accountsCollection.add(walletAccount);
        Tasks.whenAllSuccess(walletTask)
                .addOnSuccessListener(result -> {
                    String walletAccountId = walletTask.getResult().getId();
                    updateUserAccountReferences(userUID, walletAccountId);
                })
                .addOnFailureListener(e -> {
                    Utility.showToast(getActivity(), "Failed to create accounts: " + e.getMessage());
                });
    }

    private void updateUserAccountReferences(String userUID, String accountId) {
        DocumentReference userRef = db.collection("users").document(userUID);

        Map<String, Object> accountsUpdate = new HashMap<>();
        accountsUpdate.put("accounts." + accountId, true);

        userRef.update(accountsUpdate)
                .addOnSuccessListener(aVoid -> {
                    if (reason.equals("Start Savings")){
                        Intent intent = new Intent(getActivity(), AllSetSavings.class);
                        startActivity(intent);
                        getActivity().finish();

                    } else{
                        createOnlineCard(userUID);
                    }
                })
                .addOnFailureListener(e -> {
                    Utility.showToast(getActivity(), "Failed to update user account references: " + e.getMessage());
                });
    }
    private void createOnlineCard(String userUID){
        CollectionReference cardCollection = db.collection("cards");

        Map<String, Object> cardAccount = new HashMap<>();
        cardAccount.put("userId", userUID);
        cardAccount.put("cardType", "online card");
        cardAccount.put("cardNumber", generateRandomCardNumber());
        cardAccount.put("expirationDate", generateExpirationDate());
        cardAccount.put("cvv", generateRandomCVV());
        cardAccount.put("limit", 50000);
        cardAccount.put("balance", 0.00);

        Task<DocumentReference> cardTask = cardCollection.add(cardAccount);
        Tasks.whenAll(cardTask)
                .addOnSuccessListener(result -> {
                    String cardAccountId = cardTask.getResult().getId();
                    updateUserCardReferences(userUID, cardAccountId);
                })
                .addOnFailureListener(e -> {
                    Utility.showToast(getActivity(), "Failed to create card: " + e.getMessage());
                });
    }
    private void startSavingsAccount(String userUID){
        CollectionReference accountsCollection = db.collection("accounts");

        Map<String, Object> savingsAccount = new HashMap<>();
        savingsAccount.put("userId", userUID);
        savingsAccount.put("accountType", "savings");
        savingsAccount.put("accountNumber", generateRandomSavingsNumber());
        savingsAccount.put("balance", 0.00);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
        long currentTimeMillis = calendar.getTimeInMillis();
        savingsAccount.put("openingDate", currentTimeMillis);
        savingsAccount.put("transactions", new HashMap<String, Boolean>());

        Task<DocumentReference> savingsTask = accountsCollection.add(savingsAccount);
        Tasks.whenAllSuccess(savingsTask)
                .addOnSuccessListener(result -> {
                    String savingsAccountId = savingsTask.getResult().getId();
                    updateUserAccountReferences(userUID, savingsAccountId);
                })
                .addOnFailureListener(e -> {
                    Utility.showToast(getActivity(), "Failed to create accounts: " + e.getMessage());
                });

    }
    private void updatePhoneNumber(String uid) {
        String formattedPhoneNumber = countryCode + phoneNumber;
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d("before uid", uid);
            Log.d("after uid", currentUser.getUid());
            Log.d("debug", phoneNumber);
            Log.d("debug", countryCode);
            if (verificationId != null && !otpCode.getText().toString().isEmpty()) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otpCode.getText().toString());

                db.collection("users").document(uid)
                        .update("countryCode", countryCode, "phoneNumber", phoneNumber)
                        .addOnSuccessListener(aVoid -> {
                            currentUser.updatePhoneNumber(credential)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            setInProgress(false);
                                            Toast.makeText(getActivity(), "Phone Number Changed", Toast.LENGTH_SHORT).show();
                                            transferUserData(uid, currentUser.getUid());
                                        } else {
                                            setInProgress(false);
                                            Toast.makeText(getActivity(), "Phone number update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .addOnFailureListener(e -> {
                            setInProgress(false);
                            Toast.makeText(getActivity(), "Error updating phone number: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                setInProgress(false);
                Toast.makeText(getActivity(), "Verification ID or OTP code is missing", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void transferUserData(String oldUid, String newUid){
        DocumentReference oldUserRef = db.collection("users").document(oldUid);
        DocumentReference newUserRef = db.collection("users").document(newUid);

        oldUserRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Map<String, Object> oldUserData = task.getResult().getData();
                if (oldUserData != null) {
                    newUserRef.set(oldUserData).addOnCompleteListener(setTask -> {
                        if (setTask.isSuccessful()) {
                            transferRelatedData(oldUid, newUid);
                        } else {
                            String errorMessage = setTask.getException().getMessage();
                            Toast.makeText(getActivity(), "Failed to set new user data: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                String errorMessage = task.getException().getMessage();
                Toast.makeText(getActivity(), "Failed to get old user data: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void transferRelatedData(String oldUserId, String newUserId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("accounts")
                .whereEqualTo("userId", oldUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Map<String, Object> accountData = document.getData();
                            accountData.put("userId", newUserId);
                            db.collection("accounts").document(document.getId()).set(accountData);
                        }
                    }
                });

        db.collection("cards")
                .whereEqualTo("userId", oldUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Map<String, Object> cardData = document.getData();
                            cardData.put("userId", newUserId);
                            db.collection("cards").document(document.getId()).set(cardData);
                        }
                    }
                });
        db.collection("users").document(oldUserId).delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                navigateToMainActivity();
            } else {
                String errorMessage = task.getException().getMessage();
                Toast.makeText(getActivity(), "Failed to delete old user data: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

    }
    private String generateExpirationDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 2);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yy");
        return dateFormat.format(calendar.getTime());
    }
    private String generateRandomSavingsNumber() {
        Random random = new Random();
        StringBuilder savings = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            int digit = random.nextInt(10);
            savings.append(digit);
        }
        return savings.toString();
    }
    private String generateRandomCardNumber() {
        Random random = new Random();
        StringBuilder cardNumber = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int digit = random.nextInt(10);
            cardNumber.append(digit);
        }
        return cardNumber.toString();
    }
    private String generateRandomCVV() {
        Random random = new Random();
        StringBuilder cvv = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            int digit = random.nextInt(10);
            cvv.append(digit);
        }
        return cvv.toString();
    }
    private void updateUserCardReferences(String userUID, String cardAccountId) {
        DocumentReference userRef = db.collection("users").document(userUID);

        Map<String, Object> accountsUpdate = new HashMap<>();
        accountsUpdate.put("cards." + cardAccountId, true);

        userRef.update(accountsUpdate)
                .addOnSuccessListener(aVoid -> {
                    navigateToMainActivity();
                })
                .addOnFailureListener(e -> {
                    Utility.showToast(getActivity(), "Failed to update user card references: " + e.getMessage());
                });
    }
    private void navigateToMainActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
    private void navigateToChangeMobileNumber() {
        Intent intent = new Intent(getActivity(), NewPhoneNumber.class);
        intent.putExtra("reason", "New Number");
        startActivity(intent);
    }

    private void resendVerificationCode() {
        startResendTimer();
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(getActivity())
                .setCallbacks(mCallbacks)
                .setForceResendingToken(resendingToken)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    void startResendTimer() {
        resendText.setEnabled(false);
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                resendText.setText("Resend OTP in " + secondsRemaining + " seconds");
            }

            public void onFinish() {
                resendText.setEnabled(true);
            }
        }.start();
    }

    void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            verifyButton.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            verifyButton.setVisibility(View.VISIBLE);
        }
    }
}
