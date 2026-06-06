package com.example.fintechaiassistantapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintechaiassistantapp.R;
import com.example.fintechaiassistantapp.adapters.InsightAdapter;
import com.example.fintechaiassistantapp.database.AppDatabase;
import com.example.fintechaiassistantapp.models.ExpenseEntity;
import com.example.fintechaiassistantapp.network.ModelResponse;
import com.example.fintechaiassistantapp.repository.PredictionRepository;
import com.example.fintechaiassistantapp.utils.CurrencyUtils;
import com.example.fintechaiassistantapp.utils.SessionManager;
import com.example.fintechaiassistantapp.utils.ThreadManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrendsFragment extends Fragment {

    private RecyclerView recyclerView;
    private InsightAdapter adapter;
    private AppDatabase db;

    private TextView tvPredictedAmount, tvTrend;
    private LineChart lineChart;

    private double currentTotalIncome = 0;
    private PredictionRepository predictionRepository;

    // ✅ SAFE CACHE (prevents duplicate API calls)
    private String lastPredictionEmail = null;
    private List<ExpenseEntity> cachedExpenses = new ArrayList<>();
    private Double cachedForecast = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_prediction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDatabase.getInstance(requireContext());
        predictionRepository = new PredictionRepository();

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setNavigationIcon(null);

        recyclerView = view.findViewById(R.id.recycler_insights);
        tvPredictedAmount = view.findViewById(R.id.tv_predicted_amount);
        tvTrend = view.findViewById(R.id.tv_trend);
        lineChart = view.findViewById(R.id.prediction_chart);

        setupChart();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InsightAdapter();
        recyclerView.setAdapter(adapter);

        view.post(this::loadData);
    }

    // -------------------------
    // CHART SETUP
    // -------------------------
    private void setupChart() {
        if (lineChart == null) return;

        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setPosition(
                com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        );
        lineChart.getLegend().setEnabled(false);
        lineChart.setTouchEnabled(false);
    }

    // -------------------------
    // LOAD DATA (DB OBSERVERS)
    // -------------------------
    private void loadData() {

        String email = new SessionManager(requireContext()).getUserEmail();

        db.aiInsightDao().getLatestInsights(email)
                .observe(getViewLifecycleOwner(), adapter::setInsights);

        db.incomeDao().getLatestIncomeAmount(email)
                .observe(getViewLifecycleOwner(), income ->
                        currentTotalIncome = (income != null ? income : 0)
                );

        db.expenseDao().getAllExpenses(email)
                .observe(getViewLifecycleOwner(), expenses -> {

                    double totalExpense = 0;

                    if (expenses != null) {
                        this.cachedExpenses = expenses;
                        for (ExpenseEntity e : expenses) {
                            totalExpense += e.getAmount();
                        }
                    }

                    updateLocalPrediction(totalExpense);
                    updateChart(expenses);

                    // ✅ SAFE: call backend only once per user session
                    triggerPredictionOnce(email);
                });
    }

    // -------------------------
    // PREVENT DUPLICATE API CALL
    // -------------------------
    private void triggerPredictionOnce(String email) {

        if (lastPredictionEmail != null && lastPredictionEmail.equals(email)) {
            return; // already called
        }

        lastPredictionEmail = email;
        fetchRealPrediction();
    }

    // -------------------------
    // LOCAL PREDICTION
    // -------------------------
    private void updateLocalPrediction(double totalExpense) {

        double predicted = totalExpense * 1.10;

        double trendPercent = 0;
        if (currentTotalIncome > 0) {
            trendPercent = (totalExpense / currentTotalIncome) * 100;
        }

        if (tvPredictedAmount != null) {
            tvPredictedAmount.setText(CurrencyUtils.formatPKR(predicted));
        }

        if (tvTrend != null) {
            tvTrend.setText(String.format(Locale.getDefault(), "↑ %.1f%%", trendPercent));
        }
    }

    // -------------------------
    // CHART UPDATE
    // -------------------------
    private void updateChart(List<ExpenseEntity> expenses) {

        if (lineChart == null || ((expenses == null || expenses.isEmpty()) && cachedForecast == null)) return;

        List<Entry> entries = new ArrayList<>();
        int count = 0;

        if (expenses != null && !expenses.isEmpty()) {
            count = Math.min(expenses.size(), 10);
            for (int i = 0; i < count; i++) {
                entries.add(new Entry(i,
                        (float) expenses.get(count - 1 - i).getAmount()));
            }
        }

        // ✅ Append AI Forecast point at the end
        if (cachedForecast != null) {
            entries.add(new Entry(count, cachedForecast.floatValue()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Spending Trend");
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        lineChart.setData(new LineData(dataSet));
        lineChart.invalidate();
    }

    // -------------------------
    // BACKEND PREDICTION
    // -------------------------
    private void fetchRealPrediction() {

        String email = new SessionManager(requireContext()).getUserEmail();

        ThreadManager.runInBackground(() -> {

            List<ExpenseEntity> lastExpenses =
                    db.expenseDao().getRecentExpensesSync(email, 10);

            List<Double> amounts = new ArrayList<>();

            for (ExpenseEntity e : lastExpenses) {
                amounts.add(e.getAmount());
            }

            if (amounts.isEmpty()) return;

            predictionRepository.fetchPrediction(
                    email,

                    new PredictionRepository.PredictionCallback() {

                        @Override
                        public void onSuccess(ModelResponse response) {

                            if (!isAdded() || getActivity() == null) return;

                            getActivity().runOnUiThread(() -> {
                                    tvPredictedAmount.setText(
                                            CurrencyUtils.formatPKR(response.getFinalPrediction())
                                    );

                                    // ✅ Save forecast and refresh chart to show the new point
                                    cachedForecast = response.getFinalPrediction();
                                    updateChart(cachedExpenses);
                            });
                        }

                        @Override
                        public void onError(String message) {
                            // fallback already active
                        }
                    }
            );
        });
    }
}