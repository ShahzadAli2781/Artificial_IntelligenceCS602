package com.example.fintechaiassistantapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "FinIntelligenceSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_MONTHLY_INCOME = "monthlyIncome";
    private static final String KEY_MONTHLY_BUDGET = "monthlyBudget";
    private static final String KEY_BIOMETRIC_ENABLED = "biometricEnabled";
    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_PROFILE_IMAGE = "profileImage";

    public static final String KEY_USER_EMAIL = "userEmail";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn, String email) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    public void setMonthlyIncome(float income) {
        editor.putFloat(KEY_MONTHLY_INCOME, income);
        editor.apply();
    }

    public void setMonthlyBudget(float budget) {
        editor.putFloat(KEY_MONTHLY_BUDGET, budget);
        editor.apply();
    }

    public float getMonthlyBudget() {
        return pref.getFloat(KEY_MONTHLY_BUDGET, 0.0f);
    }

    public void setBiometricEnabled(boolean enabled) {
        editor.putBoolean(KEY_BIOMETRIC_ENABLED, enabled);
        editor.apply();
    }

    public boolean isBiometricEnabled() {
        return pref.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }

    public void setDarkMode(boolean enabled) {
        editor.putBoolean(KEY_DARK_MODE, enabled);
        editor.apply();
    }

    public boolean isDarkMode() {
        return pref.getBoolean(KEY_DARK_MODE, false);
    }

    public void setUsername(String username) {
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public void setProfileImage(String uri) {
        editor.putString(KEY_PROFILE_IMAGE, uri);
        editor.apply();
    }

    public String getProfileImage() {
        return pref.getString(KEY_PROFILE_IMAGE, null);
    }

    public String getUsername() {
        String email = getUserEmail();
        String defaultName = "";
        if (email.contains("@")) {
            defaultName = email.split("@")[0];
            defaultName = defaultName.substring(0, 1).toUpperCase() + defaultName.substring(1);
        }
        return pref.getString(KEY_USERNAME, defaultName);
    }

    public float getMonthlyIncome() {
        return pref.getFloat(KEY_MONTHLY_INCOME, 0.0f);
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}