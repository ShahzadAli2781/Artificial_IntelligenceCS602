package com.example.fintechaiassistantapp.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.fintechaiassistantapp.models.IncomeEntity;
import java.util.List;

@Dao
public interface IncomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIncome(IncomeEntity income);

    @Query("SELECT * FROM income WHERE userEmail = :email ORDER BY timestamp DESC")
    LiveData<List<IncomeEntity>> getAllIncome(String email);

    @Query("SELECT IFNULL(SUM(amount), 0) FROM income WHERE userEmail = :email")
    LiveData<Double> getTotalIncome(String email);

    @Query("SELECT IFNULL((SELECT amount FROM income WHERE userEmail = :email ORDER BY timestamp DESC LIMIT 1), 0)")
    LiveData<Double> getLatestIncomeAmount(String email);

    @Query("SELECT IFNULL(SUM(amount), 0) FROM income WHERE userEmail = :email")
    double getTotalIncomeSync(String email);

    @Query("SELECT IFNULL((SELECT amount FROM income WHERE userEmail = :email ORDER BY timestamp DESC LIMIT 1), 0)")
    double getLatestIncomeAmountSync(String email);

    @Query("SELECT * FROM income WHERE userEmail = :email ORDER BY timestamp DESC LIMIT 1")
    IncomeEntity getLatestIncomeSync(String email);

    @Query("DELETE FROM income WHERE userEmail = :email")
    void deleteAllIncome(String email);
}