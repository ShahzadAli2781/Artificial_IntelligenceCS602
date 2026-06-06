package com.example.fintechaiassistantapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "income")
public class IncomeEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private double amount;
    private long timestamp;
    private String month; // e.g., "October 2023"
    private String userEmail;

    public IncomeEntity(double amount, long timestamp, String month, String userEmail) {
        this.amount = amount;
        this.timestamp = timestamp;
        this.month = month;
        this.userEmail = userEmail;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}