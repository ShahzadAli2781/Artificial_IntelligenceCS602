package com.example.fintechaiassistantapp.ocr;

import com.example.fintechaiassistantapp.models.OCRExpenseModel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

public class BillParser {

    private static final String[] FOOD_KEYWORDS = {"restaurant", "cafe", "burger", "pizza", "food", "eat", "dining", "bakery", "coffee"};
    private static final String[] SHOPPING_KEYWORDS = {"mart", "store", "shop", "amazon", "walmart", "target", "mall", "clothing", "electronics"};
    private static final String[] TRANSPORT_KEYWORDS = {"uber", "lyft", "taxi", "fuel", "gas", "shell", "subway", "metro", "parking", "train"};
    private static final String[] HEALTH_KEYWORDS = {"pharmacy", "doctor", "hospital", "medical", "health", "gym", "dental", "clinic"};

    public static OCRExpenseModel parse(String text) {
        if (text == null || text.isEmpty()) return null;

        String lowerText = text.toLowerCase();
        double amount = extractMaxAmount(text);
        String category = detectCategory(lowerText);
        String title = extractTitle(text);

        return new OCRExpenseModel(title, amount, category);
    }

    private static double extractMaxAmount(String text) {
        // Regex to find numbers that look like prices (e.g., 10.99, 1,000.00)
        Pattern pattern = Pattern.compile("(\\d{1,3}(,\\d{3})*(\\.\\d{2}))|(\\d+(\\.\\d{2}))");
        Matcher matcher = pattern.matcher(text);

        double maxAmount = 0.0;
        while (matcher.find()) {
            try {
                String match = matcher.group().replace(",", "");
                double val = Double.parseDouble(match);
                if (val > maxAmount) {
                    maxAmount = val;
                }
            } catch (NumberFormatException ignored) {}
        }
        return maxAmount;
    }

    private static String detectCategory(String text) {
        if (matchesAny(text, FOOD_KEYWORDS)) return "Food";
        if (matchesAny(text, SHOPPING_KEYWORDS)) return "Shopping";
        if (matchesAny(text, TRANSPORT_KEYWORDS)) return "Transport";
        if (matchesAny(text, HEALTH_KEYWORDS)) return "Health";
        return "General";
    }

    private static boolean matchesAny(String text, String[] keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    private static String extractTitle(String text) {
        // Very basic: just take the first line as a potential title (merchant name)
        String[] lines = text.split("\\n");
        if (lines.length > 0) {
            String firstLine = lines[0].trim();
            if (firstLine.length() > 20) {
                return firstLine.substring(0, 20);
            }
            return firstLine;
        }
        return "Scanned Bill";
    }
}
