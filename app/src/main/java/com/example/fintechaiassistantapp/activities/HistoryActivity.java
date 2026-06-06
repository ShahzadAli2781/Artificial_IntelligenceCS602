package com.example.fintechaiassistantapp.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintechaiassistantapp.R;
import com.example.fintechaiassistantapp.adapters.ExpenseAdapter;
import com.example.fintechaiassistantapp.database.AppDatabase;
import com.example.fintechaiassistantapp.models.ExpenseEntity;
import com.example.fintechaiassistantapp.utils.ThreadManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.example.fintechaiassistantapp.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;

import android.content.Intent;
import android.widget.Toast;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private AppDatabase db;
    private android.widget.EditText etSearch;
    private ChipGroup cgFilter;
    private String currentCategory = "All";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        if (savedInstanceState != null) {
            currentCategory = savedInstanceState.getString("category", "All");
            searchQuery = savedInstanceState.getString("query", "");
        }

        db = AppDatabase.getInstance(this);
        
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_history);
        etSearch = findViewById(R.id.et_search);
        cgFilter = findViewById(R.id.cg_filter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExpenseAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnExpenseActionListener(new ExpenseAdapter.OnExpenseActionListener() {
            @Override
            public void onEdit(ExpenseEntity expense) {
                Intent intent = new Intent(HistoryActivity.this, AddExpenseActivity.class);
                intent.putExtra("expense_id", expense.getId());
                intent.putExtra("amount", expense.getAmount());
                intent.putExtra("note", expense.getTitle());
                intent.putExtra("category", expense.getCategory());
                intent.putExtra("is_edit", true);
                startActivity(intent);
            }

            @Override
            public void onShare(ExpenseEntity expense) {
                com.example.fintechaiassistantapp.utils.ReceiptUtils.generateAndSharePdfReceipt(HistoryActivity.this, expense);
            }

            @Override
            public void onPrint(ExpenseEntity expense) {
                com.example.fintechaiassistantapp.utils.ReceiptUtils.printReceipt(HistoryActivity.this, expense);
            }

            @Override
            public void onDelete(ExpenseEntity expense) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(HistoryActivity.this)
                        .setTitle("Delete Transaction")
                        .setMessage("Are you sure you want to delete this expense?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            ThreadManager.runInBackground(() -> {
                                db.expenseDao().deleteExpenseById(expense.getId());
                                runOnUiThread(() -> loadExpenses(currentCategory));
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        if (!searchQuery.isEmpty()) {
            etSearch.setText(searchQuery);
            searchExpenses(searchQuery);
        } else {
            loadExpenses(currentCategory);
        }

        // Restore chip selection
        for (int i = 0; i < cgFilter.getChildCount(); i++) {
            View child = cgFilter.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.getText().toString().equals(currentCategory)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                if (searchQuery.isEmpty()) {
                    loadExpenses(currentCategory);
                } else {
                    searchExpenses(searchQuery);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        cgFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds != null && !checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    currentCategory = chip.getText().toString();
                    if (searchQuery.isEmpty()) {
                        loadExpenses(currentCategory);
                    } else {
                        searchExpenses(searchQuery);
                    }
                }
            } else {
                // Default to All if nothing selected
                currentCategory = "All";
                loadExpenses(currentCategory);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("category", currentCategory);
        outState.putString("query", searchQuery);
    }

    private androidx.lifecycle.LiveData<List<ExpenseEntity>> currentLiveData;

    private void loadExpenses(String category) {
        String userEmail = new SessionManager(this).getUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            return;
        }

        // Remove old observer to prevent memory leak and multiple triggers
        if (currentLiveData != null) {
            currentLiveData.removeObservers(this);
        }

        if (category.equals("All")) {
            currentLiveData = db.expenseDao().getAllExpenses(userEmail);
        } else {
            currentLiveData = db.expenseDao().getExpensesByCategory(userEmail, category);
        }

        currentLiveData.observe(this, expenses -> {
            if (expenses != null) {
                adapter.setExpenses(expenses);
                findViewById(R.id.ll_empty_state).setVisibility(expenses.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void searchExpenses(String query) {
        String userEmail = new SessionManager(this).getUserEmail();
        if (userEmail == null || userEmail.isEmpty()) return;

        if (currentLiveData != null) {
            currentLiveData.removeObservers(this);
        }

        currentLiveData = db.expenseDao().searchExpenses(userEmail, query);
        currentLiveData.observe(this, expenses -> {
            if (expenses != null) {
                adapter.setExpenses(expenses);
                findViewById(R.id.ll_empty_state).setVisibility(expenses.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }
}