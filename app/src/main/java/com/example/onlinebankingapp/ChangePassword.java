package com.example.onlinebankingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.mindrot.jbcrypt.BCrypt;

public class ChangePassword extends AppCompatActivity {
    EditText currentPassword, newPassword, confirmNewPassword;
    Button saveButton;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentPassword = findViewById(R.id.currentPassword);
        newPassword = findViewById(R.id.newPassword);
        confirmNewPassword = findViewById(R.id.confirmNewPassword);
        saveButton = findViewById(R.id.saveButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        saveButton.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String currentPass = currentPassword.getText().toString();
        String newPass = newPassword.getText().toString();
        String confirmNewPass = confirmNewPassword.getText().toString();

        if (newPass.isEmpty() || confirmNewPass.isEmpty() || currentPass.isEmpty()) {
            Toast.makeText(ChangePassword.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmNewPass)) {
            Toast.makeText(ChangePassword.this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String storedHash = document.getString("password");
                    if (BCrypt.checkpw(currentPass, storedHash)) {
                        String newHash = BCrypt.hashpw(newPass, BCrypt.gensalt());
                        db.collection("users").document(userId).update("password", newHash).addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                currentPassword.setText("");
                                newPassword.setText("");
                                confirmNewPassword.setText("");
                                Toast.makeText(ChangePassword.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(ChangePassword.this, "Error updating password", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(ChangePassword.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChangePassword.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ChangePassword.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
