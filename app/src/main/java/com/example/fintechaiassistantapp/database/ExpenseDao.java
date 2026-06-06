package com.example.fintechaiassistantapp.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.fintechaiassistantapp.models.ExpenseEntity;

import java.util.List;

/**
 * DAO for Expense operations (Room Database)
 * Production-safe + LiveData support
 */
@Dao
public interface ExpenseDao {

    // ✅ Insert Expense
    @Insert
    void insertExpense(ExpenseEntity expense);

    // ✅ Get all expenses (Live updates for UI)
    @Query("SELECT * FROM expenses WHERE userEmail = :email ORDER BY timestamp DESC")
    LiveData<List<ExpenseEntity>> getAllExpenses(String email);

    // ✅ Sync fetch for background AI processing
    @Query("SELECT * FROM expenses WHERE userEmail = :email ORDER BY timestamp DESC")
    List<ExpenseEntity> getAllExpensesSync(String email);

    // ✅ Total expense (SAFE - no null crash)
    @Query("SELECT IFNULL(SUM(amount), 0) FROM expenses WHERE userEmail = :email")
    LiveData<Double> getTotalExpense(String email);

    // ✅ Update Expense
    @androidx.room.Update
    void updateExpense(ExpenseEntity expense);

    // ✅ Get expense by ID
    @Query("SELECT * FROM expenses WHERE id = :id")
    ExpenseEntity getExpenseByIdSync(int id);

    // ✅ Delete single expense
    @Query("DELETE FROM expenses WHERE id = :id")
    void deleteExpenseById(int id);

    // ✅ Delete all expenses
    @Query("DELETE FROM expenses WHERE userEmail = :email")
    void deleteAllExpenses(String email);

    // ✅ Search expenses
    @Query("SELECT * FROM expenses " +
            "WHERE userEmail = :email AND (title LIKE '%' || :searchQuery || '%' " +
            "OR category LIKE '%' || :searchQuery || '%') " +
            "ORDER BY timestamp DESC")
    LiveData<List<ExpenseEntity>> searchExpenses(String email, String searchQuery);

    // ✅ Filter by category
    @Query("SELECT * FROM expenses WHERE userEmail = :email AND category = :category ORDER BY timestamp DESC")
    LiveData<List<ExpenseEntity>> getExpensesByCategory(String email, String category);

    // ✅ Get recent expenses for ML prediction
    @Query("SELECT * FROM expenses WHERE userEmail = :email ORDER BY timestamp DESC LIMIT :limit")
    List<ExpenseEntity> getRecentExpensesSync(String email, int limit);

    @Query("SELECT * FROM expenses WHERE userEmail = :email AND isSynced = 0")
    List<ExpenseEntity> getUnsyncedExpenses(String email);

    @Query("UPDATE expenses SET isSynced = 1 WHERE id = :id")
    void markAsSynced(int id);

    @Query("SELECT IFNULL(SUM(amount), 0) FROM expenses WHERE userEmail = :email")
    double getTotalExpensesSync(String email);

    @Query("SELECT IFNULL(AVG(amount), 0) FROM expenses WHERE userEmail = :email")
    double getAverageExpenseSync(String email);

    // ✅ Get expenses by time range for reporting
    @Query("SELECT * FROM expenses WHERE userEmail = :email AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    List<ExpenseEntity> getExpensesByTimeRangeSync(String email, long startTime, long endTime);
}