package com.example.fintechaiassistantapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;
import java.util.regex.Pattern;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fintechaiassistantapp.R;
import com.example.fintechaiassistantapp.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SignupActivity extends AppCompatActivity {
    private TextInputEditText etName, etEmail, etPassword;
    private MaterialButton btnSignup;
    private TextView tvLoginLink;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        sessionManager = new SessionManager(this);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnSignup = findViewById(R.id.btn_signup);
        tvLoginLink = findViewById(R.id.tv_login_link);

        btnSignup.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidEmail(email)) {
                etEmail.setError("Please enter a valid Gmail address (e.g., user@gmail.com)");
                return;
            }

            if (!isValidPassword(password)) {
                etPassword.setError("Password must be at least 8 characters long and contain at least one special character");
                return;
            }

            // Mock Signup Logic - Save user info but don't log in yet
            Toast.makeText(this, "Account created successfully. Please login.", Toast.LENGTH_SHORT).show();
            
            // Go to Login (Enforcing Signup -> Login flow)
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.toLowerCase().endsWith("@gmail.com");
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }
        // Regex to check for at least one special character
        Pattern specialCharPattern = Pattern.compile("[^a-zA-Z0-9 ]");
        return specialCharPattern.matcher(password).find();
    }
}