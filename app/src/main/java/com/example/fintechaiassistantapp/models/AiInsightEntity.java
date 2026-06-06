package com.example.fintechaiassistantapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing an AI-generated insight record in the database.
 */
@Entity(tableName = "ai_insights")
public class AiInsightEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String message;
    private String type; // warning, suggestion, info
    private long timestamp;
    private String userEmail;

    public AiInsightEntity(String message, String type, long timestamp, String userEmail) {
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
        this.userEmail = userEmail;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}
