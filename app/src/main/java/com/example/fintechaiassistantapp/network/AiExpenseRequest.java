package com.example.fintechaiassistantapp.network;

public class AiExpenseRequest {
    private String email;
    private String text;

    public AiExpenseRequest(String email, String text) {
        this.email = email;
        this.text = text;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
