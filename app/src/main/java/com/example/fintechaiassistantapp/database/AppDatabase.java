package com.example.fintechaiassistantapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.fintechaiassistantapp.models.AiInsightEntity;
import com.example.fintechaiassistantapp.models.ExpenseEntity;
import com.example.fintechaiassistantapp.models.IncomeEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main Room Database for FinIntelligence App
 */
@Database(
        entities = {ExpenseEntity.class, AiInsightEntity.class, IncomeEntity.class},
        version = 4,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    // ✅ DAOs
    public abstract ExpenseDao expenseDao();
    public abstract AiInsightDao aiInsightDao();
    public abstract IncomeDao incomeDao();

    // ✅ Background thread executor (IMPORTANT)
    public static final ExecutorService databaseExecutor =
            Executors.newFixedThreadPool(4);

    // ✅ Singleton Instance
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "finintelligence_db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}