package com.example.fintechaiassistantapp.voice;

import com.example.fintechaiassistantapp.models.VoiceExpenseModel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Professional Voice Expense Parser
 * Supports:
 * - "500 food"
 * - "spent 700 on shopping"
 * - "1000 for transport"
 * - "I spent 250 on health"
 * - "5 dollar food"
 * - "500 rupees for shopping"
 * 
 * Updated to avoid null returns, allowing the UI to pre-fill partial data.
 */
public class SpeechParser {

    // Improved amount regex
    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("(\\d+(?:\\.\\d+)?)");

    public static VoiceExpenseModel parse(String text) {

        // Safety check - return empty model instead of null for better UI stability
        if (text == null || text.trim().isEmpty()) {
            return new VoiceExpenseModel("Other", 0, "");
        }

        // Normalize text: lowercase, trim, and remove currency symbols/commas for easier parsing
        String normalized = text.toLowerCase()
                .replaceAll("[$,]", "")
                .replaceAll("\\s+", " ")
                .trim();

        // =========================
        // Amount Extraction
        // =========================
        double amount = 0;

        Matcher matcher = AMOUNT_PATTERN.matcher(normalized);

        if (matcher.find()) {
            try {
                amount = Double.parseDouble(matcher.group(1));
            } catch (Exception e) {
                amount = 0;
            }
        }

        // =========================
        // Category Detection
        // =========================
        String category = "Other";

        // FOOD
        if (containsAny(normalized,
                "food",
                "meal",
                "lunch",
                "dinner",
                "restaurant",
                "burger",
                "pizza",
                "khana",
                "eat",
                "eating")) {

            category = "Food";
        }

        // SHOPPING
        else if (containsAny(normalized,
                "shopping",
                "shop",
                "cloth",
                "clothes",
                "dress",
                "buy",
                "purchase")) {

            category = "Shopping";
        }

        // HEALTH
        else if (containsAny(normalized,
                "health",
                "medicine",
                "doctor",
                "hospital",
                "tablet",
                "medical")) {

            category = "Health";
        }

        // TRANSPORT
        else if (containsAny(normalized,
                "transport",
                "travel",
                "uber",
                "bike",
                "car",
                "bus",
                "fuel",
                "petrol",
                "diesel",
                "rickshaw")) {

            category = "Transport";
        }

        // =========================
        // Return Parsed Model
        // =========================
        // We always return a model now. Even if amount is 0,
        // it allows the UI to pre-fill category and raw text.
        return new VoiceExpenseModel(
                category,
                amount,
                text
        );
    }

    /**
     * Utility method for keyword matching
     */
    private static boolean containsAny(String text, String... keywords) {

        for (String keyword : keywords) {

            if (text.contains(keyword)) {
                return true;
            }
        }

        return false;
    }
}
