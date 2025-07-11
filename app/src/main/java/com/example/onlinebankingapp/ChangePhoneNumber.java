package com.example.onlinebankingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.Firebase;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import org.mindrot.jbcrypt.BCrypt;

import java.util.concurrent.TimeUnit;

public class ChangePhoneNumber extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    Button changePhone;
    TextView phoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.change_phone_number);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        phoneNumber = findViewById(R.id.currentPhone);
        loadUserData();


        changePhone = findViewById(R.id.changePhoneButton);

        changePhone.setOnClickListener(v ->{
            final DialogPlus dialogPlus = DialogPlus.newDialog(this)
                    .setContentHolder(new ViewHolder(R.layout.dialogpassword))
                    .setExpanded(true, 1000)
                    .create();
            View myview = dialogPlus.getHolderView();
            final EditText password = myview.findViewById(R.id.password);
            final Button cancel = myview.findViewById(R.id.cancel);
            final Button confirm = myview.findViewById(R.id.confirm);
            dialogPlus.show();

           cancel.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   dialogPlus.dismiss();
               }
           });
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String cpassword = password.getText().toString();
                    if (cpassword.isEmpty()){
                        Utility.showToast(ChangePhoneNumber.this ,"Please enter your password");
                    }
                    String userId = mAuth.getCurrentUser().getUid();
                    db.collection("users").document(userId).get().addOnCompleteListener(task ->{
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String storedHash = document.getString("password");
                                String countryCode = document.getString("countryCode");
                                String pnumber = document.getString("phoneNumber");
                                if (BCrypt.checkpw(cpassword, storedHash)){
                                    dialogPlus.dismiss();
                                    sendOtpToPhoneNumber(pnumber, countryCode);
                                } else {
                                    Toast.makeText(ChangePhoneNumber.this, "Password is incorrect", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ChangePhoneNumber.this, "User not found", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Toast.makeText(ChangePhoneNumber.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });
    }
    private void loadUserData(){
        db.collection("users").document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(task ->{
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String pnumber = document.getString("phoneNumber");
                    String countryCode = document.getString("countryCode");
                    phoneNumber.setText(countryCode+pnumber);
                }
            }
        });
    }
    private void sendOtpToPhoneNumber(String phoneNumber, String countryCode){
        String completePhoneNumber = countryCode + phoneNumber;
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(completePhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(ChangePhoneNumber.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        PagePhoneVerification fragment = new PagePhoneVerification();
                        Bundle bundle = new Bundle();
                        bundle.putString("reason", "Change Phone Number");
                        bundle.putString("countryCode", countryCode);
                        bundle.putString("phoneNumber", phoneNumber);
                        bundle.putString("verificationId", verificationId);
                        bundle.putParcelable("resendingToken", forceResendingToken);

                        fragment.setArguments(bundle);
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

}