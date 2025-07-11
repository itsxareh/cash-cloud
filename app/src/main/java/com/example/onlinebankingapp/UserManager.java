package com.example.onlinebankingapp;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class UserManager {
    private static final String TAG = "UserManager";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static UserManager instance;

    private UserManager() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public String getNewTransactionId() {
        return db.collection("transactions").document().getId();
    }

    public Task<Void> addTransaction(String transactionId, Map<String, Object> transaction) {
        return db.collection("transactions").document(transactionId).set(transaction);
    }

    public void getUserData(final UserDataCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                callback.onDataLoaded(document.getData());
                            } else {
                                Log.d(TAG, "No such document");
                                callback.onFailure(new Exception("No such document"));
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                            callback.onFailure(task.getException());
                        }
                    });
        } else {
            callback.onFailure(new Exception("User not signed in"));
        }
    }

    public void getAccountData(String accountId, final AccountDataCallback callback) {
        db.collection("accounts").document(accountId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            callback.onAccountLoaded(document);
                        } else {
                            Log.d(TAG, "No such document");
                            callback.onFailure(new Exception("No such document"));
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                        callback.onFailure(task.getException());
                    }
                });
    }
    public void getCardData(String cardId, final CardDataCallback callback) {
        db.collection("cards").document(cardId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            callback.onCardLoaded(document);
                        } else {
                            Log.d(TAG, "No such document");
                            callback.onFailure(new Exception("No such document"));
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void getTransaction(String transactionId, TransactionCallback callback) {
        db.collection("transactions").document(transactionId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        double amount = documentSnapshot.getDouble("amount");
                        Long dateTime = documentSnapshot.getLong("dateTime");
                        if (dateTime == null) {
                            dateTime = 0L;
                        }
                        String type = documentSnapshot.getString("type");
                        String description = documentSnapshot.getString("description");
                        String reference = documentSnapshot.getString("reference");

                        AppTransaction transaction = new AppTransaction(amount, dateTime, type, description, reference);
                        callback.onTransactionLoaded(transaction);
                    } else {
                        callback.onTransactionLoaded(null);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                });
    }
    public void getAccountsByType(String userId, String accountType, AccountsCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("accounts")
                .whereEqualTo("userId", userId)
                .whereEqualTo("accountType", accountType)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> accounts = task.getResult().getDocuments();
                        callback.onAccountsLoaded(accounts);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public interface AccountsCallback {
        void onAccountsLoaded(List<DocumentSnapshot> accounts);
        void onFailure(Exception e);
    }
    public interface TransactionCallback {
        void onTransactionLoaded(AppTransaction transaction);
        void onFailure(Exception e);
    }

    public interface UserDataCallback {
        void onDataLoaded(Map<String, Object> userData);
        void onFailure(Exception e);
    }

    public interface AccountDataCallback {
        void onAccountLoaded(DocumentSnapshot accountData);
        void onFailure(Exception e);
    }
    public interface CardDataCallback {
        void onCardLoaded(DocumentSnapshot cardData);
        void onFailure(Exception e);
    }

}
