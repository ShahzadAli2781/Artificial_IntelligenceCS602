package com.example.fintechaiassistantapp.analytics;

import com.example.fintechaiassistantapp.models.CategorySummaryModel;
import com.example.fintechaiassistantapp.models.ExpenseEntity;
import com.example.fintechaiassistantapp.models.MonthlyTrendModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ExpenseAnalyticsHelper {

    public static List<CategorySummaryModel> getCategorySummaries(List<ExpenseEntity> expenses) {
        if (expenses == null || expenses.isEmpty()) return new ArrayList<>();

        Map<String, Double> categoryTotals = new HashMap<>();
        double totalSpending = 0;

        for (ExpenseEntity expense : expenses) {
            String category = expense.getCategory();
            double amount = expense.getAmount();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
            totalSpending += amount;
        }

        List<CategorySummaryModel> summaries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            CategorySummaryModel model = new CategorySummaryModel(entry.getKey(), entry.getValue());
            if (totalSpending > 0) {
                model.setPercentage((float) ((entry.getValue() / totalSpending) * 100));
            }
            summaries.add(model);
        }
        return summaries;
    }

    public static List<MonthlyTrendModel> getWeeklyTrend(List<ExpenseEntity> expenses) {
        if (expenses == null || expenses.isEmpty()) return new ArrayList<>();

        // Group by week (Simplified: Last 7 days or weeks of month)
        // For professional look, let's group by last 4 weeks or last 7 days.
        // Let's do Day-wise for the last 7 days for better visualization in BarChart.
        
        Map<String, Double> dailyTotals = new TreeMap<>(); // TreeMap to keep dates sorted
        SimpleDateFormat df = new SimpleDateFormat("MMM dd", Locale.getDefault());
        
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            String dateKey = df.format(cal.getTime());
            dailyTotals.put(dateKey, 0.0);
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }

        for (ExpenseEntity expense : expenses) {
            cal.setTimeInMillis(expense.getTimestamp());
            String dateKey = df.format(cal.getTime());
            if (dailyTotals.containsKey(dateKey)) {
                dailyTotals.put(dateKey, dailyTotals.get(dateKey) + expense.getAmount());
            }
        }

        List<MonthlyTrendModel> trends = new ArrayList<>();
        // Convert to list in correct chronological order (ascending)
        List<String> sortedKeys = new ArrayList<>(dailyTotals.keySet());
        // Since we want chronological order and TreeMap is natural order (string-wise), 
        // and we filled it backwards, we might need more careful handling.
        // Actually, let's just use the Calendar to iterate forward.
        
        trends.clear();
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6);
        for (int i = 0; i < 7; i++) {
            String dateKey = df.format(cal.getTime());
            trends.add(new MonthlyTrendModel(dateKey, dailyTotals.getOrDefault(dateKey, 0.0)));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        return trends;
    }
}
