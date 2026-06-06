package com.example.fintechaiassistantapp.network;

public class ExpenseRequest {
    private String title;
    private double amount;
    private String category;
    private String date;
    private String email;

    public ExpenseRequest(String title, double amount, String category, String date, String email) {
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.email = email;
    }

    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getDate() { return date; }
    public String getEmail() { return email; }
}
