package com.example.fintechaiassistantapp.models;

public class CategorySummaryModel {
    private String category;
    private double totalAmount;
    private float percentage;

    public CategorySummaryModel(String category, double totalAmount) {
        this.category = category;
        this.totalAmount = totalAmount;
    }

    public String getCategory() { return category; }
    public double getTotalAmount() { return totalAmount; }
    public float getPercentage() { return percentage; }
    public void setPercentage(float percentage) { this.percentage = percentage; }
}
