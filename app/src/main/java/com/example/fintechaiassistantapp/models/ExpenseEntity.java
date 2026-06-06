package com.example.fintechaiassistantapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing an expense record in the database.
 */
@Entity(tableName = "expenses")
public class ExpenseEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private double amount;
    private String category;
    private String date;
    private long timestamp;
    private String userEmail;
    private boolean isSynced = false;

    public ExpenseEntity(String title, double amount, String category, String date, long timestamp, String userEmail) {
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.timestamp = timestamp;
        this.userEmail = userEmail;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }
}
