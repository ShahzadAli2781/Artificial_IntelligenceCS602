package com.example.fintechaiassistantapp.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fintechaiassistantapp.R;
import com.google.android.material.appbar.MaterialToolbar;

public class SecurityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        findViewById(R.id.rl_change_password).setOnClickListener(v -> 
            Toast.makeText(this, "Change Password feature coming soon!", Toast.LENGTH_SHORT).show());
            
        findViewById(R.id.rl_privacy_policy).setOnClickListener(v -> 
            Toast.makeText(this, "Privacy Policy coming soon!", Toast.LENGTH_SHORT).show());
            
        findViewById(R.id.rl_data_export).setOnClickListener(v -> 
            Toast.makeText(this, "Data Export coming soon!", Toast.LENGTH_SHORT).show());
    }
}