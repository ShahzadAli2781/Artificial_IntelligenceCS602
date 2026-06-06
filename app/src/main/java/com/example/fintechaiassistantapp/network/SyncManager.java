package com.example.fintechaiassistantapp.network;

import android.content.Context;
import android.util.Log;
import com.example.fintechaiassistantapp.database.AppDatabase;
import com.example.fintechaiassistantapp.models.ExpenseEntity;
import com.example.fintechaiassistantapp.utils.SessionManager;
import com.example.fintechaiassistantapp.utils.ThreadManager;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import java.io.IOException;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncManager {
    private static final String TAG = "SyncManager";
    private final AppDatabase db;
    private final SessionManager sessionManager;
    private final ApiService apiService;

    public SyncManager(Context context) {
        this.db = AppDatabase.getInstance(context);
        this.sessionManager = new SessionManager(context);
        this.apiService = ApiClient.getApiService();
    }

    public static void syncNow(Context context) {
        ThreadManager.runInBackground(() -> {
            new SyncManager(context).syncUnsyncedExpenses();
        });
    }

    public boolean syncUnsyncedExpenses() {
        String email = sessionManager.getUserEmail();
        if (email == null || email.isEmpty()) return true;

        List<ExpenseEntity> unsynced = db.expenseDao().getUnsyncedExpenses(email);
        if (unsynced == null || unsynced.isEmpty()) {
            Log.d(TAG, "No unsynced expenses found.");
            return true;
        }

        Log.d(TAG, "Found " + unsynced.size() + " unsynced expenses. Syncing...");
        boolean allSynced = true;
        for (ExpenseEntity expense : unsynced) {
            if (!syncExpenseBlocking(expense)) {
                allSynced = false;
            }
        }
        return allSynced;
    }

    public static void scheduleSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(
                        androidx.work.BackoffPolicy.EXPONENTIAL,
                        androidx.work.OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                        java.util.concurrent.TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueue(syncRequest);
    }

    private boolean syncExpenseBlocking(ExpenseEntity expense) {
        ExpenseRequest request = new ExpenseRequest(
                expense.getTitle(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate(),
                expense.getUserEmail()
        );

        try {
            Response<Void> response = apiService.addExpense(request).execute();
            if (response.isSuccessful()) {
                Log.d(TAG, "Sync success for expense: " + expense.getId());
                db.expenseDao().markAsSynced(expense.getId());
                return true;
            } else {
                Log.e(TAG, "Sync failed for expense: " + expense.getId() + ", code: " + response.code());
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "Sync network failure for expense: " + expense.getId(), e);
            return false;
        }
    }
}
