package com.example.fintechaiassistantapp.network;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.concurrent.TimeUnit;

public class SyncWorker extends Worker {
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        boolean success = new SyncManager(getApplicationContext()).syncUnsyncedExpenses();
        if (success) {
            return Result.success();
        } else {
            // Retry with exponential backoff if configured in the request
            return Result.retry();
        }
    }
}
