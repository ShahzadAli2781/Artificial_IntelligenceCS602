package com.example.fintechaiassistantapp.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.fintechaiassistantapp.models.AiInsightEntity;
import java.util.List;

/**
 * Data Access Object for the ai_insights table.
 */
@Dao
public interface AiInsightDao {
    @Insert
    void insert(AiInsightEntity insight);

    @Query("SELECT * FROM ai_insights WHERE userEmail = :email ORDER BY timestamp DESC LIMIT 10")
    LiveData<List<AiInsightEntity>> getLatestInsights(String email);

    @Query("DELETE FROM ai_insights WHERE userEmail = :email")
    void deleteAll(String email);
}
