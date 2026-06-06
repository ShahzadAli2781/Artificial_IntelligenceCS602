package com.example.fintechaiassistantapp.models;

public class VoiceExpenseModel {
    private String category;
    private double amount;
    private String rawText;

    public VoiceExpenseModel(String category, double amount, String rawText) {
        this.category = category;
        this.amount = amount;
        this.rawText = rawText;
    }

    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getRawText() { return rawText; }
}
