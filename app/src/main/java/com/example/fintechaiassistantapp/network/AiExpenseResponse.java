package com.example.fintechaiassistantapp.network;

public class AiExpenseResponse {
    private String status;
    private String message;
    private ParsedData data;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public ParsedData getData() { return data; }

    public static class ParsedData {
        private String title;
        private double amount;
        private String category;
        private String date;

        public String getTitle() { return title; }
        public double getAmount() { return amount; }
        public String getCategory() { return category; }
        public String getDate() { return date; }
    }
}
