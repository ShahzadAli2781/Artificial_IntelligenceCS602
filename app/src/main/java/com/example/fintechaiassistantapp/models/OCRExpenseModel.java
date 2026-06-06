package com.example.fintechaiassistantapp.models;

public class OCRExpenseModel {
    private String title;
    private double amount;
    private String category;

    public OCRExpenseModel(String title, double amount, String category) {
        this.title = title;
        this.amount = amount;
        this.category = category;
    }

    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
}
