package com.example.fintechaiassistantapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintechaiassistantapp.R;
import com.example.fintechaiassistantapp.adapters.InsightAdapter;
import com.example.fintechaiassistantapp.database.AppDatabase;
import com.example.fintechaiassistantapp.models.ExpenseEntity;
import com.example.fintechaiassistantapp.utils.CurrencyUtils;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import com.example.fintechaiassistantapp.network.ModelResponse;
import com.example.fintechaiassistantapp.repository.PredictionRepository;
import com.example.fintechaiassistantapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import android.graphics.Color;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import com.example.fintechaiassistantapp.utils.NetworkUtils;
import com.google.android.material.color.MaterialColors;

public class PredictionActivity extends AppCompatActivity {

    private InsightAdapter adapter;
    private AppDatabase db;
    private TextView tvPredictedAmount, tvTrend;
    private TextView tvForecastTitle, tvForecastSubtitle, tvTransactionCount, tvConfidenceLabel, tvModelStatus, tvPredictionLabel;
    private LineChart lineChart;
    private android.widget.ProgressBar pbLoading;
    private PredictionRepository predictionRepository;


    private double totalExpense = 0;
    private List<ExpenseEntity> recentExpensesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        db = AppDatabase.getInstance(this);
        predictionRepository = new PredictionRepository();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        RecyclerView recyclerView = findViewById(R.id.recycler_insights);
        tvPredictedAmount = findViewById(R.id.tv_predicted_amount);
        tvTrend = findViewById(R.id.tv_trend);

        tvForecastTitle = findViewById(R.id.tv_forecast_title);
        tvForecastSubtitle = findViewById(R.id.tv_forecast_subtitle);
        tvTransactionCount = findViewById(R.id.tv_transaction_count);
        tvConfidenceLabel = findViewById(R.id.tv_confidence_label);
        tvModelStatus = findViewById(R.id.tv_model_status);
        tvPredictionLabel = findViewById(R.id.tv_prediction_label);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InsightAdapter();
        recyclerView.setAdapter(adapter);

        lineChart = findViewById(R.id.prediction_chart);
        pbLoading = findViewById(R.id.pb_chart_loading);
        setupChart();

