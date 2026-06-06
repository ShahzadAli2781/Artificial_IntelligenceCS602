package com.example.fintechaiassistantapp.models;

public class MonthlyTrendModel {
    private String label; // e.g., "Jan", "Week 1"
    private double amount;

    public MonthlyTrendModel(String label, double amount) {
        this.label = label;
        this.amount = amount;
    }

    public String getLabel() { return label; }
    public double getAmount() { return amount; }
}
