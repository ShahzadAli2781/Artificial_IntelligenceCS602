package com.example.fintechaiassistantapp.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import android.widget.Toast;
import android.print.PrintDocumentInfo;

import java.util.List;

import android.content.Intent;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.content.Context;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private AppDatabase db;
    private android.widget.EditText etSearch;
    private ChipGroup cgFilter;
    private View emptyState;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;
    private String currentCategory = "All";
    private String searchQuery = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_history, container, false);

        if (savedInstanceState != null) {
            currentCategory = savedInstanceState.getString("category", "All");
            searchQuery = savedInstanceState.getString("query", "");
        }

        db = AppDatabase.getInstance(requireContext());
        
        recyclerView = view.findViewById(R.id.recycler_history);
        etSearch = view.findViewById(R.id.et_search);
        cgFilter = view.findViewById(R.id.cg_filter);
        emptyState = view.findViewById(R.id.ll_empty_state);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_history);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExpenseAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnExpenseActionListener(new ExpenseAdapter.OnExpenseActionListener() {
            @Override
            public void onEdit(ExpenseEntity expense) {
                Intent intent = new Intent(requireContext(), com.example.fintechaiassistantapp.activities.AddExpenseActivity.class);
                intent.putExtra("expense_id", expense.getId());
                intent.putExtra("amount", expense.getAmount());
                intent.putExtra("note", expense.getTitle());
                intent.putExtra("category", expense.getCategory());
                intent.putExtra("is_edit", true);
                startActivity(intent);
            }

            @Override
            public void onShare(ExpenseEntity expense) {
                com.example.fintechaiassistantapp.utils.ReceiptUtils.generateAndSharePdfReceipt(requireContext(), expense);
            }

            @Override
            public void onPrint(ExpenseEntity expense) {
                com.example.fintechaiassistantapp.utils.ReceiptUtils.printReceipt(requireContext(), expense);
            }

            @Override
            public void onDelete(ExpenseEntity expense) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.delete)
                        .setMessage("Are you sure you want to delete this expense?")
                        .setPositiveButton(R.string.delete, (dialog, which) -> {
                            ThreadManager.runInBackground(() -> {
                                db.expenseDao().deleteExpenseById(expense.getId());
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

        // Set chip selection based on restored state
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
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    currentCategory = chip.getText().toString();
                    if (searchQuery.isEmpty()) {
                        loadExpenses(currentCategory);
                    }
                }
            }
        });

        setupSwipeRefresh();

        // Lazy load data after the view is fully created to avoid blocking the UI thread during navigation
        view.post(() -> {
            if (isAdded()) {
                if (!searchQuery.isEmpty()) {
                    etSearch.setText(searchQuery);
                    searchExpenses(searchQuery);
                } else {
                    loadExpenses(currentCategory);
                }
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.history_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_report) {
            showReportTypeDialog();
            return true;
        } else if (id == R.id.action_export_csv) {
            exportToCSV();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportToCSV() {
        String userEmail = new SessionManager(requireContext()).getUserEmail();
        // We can export the current visible list or all data. Usually, users want all data.
        db.expenseDao().getAllExpenses(userEmail).observe(getViewLifecycleOwner(), expenses -> {
            if (expenses == null || expenses.isEmpty()) {
                Toast.makeText(getContext(), "No data to export", Toast.LENGTH_SHORT).show();
                return;
            }
            
            StringBuilder csvData = new StringBuilder();
            csvData.append("ID,Title,Amount,Category,Date,Note\n");
            
            for (ExpenseEntity expense : expenses) {
                csvData.append(expense.getId()).append(",")
                        .append("\"").append(expense.getTitle().replace("\"", "\"\"")).append("\",")
                        .append(expense.getAmount()).append(",")
                        .append("\"").append(expense.getCategory()).append("\",")
                        .append("\"").append(expense.getDate()).append("\",")
                        .append("\"").append(expense.getTitle().replace("\"", "\"\"")).append("\"\n");
            }

            shareCSVFile(csvData.toString());
        });
    }

    private void shareCSVFile(String csvContent) {
        try {
            String filename = "Expenses_" + System.currentTimeMillis() + ".csv";
            java.io.File file = new java.io.File(requireContext().getCacheDir(), filename);
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(csvContent);
            writer.close();

            android.net.Uri contentUri = androidx.core.content.FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
                    file);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Expense Report CSV");
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Export CSV via"));

        } catch (IOException e) {
            Toast.makeText(getContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showReportTypeDialog() {
        String[] reportTypes = {"Daily", "Weekly", "Monthly", "Yearly"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Report Period")
                .setItems(reportTypes, (dialog, which) -> {
                    generateReport(reportTypes[which]);
                })
                .show();
    }

    private void generateReport(String type) {
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        long startTime;

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (type.equals("Daily")) {
            startTime = calendar.getTimeInMillis();
        } else if (type.equals("Weekly")) {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            startTime = calendar.getTimeInMillis();
        } else if (type.equals("Monthly")) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            startTime = calendar.getTimeInMillis();
        } else { // Yearly
            calendar.set(Calendar.DAY_OF_YEAR, 1);
            startTime = calendar.getTimeInMillis();
        }

        ThreadManager.runInBackground(() -> {
            String userEmail = new SessionManager(requireContext()).getUserEmail();
            List<ExpenseEntity> expenses = db.expenseDao().getExpensesByTimeRangeSync(userEmail, startTime, endTime);
            
            requireActivity().runOnUiThread(() -> {
                if (expenses.isEmpty()) {
                    android.widget.Toast.makeText(requireContext(), "No data found for this period", android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    printReport(type, expenses);
                }
            });
        });
    }

    private void printReport(String type, List<ExpenseEntity> expenses) {
        PrintManager printManager = (PrintManager) requireContext().getSystemService(Context.PRINT_SERVICE);
        String jobName = getString(R.string.app_name) + " " + type + " Report";

        printManager.print(jobName, new PrintDocumentAdapter() {
            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
                if (cancellationSignal.isCanceled()) {
                    callback.onLayoutCancelled();
                    return;
                }
                PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(jobName).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build();
                callback.onLayoutFinished(pdi, true);
            }

            @Override
            public void onWrite(android.print.PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
                PdfDocument pdfDocument = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 800, 1).create();
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                
                int y = 40;
                paint.setTextSize(16);
                paint.setFakeBoldText(true);
                canvas.drawText("FinIntelligence " + type + " Report", 50, y, paint);
                
                y += 30;
                paint.setTextSize(10);
                paint.setFakeBoldText(false);
                paint.setColor(Color.GRAY);
                canvas.drawText("Generated on: " + java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()), 20, y, paint);
                
                y += 40;
                paint.setColor(Color.BLACK);
                paint.setFakeBoldText(true);
                canvas.drawText("Title", 20, y, paint);
                canvas.drawText("Category", 120, y, paint);
                canvas.drawText("Amount", 220, y, paint);
                
                y += 10;
                canvas.drawLine(20, y, 280, y, paint);
                
                paint.setFakeBoldText(false);
                double total = 0;
                for (ExpenseEntity expense : expenses) {
                    y += 25;
                    if (y > 750) break; // Simple page limit for now
                    
                    String title = expense.getTitle();
                    if (title.length() > 15) title = title.substring(0, 12) + "...";
                    
                    canvas.drawText(title, 20, y, paint);
                    canvas.drawText(expense.getCategory(), 120, y, paint);
                    canvas.drawText(com.example.fintechaiassistantapp.utils.CurrencyUtils.formatPKR(expense.getAmount()), 220, y, paint);
                    total += expense.getAmount();
                }
                
                y += 30;
                canvas.drawLine(20, y, 280, y, paint);
                y += 20;
                paint.setFakeBoldText(true);
                canvas.drawText("TOTAL EXPENSES:", 20, y, paint);
                canvas.drawText(com.example.fintechaiassistantapp.utils.CurrencyUtils.formatPKR(total), 220, y, paint);

                pdfDocument.finishPage(page);

                try {
                    pdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));
                } catch (IOException e) {
                    callback.onWriteFailed(e.toString());
                    return;
                } finally {
                    pdfDocument.close();
                }
                callback.onWriteFinished(new android.print.PageRange[]{android.print.PageRange.ALL_PAGES});
            }
        }, null);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("category", currentCategory);
        outState.putString("query", searchQuery);
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.secondary);
            swipeRefreshLayout.setOnRefreshListener(() -> {
                com.example.fintechaiassistantapp.network.SyncManager.syncNow(requireContext());
                if (searchQuery.isEmpty()) {
                    loadExpenses(currentCategory);
                } else {
                    searchExpenses(searchQuery);
                }
                swipeRefreshLayout.postDelayed(() -> {
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            });
        }
    }

    private void loadExpenses(String category) {
        String userEmail = new SessionManager(requireContext()).getUserEmail();
        if (category.equals("All")) {
            db.expenseDao().getAllExpenses(userEmail).observe(getViewLifecycleOwner(), expenses -> {
                updateList(expenses);
            });
        } else {
            db.expenseDao().getExpensesByCategory(userEmail, category).observe(getViewLifecycleOwner(), expenses -> {
                updateList(expenses);
            });
        }
    }

    private void searchExpenses(String query) {
        String userEmail = new SessionManager(requireContext()).getUserEmail();
        db.expenseDao().searchExpenses(userEmail, query).observe(getViewLifecycleOwner(), expenses -> {
            updateList(expenses);
        });
    }

    private void updateList(List<ExpenseEntity> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
            adapter.setExpenses(java.util.Collections.emptyList());
        } else {
            if (emptyState != null) emptyState.setVisibility(View.GONE);
            adapter.setExpenses(expenses);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (searchQuery.isEmpty()) {
            loadExpenses(currentCategory);
        } else {
            searchExpenses(searchQuery);
        }
    }
}