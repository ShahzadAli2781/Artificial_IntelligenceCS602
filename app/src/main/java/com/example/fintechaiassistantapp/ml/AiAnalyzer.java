package com.example.fintechaiassistantapp.ml;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.fintechaiassistantapp.database.AppDatabase;
import com.example.fintechaiassistantapp.models.AiInsightEntity;
import com.example.fintechaiassistantapp.models.ExpenseEntity;
import com.example.fintechaiassistantapp.network.ModelResponse;
import com.example.fintechaiassistantapp.repository.PredictionRepository;
import com.example.fintechaiassistantapp.utils.CurrencyUtils;
import com.example.fintechaiassistantapp.utils.NotificationHelper;
import com.example.fintechaiassistantapp.utils.SessionManager;
import com.example.fintechaiassistantapp.utils.ThreadManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AiAnalyzer {
    private Context context;
    private AppDatabase db;
    private NotificationHelper notificationHelper;
    private PredictionRepository predictionRepository;

    public AiAnalyzer(Context context) {
        this.context = context;
        this.db = AppDatabase.getInstance(context);
        this.notificationHelper = new NotificationHelper(context);
        this.predictionRepository = new PredictionRepository();
    }

    public void analyzeAndGenerateInsights() {
        String userEmail = new SessionManager(context).getUserEmail();
        
        // Always clean up old insights for this user first to maintain relevance
        db.aiInsightDao().deleteAll(userEmail);

        List<ExpenseEntity> expenses = db.expenseDao().getAllExpensesSync(userEmail);
        double totalIncome = db.incomeDao().getLatestIncomeAmountSync(userEmail);

        if (expenses == null || expenses.isEmpty()) return;

        double totalSpending = 0;
        Map<String, Double> categoryTotals = new HashMap<>();

        for (ExpenseEntity expense : expenses) {
            totalSpending += expense.getAmount();
            categoryTotals.put(expense.getCategory(), 
                categoryTotals.getOrDefault(expense.getCategory(), 0.0) + expense.getAmount());
        }

        // 1. Detect High Spending Category
        String topCategory = "";
        double maxAmount = 0;
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > maxAmount) {
                maxAmount = entry.getValue();
                topCategory = entry.getKey();
            }
        }

        if (!topCategory.isEmpty() && totalIncome > 0) {
            double incomePercentage = (maxAmount / totalIncome) * 100;
            if (incomePercentage > 40) {
                saveInsight("Budget Warning", 
                    String.format(Locale.getDefault(), "Critical spending in %s (%.1f%% of income).", topCategory, incomePercentage), 
                    "WARNING");
            } else if (incomePercentage > 20) {
                saveInsight("Spending Insight", 
                    String.format(Locale.getDefault(), "You spend a significant portion on %s. Can you optimize this?", topCategory), 
                    "SUGGESTION");
            }
        }

        // 2. Savings Rate Insight
        if (totalIncome > 0) {
            double savingsRate = ((totalIncome - totalSpending) / totalIncome) * 100;
            if (savingsRate > 20) {
                saveInsight("Excellent Work!", "Your savings rate is " + String.format(Locale.getDefault(), "%.1f%%", savingsRate) + ". You're on track for your goals.", "SUGGESTION");
            } else if (savingsRate < 5 && savingsRate > 0) {
                saveInsight("Savings Alert", "Your savings rate is very low (" + String.format(Locale.getDefault(), "%.1f%%", savingsRate) + "). Consider building an emergency fund.", "WARNING");
            }
        }

        // 3. ML-Powered Forecast
        fetchMLPrediction(expenses, userEmail);
    }

    private void fetchMLPrediction(List<ExpenseEntity> expenses, String userEmail) {
        List<Double> amounts = new ArrayList<>();
        // Use last 20 expenses for a better trend
        int count = 0;
        for (int i = expenses.size() - 1; i >= 0 && count < 20; i--) {
            amounts.add(expenses.get(i).getAmount());
            count++;
        }

        if (amounts.isEmpty()) return;

        predictionRepository.fetchPrediction(userEmail,  new PredictionRepository.PredictionCallback() {
            @Override
            public void onSuccess(ModelResponse response) {
                // Use backend's accurate prediction for the main forecast insight
                saveInsight("Monthly Forecast", 
                    String.format(Locale.getDefault(), "Based on AI analysis, your estimated month-end expense is %s. %s", 
                        CurrencyUtils.formatPKR(response.getFinalPrediction()),
                        response.getTrend().startsWith("-") ? "You are saving well!" : "Try to reduce non-essential spending."),
                    "TIP");

                // Sync extra insights sent by backend
                if (response.getInsights() != null) {
                    for (ModelResponse.Insight in : response.getInsights()) {
                        saveInsight("AI Analysis", in.getMessage(), in.getType());
                    }
                }
            }

            @Override
            public void onError(String message) {
                // Fallback to basic projection if backend is down
                double totalSpending = 0;
                for (ExpenseEntity e : expenses) totalSpending += e.getAmount();
                double predictedSpending = totalSpending * 1.05;
                saveInsight("Monthly Forecast", 
                    String.format(Locale.getDefault(), "Based on your current spending pattern, your estimated month-end expense is around %s.", CurrencyUtils.formatPKR(predictedSpending)),
                    "TIP");
            }
        });
    }

    private void saveInsight(String title, String content, String type) {
        String userEmail = new SessionManager(context).getUserEmail();
        String message = title + ": " + content;
        AiInsightEntity insight = new AiInsightEntity(message, type.toLowerCase(), System.currentTimeMillis(), userEmail);

        ThreadManager.runInBackground(() -> {
            try {
                db.aiInsightDao().insert(insight);

                // Show notification for warnings on UI thread
                if (type.equalsIgnoreCase("WARNING")) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            notificationHelper.showAiInsightNotification(title, content));
                }
            } catch (Exception e) {
                Log.e("AiAnalyzer", "Error saving AI insight: " + e.getMessage());
            }
        });
    }
}