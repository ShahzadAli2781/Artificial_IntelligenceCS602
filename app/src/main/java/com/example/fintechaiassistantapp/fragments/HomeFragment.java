package com.example.fintechaiassistantapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintechaiassistantapp.R;
import com.example.fintechaiassistantapp.adapters.ExpenseAdapter;
import com.example.fintechaiassistantapp.database.AppDatabase;
import com.example.fintechaiassistantapp.ml.AiAnalyzer;
import com.example.fintechaiassistantapp.models.AiInsightEntity;
import com.example.fintechaiassistantapp.models.ExpenseEntity;
import com.example.fintechaiassistantapp.utils.CurrencyUtils;
import com.example.fintechaiassistantapp.network.SyncManager;
import com.example.fintechaiassistantapp.utils.SessionManager;
import com.example.fintechaiassistantapp.utils.ThreadManager;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private AppDatabase db;
    private SessionManager sessionManager;
    private TextView tvTotalBalance, tvBudgetStatus;
    private TextView tvTotalIncomeDisplay, tvTotalExpenseDisplay;
    private TextView tvSeeAll;
    private com.google.android.material.button.MaterialButton btnScan;
    private com.google.android.material.button.MaterialButton btnAddIncome;
    private com.google.android.material.button.MaterialButton btnAiAssistant;
    private View cardAiInsight;
    private TextView tvInsightTitle, tvInsightContent;
    private ImageView ivInsightIcon;
    private LinearProgressIndicator budgetProgress;
    private AiAnalyzer aiAnalyzer;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;
    private View emptyState;
    
    private com.github.mikephil.charting.charts.PieChart pieChart;
    private com.github.mikephil.charting.charts.BarChart barChart;

    private double currentTotalIncome = 0;
    private double currentTotalExpense = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        db = AppDatabase.getInstance(requireContext());
        aiAnalyzer = new AiAnalyzer(requireContext());
        sessionManager = new SessionManager(requireContext());

        recyclerView = view.findViewById(R.id.recycler_recent);
        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvTotalIncomeDisplay = view.findViewById(R.id.tv_total_income_display);
        tvTotalExpenseDisplay = view.findViewById(R.id.tv_total_expense_display);
        tvBudgetStatus = view.findViewById(R.id.tv_budget_status);
        tvSeeAll = view.findViewById(R.id.tv_see_all);
        btnScan = view.findViewById(R.id.btn_scan_home);
        btnAddIncome = view.findViewById(R.id.btn_add_income);
        btnAiAssistant = view.findViewById(R.id.btn_ai_assistant);
        budgetProgress = view.findViewById(R.id.budget_progress);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_home);
        emptyState = view.findViewById(R.id.ll_empty_home);
        
        pieChart = view.findViewById(R.id.pie_chart);
        barChart = view.findViewById(R.id.bar_chart);
        setupCharts();

        cardAiInsight = view.findViewById(R.id.card_ai_insight);
        tvInsightTitle = view.findViewById(R.id.tv_insight_title);
        tvInsightContent = view.findViewById(R.id.tv_insight_content);
        ivInsightIcon = view.findViewById(R.id.iv_insight_icon);
        
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        adapter = new ExpenseAdapter();
        recyclerView.setAdapter(adapter);
        
        if (tvSeeAll != null) {
            tvSeeAll.setOnClickListener(v -> {
                if (getActivity() instanceof com.example.fintechaiassistantapp.activities.DashboardActivity) {
                    ((com.example.fintechaiassistantapp.activities.DashboardActivity) getActivity()).selectTab(R.id.nav_history);
                } else {
                    startActivity(new android.content.Intent(getActivity(), com.example.fintechaiassistantapp.activities.HistoryActivity.class));
                }
            });
        }

        if (btnScan != null) {
            btnScan.setOnClickListener(v -> {
                startActivity(new android.content.Intent(getActivity(), com.example.fintechaiassistantapp.activities.OcrScannerActivity.class));
            });
        }

        if (btnAddIncome != null) {
            btnAddIncome.setOnClickListener(v -> {
                startActivity(new android.content.Intent(getActivity(), com.example.fintechaiassistantapp.activities.AddIncomeActivity.class));
            });
        }

        if (btnAiAssistant != null) {
            btnAiAssistant.setOnClickListener(v -> {
                startActivity(new android.content.Intent(getActivity(), com.example.fintechaiassistantapp.activities.AiAssistantActivity.class));
            });
        }

        view.findViewById(R.id.card_balance).setOnClickListener(v -> {
            startActivity(new android.content.Intent(getActivity(), com.example.fintechaiassistantapp.activities.PredictionActivity.class));
        });

        setupSwipeRefresh();

        // Lazy load data after the UI is rendered to prevent startup ANRs
        view.post(() -> {
            if (isAdded()) {
                loadData();
                SyncManager.scheduleSync(requireContext());
            }
        });
        
        return view;
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.secondary);
            swipeRefreshLayout.setOnRefreshListener(() -> {
                // Refresh logic
                SyncManager.syncNow(requireContext());
                loadData();
                // AI Analysis can be triggered on manual refresh
                ThreadManager.runInBackground(() -> aiAnalyzer.analyzeAndGenerateInsights());
                
                // Stop refreshing after a delay or when data is loaded
                swipeRefreshLayout.postDelayed(() -> {
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            });
        }
    }

    private void loadData() {
        String userEmail = new SessionManager(requireContext()).getUserEmail();

        // 1. AI Analysis is now decoupled from startup. 
        // It will be triggered only on user action or after a significant delay.

        // 2. Observe Recent Expenses for List
        db.expenseDao().getAllExpenses(userEmail).observe(getViewLifecycleOwner(), allExpenses -> {
            if (allExpenses == null || allExpenses.isEmpty()) {
                if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                adapter.setExpenses(java.util.Collections.emptyList());
                updateAnalytics(java.util.Collections.emptyList());
                return;
            }
            if (emptyState != null) emptyState.setVisibility(View.GONE);
            List<ExpenseEntity> recentExpenses = allExpenses.size() > 5 ? allExpenses.subList(0, 5) : allExpenses;
            adapter.setExpenses(recentExpenses);
            
            // Update Analytics
            updateAnalytics(allExpenses);
        });

        // 3. Observe Latest Monthly Income (Fixed for the month)
        db.incomeDao().getLatestIncomeAmount(userEmail).observe(getViewLifecycleOwner(), latestIncome -> {
            currentTotalIncome = (latestIncome != null) ? latestIncome : 0;
            updateFinancialSummary();
        });

        // 4. Observe Total Expense (Dynamic)
        db.expenseDao().getTotalExpense(userEmail).observe(getViewLifecycleOwner(), totalExpense -> {
            currentTotalExpense = (totalExpense != null) ? totalExpense : 0;
            updateFinancialSummary();
        });

        // 5. Observe Latest Insight
        db.aiInsightDao().getLatestInsights(userEmail).observe(getViewLifecycleOwner(), insights -> {
            AiInsightEntity latestInsight = (insights != null && !insights.isEmpty()) ? insights.get(0) : null;
            updateInsightUi(latestInsight);
        });
    }

    private void setupCharts() {
        if (pieChart != null) {
            pieChart.setUsePercentValues(true);
            pieChart.getDescription().setEnabled(false);
            pieChart.setExtraOffsets(5, 10, 5, 5);
            pieChart.setDragDecelerationFrictionCoef(0.95f);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleColor(android.graphics.Color.TRANSPARENT);
            pieChart.setTransparentCircleRadius(61f);
            pieChart.setEntryLabelColor(android.graphics.Color.WHITE);
            pieChart.setEntryLabelTextSize(10f);
            pieChart.animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
            pieChart.getLegend().setEnabled(true);
        }

        if (barChart != null) {
            barChart.getDescription().setEnabled(false);
            barChart.setDrawGridBackground(false);
            barChart.setDrawBarShadow(false);
            barChart.setDrawValueAboveBar(true);
            barChart.setMaxVisibleValueCount(60);
            barChart.setPinchZoom(false);
            barChart.setDrawGridBackground(false);
            barChart.animateY(1000);
            
            com.github.mikephil.charting.components.XAxis xAxis = barChart.getXAxis();
            xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setGranularity(1f);
            xAxis.setTextColor(getResources().getColor(R.color.text_secondary));
            
            barChart.getAxisLeft().setTextColor(getResources().getColor(R.color.text_secondary));
            barChart.getAxisRight().setEnabled(false);
        }
    }

    private void updateAnalytics(List<ExpenseEntity> expenses) {
        if (expenses == null || expenses.isEmpty()) return;

        // HARDENING: Offload ALL analytics processing to background thread
        ThreadManager.runInBackground(() -> {
            try {
                // 1. Data Aggregation (Heavy)
                List<com.example.fintechaiassistantapp.models.CategorySummaryModel> categories = 
                    com.example.fintechaiassistantapp.analytics.ExpenseAnalyticsHelper.getCategorySummaries(expenses);
                
                List<com.example.fintechaiassistantapp.models.MonthlyTrendModel> trends = 
                    com.example.fintechaiassistantapp.analytics.ExpenseAnalyticsHelper.getWeeklyTrend(expenses);

                // 2. Chart Model Preparation (Heavier than simple data)
                com.github.mikephil.charting.data.PieData pieData = 
                    com.example.fintechaiassistantapp.analytics.ChartDataManager.getPieData(categories);
                
                com.github.mikephil.charting.data.BarData barData = 
                    com.example.fintechaiassistantapp.analytics.ChartDataManager.getBarData(trends);

                // 3. UI Update on Main Thread
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (pieChart != null) {
                            pieChart.setData(pieData);
                            pieChart.invalidate();
                        }

                        if (barChart != null) {
                            barChart.setData(barData);
                            
                            // Setup X-Axis labels using the processed trends
                            barChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter() {
                                @Override
                                public String getFormattedValue(float value) {
                                    int index = (int) value;
                                    if (index >= 0 && index < trends.size()) {
                                        return trends.get(index).getLabel();
                                    }
                                    return "";
                                }
                            });
                            barChart.invalidate();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateFinancialSummary() {
        double balance = currentTotalIncome - currentTotalExpense;
        float manualBudget = sessionManager.getMonthlyBudget();
        double budgetToUse = (manualBudget > 0) ? manualBudget : currentTotalIncome;

        if (tvTotalBalance != null) {
            tvTotalBalance.setText(CurrencyUtils.formatPKR(balance));
        }

        if (tvTotalIncomeDisplay != null) {
            tvTotalIncomeDisplay.setText(CurrencyUtils.formatPKR(currentTotalIncome));
        }

        if (tvTotalExpenseDisplay != null) {
            tvTotalExpenseDisplay.setText(CurrencyUtils.formatPKR(currentTotalExpense));
        }

        // Logic using Manual Budget (Priority) or Income
        if (budgetToUse > 0) {
            int progress = (int) ((currentTotalExpense / budgetToUse) * 100);
            if (budgetProgress != null) {
                budgetProgress.setProgress(Math.min(progress, 100));
                
                // Dynamic Progress Indicator Color
                if (progress > 90) {
                    budgetProgress.setIndicatorColor(getResources().getColor(android.R.color.holo_red_dark));
                } else if (progress > 70) {
                    budgetProgress.setIndicatorColor(getResources().getColor(android.R.color.holo_orange_dark));
                } else {
                    budgetProgress.setIndicatorColor(getResources().getColor(R.color.secondary));
                }
            }

            if (tvBudgetStatus != null) {
                if (progress > 100) {
                    tvBudgetStatus.setText("Exceeded");
                    tvBudgetStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else if (progress > 90) {
                    tvBudgetStatus.setText("Critical");
                    tvBudgetStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else if (progress > 70) {
                    tvBudgetStatus.setText("Warning");
                    tvBudgetStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                } else {
                    tvBudgetStatus.setText("Healthy");
                    tvBudgetStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                }
            }
        } else {
            if (budgetProgress != null) budgetProgress.setProgress(0);
            if (tvBudgetStatus != null) {
                tvBudgetStatus.setText(manualBudget == 0 ? "Set Budget" : "No Data");
                tvBudgetStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        }
    }

    private void updateInsightUi(AiInsightEntity insight) {
        // Display AI Insight
        if (insight != null && cardAiInsight != null) {
            cardAiInsight.setVisibility(View.VISIBLE);
            
            // Allow manual refresh of insights on click
            cardAiInsight.setOnClickListener(v -> {
                com.google.android.material.snackbar.Snackbar.make(v, "Refreshing AI insights...", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                ThreadManager.runInBackground(() -> aiAnalyzer.analyzeAndGenerateInsights());
            });

            // Splitting message if it follows "Title: Content" format
            String fullMessage = insight.getMessage();
            if (fullMessage.contains(": ")) {
                String[] parts = fullMessage.split(": ", 2);
                tvInsightTitle.setText(parts[0]);
                tvInsightContent.setText(parts[1]);
            } else {
                tvInsightTitle.setText("AI Insight");
                tvInsightContent.setText(fullMessage);
            }
            
            if (insight.getType().equalsIgnoreCase("warning")) {
                ivInsightIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                ivInsightIcon.setColorFilter(getResources().getColor(android.R.color.holo_red_light));
            } else {
                ivInsightIcon.setImageResource(android.R.drawable.ic_dialog_info);
                ivInsightIcon.setColorFilter(getResources().getColor(R.color.secondary));
            }
        } else if (cardAiInsight != null) {
            cardAiInsight.setVisibility(View.GONE);
        }
    }

    private void updateUi(double total, AiInsightEntity insight) {
        updateFinancialSummary();
        updateInsightUi(insight);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Removed auto-trigger of AI analysis to prevent startup/resume blocking.
        // Insights are now persistent and updated on user request or idle background tasks.
    }
}