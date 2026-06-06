package com.example.fintechaiassistantapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.fintechaiassistantapp.R;
import com.example.fintechaiassistantapp.activities.LoginActivity;
import com.example.fintechaiassistantapp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import java.util.concurrent.Executor;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AppCompatDelegate;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.material.imageview.ShapeableImageView;

public class ProfileFragment extends Fragment {
    private SessionManager sessionManager;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ShapeableImageView ivProfileImage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && ivProfileImage != null) {
                        try {
                            requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) {}
                        ivProfileImage.setImageURI(uri);
                        sessionManager.setProfileImage(uri.toString());
                        Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sessionManager = new SessionManager(requireContext());
        return inflater.inflate(R.layout.activity_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(null);

        TextView tvName = view.findViewById(R.id.tv_profile_name);
        TextView tvEmail = view.findViewById(R.id.tv_profile_email);
        ivProfileImage = view.findViewById(R.id.iv_profile_image);
        MaterialSwitch switchBiometric = view.findViewById(R.id.switch_biometric);
        MaterialSwitch switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        MaterialSwitch switchNotifications = view.findViewById(R.id.switch_notifications);
        View fabEdit = view.findViewById(R.id.fab_edit_profile);

        String userEmail = sessionManager.getUserEmail();
        tvEmail.setText(userEmail);
        tvName.setText(sessionManager.getUsername());

        String savedImageUri = sessionManager.getProfileImage();
        if (savedImageUri != null) {
            ivProfileImage.setImageURI(Uri.parse(savedImageUri));
        }

        ivProfileImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        switchBiometric.setChecked(sessionManager.isBiometricEnabled());
        switchDarkMode.setChecked(sessionManager.isDarkMode());
        
        fabEdit.setOnClickListener(v -> showEditProfileDialog(tvName));

        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkBiometricAvailability(switchBiometric);
            } else {
                sessionManager.setBiometricEnabled(false);
            }
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setDarkMode(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        RelativeLayout rlLinkedAccounts = view.findViewById(R.id.rl_linked_accounts);
        RelativeLayout rlSecurity = view.findViewById(R.id.rl_security);
        RelativeLayout rlMonthlyBudget = view.findViewById(R.id.rl_monthly_budget);
        TextView tvCurrentBudget = view.findViewById(R.id.tv_current_budget);
        RelativeLayout rlLogout = view.findViewById(R.id.rl_logout);

        tvCurrentBudget.setText(com.example.fintechaiassistantapp.utils.CurrencyUtils.formatPKR(sessionManager.getMonthlyBudget()));

        rlLinkedAccounts.setOnClickListener(v ->
                Toast.makeText(getContext(), "Linked Accounts coming soon!", Toast.LENGTH_SHORT).show());

        rlSecurity.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.fintechaiassistantapp.activities.SecurityActivity.class);
            startActivity(intent);
        });

        rlMonthlyBudget.setOnClickListener(v -> showEditBudgetDialog(tvCurrentBudget));

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String status = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Notifications " + status, Toast.LENGTH_SHORT).show();
        });

        rlLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }

    private void showEditProfileDialog(TextView tvName) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);
        TextInputEditText etName = dialogView.findViewById(R.id.et_edit_name);
        etName.setText(sessionManager.getUsername());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Edit Profile")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        sessionManager.setUsername(newName);
                        tvName.setText(newName);
                        Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditBudgetDialog(TextView tvBudget) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);
        com.google.android.material.textfield.TextInputLayout layout = dialogView.findViewById(R.id.et_edit_name).getRootView().findViewById(R.id.et_edit_name).getParent().getParent() instanceof com.google.android.material.textfield.TextInputLayout ? (com.google.android.material.textfield.TextInputLayout) dialogView.findViewById(R.id.et_edit_name).getParent().getParent() : null;
        
        TextInputEditText etBudget = dialogView.findViewById(R.id.et_edit_name);
        if (layout != null) layout.setHint("Monthly Budget Amount");
        etBudget.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etBudget.setText(String.valueOf(sessionManager.getMonthlyBudget()));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Set Monthly Budget")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String val = etBudget.getText().toString().trim();
                    if (!val.isEmpty()) {
                        float budget = Float.parseFloat(val);
                        sessionManager.setMonthlyBudget(budget);
                        tvBudget.setText(com.example.fintechaiassistantapp.utils.CurrencyUtils.formatPKR(budget));
                        Toast.makeText(getContext(), "Budget updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkBiometricAvailability(MaterialSwitch sw) {
        BiometricManager biometricManager = BiometricManager.from(requireContext());
        int authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL;
        
        switch (biometricManager.canAuthenticate(authenticators)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                sessionManager.setBiometricEnabled(true);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(getContext(), "No biometric hardware found", Toast.LENGTH_SHORT).show();
                sw.setChecked(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(getContext(), "Biometric hardware unavailable", Toast.LENGTH_SHORT).show();
                sw.setChecked(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(getContext(), "No biometrics enrolled. Please check settings.", Toast.LENGTH_SHORT).show();
                sw.setChecked(false);
                break;
        }
    }
}