        loadData();
    }

    private void setupChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getXAxis().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        
        int gridColor = MaterialColors.getColor(lineChart, com.google.android.material.R.attr.colorOutlineVariant);
        lineChart.getAxisLeft().setGridColor(gridColor);
        lineChart.getAxisLeft().setTextColor(MaterialColors.getColor(lineChart, com.google.android.material.R.attr.colorOnSurfaceVariant));
    }

    private void loadData() {

        String userEmail = new SessionManager(this).getUserEmail();

        db.expenseDao().getAllExpenses(userEmail).observe(this, expenses -> {

            if (expenses != null) {

                totalExpense = 0;
                recentExpensesList = expenses;

                // Sort expenses by date if not already
                List<ExpenseEntity> sortedExpenses = new ArrayList<>(expenses);
                Collections.sort(sortedExpenses, (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));

                List<Entry> entries = new ArrayList<>();
                double cumulativeTotal = 0;
                
                // Show last 15 entries for a clear trend
                int startIndex = Math.max(0, sortedExpenses.size() - 15);
                for (int i = startIndex; i < sortedExpenses.size(); i++) {
                    cumulativeTotal += sortedExpenses.get(i).getAmount();
                    entries.add(new Entry(i - startIndex, (float) cumulativeTotal));
                }

                updateChart(entries);
                fetchMLPrediction();
            }
        });
    }

    private void updateChart(List<Entry> entries) {
        if (entries.isEmpty()) return;

        int primaryColor = ContextCompat.getColor(this, R.color.primary);
        LineDataSet dataSet = new LineDataSet(entries, "Expenses");
        dataSet.setColor(primaryColor);
        dataSet.setCircleColor(primaryColor);
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(false);

        lineChart.setData(new LineData(dataSet));
        lineChart.invalidate();
    }



    private void fetchMLPrediction() {
        if (recentExpensesList.isEmpty()) return;

        if (!NetworkUtils.isNetworkAvailable(this)) {
            tvModelStatus.setText("Offline - Showing Estimate");
            tvModelStatus.setVisibility(View.VISIBLE);
            // Simple fallback if offline
            double estimate = totalExpense * 1.1; 
            tvPredictedAmount.setText(CurrencyUtils.formatPKR(estimate));
            return;
        }

        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);

        String userEmail = new SessionManager(this).getUserEmail();
        predictionRepository.fetchPrediction(userEmail, new PredictionRepository.PredictionCallback() {
            @Override
            public void onSuccess(ModelResponse response) {
                runOnUiThread(() -> {
                    if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                    
                    double predictedAmount = response.getFinalPrediction();
                    tvPredictedAmount.setText(CurrencyUtils.formatPKR(predictedAmount));

                    tvTransactionCount.setText("Transactions: " + response.getTransactionCount());
                    tvModelStatus.setText(response.getModelStatus());
                    tvModelStatus.setVisibility(View.VISIBLE);

                    // Update Trend
                    tvTrend.setText(response.getTrend());
                    if (response.getTrend() != null && response.getTrend().startsWith("-")) {
                        tvTrend.setTextColor(ContextCompat.getColor(PredictionActivity.this, R.color.success)); 
                    } else {
                        tvTrend.setTextColor(ContextCompat.getColor(PredictionActivity.this, R.color.error));
                    }

                    // Update Insights from ML
                    if (response.getInsights() != null && !response.getInsights().isEmpty()) {
                        List<com.example.fintechaiassistantapp.models.AiInsightEntity> insightEntities = new ArrayList<>();
                        for (ModelResponse.Insight insight : response.getInsights()) {
                            insightEntities.add(new com.example.fintechaiassistantapp.models.AiInsightEntity(
                                    insight.getMessage(),
                                    insight.getType(),
                                    System.currentTimeMillis(),
                                    userEmail
                            ));
                        }
                        adapter.setInsights(insightEntities);
                    }

                    // Accuracy Confidence
                    int count = response.getTransactionCount();
                    if(count < 10) tvConfidenceLabel.setText("Confidence: LOW");
                    else if(count < 30) tvConfidenceLabel.setText("Confidence: MEDIUM");
                    else tvConfidenceLabel.setText("Confidence: HIGH");

                    // Add predicted point to chart
                    addPredictionToChart((float) predictedAmount);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                });
            }
        });
    }

    private void addPredictionToChart(float predictedTotal) {
        if (lineChart.getData() == null) return;
        
        LineDataSet currentSet = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
        if (currentSet == null || currentSet.getEntryCount() == 0) return;

        // 1. Get the last actual data point
        Entry lastActualEntry = currentSet.getEntryForIndex(currentSet.getEntryCount() - 1);
        
        // 2. Create the forecast data set (only the last point and the prediction)
        List<Entry> forecastEntries = new ArrayList<>();
        forecastEntries.add(lastActualEntry);
        forecastEntries.add(new Entry(lastActualEntry.getX() + 1, predictedTotal));

        LineDataSet forecastSet = new LineDataSet(forecastEntries, "Forecast");
        
        int primaryColor = ContextCompat.getColor(this, R.color.primary);
        int secondaryColor = ContextCompat.getColor(this, R.color.secondary);

        forecastSet.setColor(primaryColor);
        forecastSet.setCircleColor(secondaryColor);
        forecastSet.setLineWidth(2.5f);
        forecastSet.setCircleRadius(5f);
        forecastSet.setDrawCircleHole(true);
        forecastSet.setCircleHoleColor(Color.WHITE);
        
        // Make it visually distinct (dashed)
        forecastSet.enableDashedLine(10f, 10f, 0f);
        forecastSet.setDrawValues(true);
        forecastSet.setValueTextSize(11f);
        forecastSet.setValueTextColor(ContextCompat.getColor(this, R.color.text_primary));

        // 3. Add both sets to the chart data
        LineData lineData = new LineData();
        lineData.addDataSet(currentSet);
        lineData.addDataSet(forecastSet);
        
        lineChart.setData(lineData);
        lineChart.invalidate();
    }
}