package com.example.onlinebankingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {
    Button showNameSetting, recoverSetting, changePhoneNumberSetting, closeAccountSetting,
    changePasswordSetting, privacyPolicySetting, termsAndConditionsSetting, licenseSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        showNameSetting = findViewById(R.id.showNameSetting);
        recoverSetting = findViewById(R.id.recoverSetting);
        changePasswordSetting = findViewById(R.id.changePasswordSetting);
        closeAccountSetting = findViewById(R.id.closeAccountSetting);
        changePhoneNumberSetting = findViewById(R.id.changePhoneNumberSetting);
        privacyPolicySetting = findViewById(R.id.privacyPolicySetting);
        termsAndConditionsSetting = findViewById(R.id.termsAndConditionsSetting);
        licenseSetting = findViewById(R.id.licenseSetting);

        showNameSetting.setOnClickListener(v -> {startActivity(new Intent(SettingsActivity.this, ShowName.class));});
        recoverSetting.setOnClickListener(v -> { startActivity(new Intent(SettingsActivity.this, AccountRecovery.class));});
        changePasswordSetting.setOnClickListener(v -> { startActivity(new Intent(SettingsActivity.this, ChangePassword.class));});
        closeAccountSetting.setOnClickListener(v -> {});
        changePhoneNumberSetting.setOnClickListener(v -> { startActivity(new Intent(SettingsActivity.this, ChangePhoneNumber.class));});
        privacyPolicySetting.setOnClickListener(v -> {});
        termsAndConditionsSetting.setOnClickListener(v -> {});
        licenseSetting.setOnClickListener(v -> {});
    }
}