package com.example.fintechaiassistantapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.fintechaiassistantapp.R;
import com.example.fintechaiassistantapp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

public class ProfileActivity extends AppCompatActivity {
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize Views
        TextView tvName = findViewById(R.id.tv_profile_name);
        TextView tvEmail = findViewById(R.id.tv_profile_email);
        MaterialSwitch switchBiometric = findViewById(R.id.switch_biometric);
        MaterialSwitch switchDarkMode = findViewById(R.id.switch_dark_mode);
        RelativeLayout rlLogout = findViewById(R.id.rl_logout);

        // Set Data
        tvName.setText(sessionManager.getUsername());
        tvEmail.setText(sessionManager.getUserEmail());
        switchBiometric.setChecked(sessionManager.isBiometricEnabled());
        switchDarkMode.setChecked(sessionManager.isDarkMode());

        // Listeners
        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setBiometricEnabled(isChecked);
            String status = isChecked ? "enabled" : "disabled";
            Toast.makeText(this, "Biometric Security " + status, Toast.LENGTH_SHORT).show();
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setDarkMode(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        rlLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}