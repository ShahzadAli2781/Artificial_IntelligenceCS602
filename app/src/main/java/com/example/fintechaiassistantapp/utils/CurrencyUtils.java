package com.example.fintechaiassistantapp.utils;

import java.util.Locale;

public class CurrencyUtils {
    public static String formatPKR(double amount) {
        return String.format(Locale.getDefault(), "PKR %.2f", amount);
    }
}
