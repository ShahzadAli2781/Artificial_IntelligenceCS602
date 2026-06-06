package com.example.fintechaiassistantapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.fintechaiassistantapp.R;
import com.example.fintechaiassistantapp.utils.SessionManager;
import java.util.concurrent.Executor;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        if (sessionManager.isDarkMode()) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        }

        setContentView(R.layout.activity_splash);

        // Animate UI elements
        ImageView logo = findViewById(R.id.logo);
        TextView appName = findViewById(R.id.tv_app_name);
        TextView tagline = findViewById(R.id.tv_tagline);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        logo.startAnimation(fadeIn);
        appName.startAnimation(slideUp);
        tagline.startAnimation(slideUp);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (sessionManager.isLoggedIn()) {
                checkSecurityAndNavigate();
            } else {
                navigateToNextScreen(false);
            }
        }, SPLASH_DELAY);
    }

    private void checkSecurityAndNavigate() {
        SessionManager sessionManager = new SessionManager(this);
        androidx.biometric.BiometricManager biometricManager = androidx.biometric.BiometricManager.from(this);
        
        int canAuthenticate = biometricManager.canAuthenticate(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG | 
                androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL);

        // Sirf tab biometric maangein agar feature enabled ho AUR hardware support kare
        if (sessionManager.isBiometricEnabled() && canAuthenticate == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
            showBiometricPrompt();
        } else {
            navigateToNextScreen(true);
        }
    }

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(SplashActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // Agar user cancel kare to app band kar dein, warna lockout ho sakta hai
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    finishAffinity();
                } else {
                    navigateToNextScreen(true); // Fallback to avoid complete lockout
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                navigateToNextScreen(true);
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Security Check")
                .setSubtitle("Confirm identity to continue")
                .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG | androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void navigateToNextScreen(boolean isLoggedIn) {
        Intent intent = new Intent(this, isLoggedIn ? DashboardActivity.class : LoginActivity.class);
        startActivity(intent);
        finish();
    }
}