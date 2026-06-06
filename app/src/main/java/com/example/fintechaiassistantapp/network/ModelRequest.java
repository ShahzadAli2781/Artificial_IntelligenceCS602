package com.example.fintechaiassistantapp.network;

import java.util.List;

public class ModelRequest {
    private String userEmail;
    private List<Double> amounts;

    public ModelRequest(String userEmail, List<Double> amounts) {
        this.userEmail = userEmail;
        this.amounts = amounts;
    }

    public String getUserEmail() { return userEmail; }
    public List<Double> getAmounts() { return amounts; }
}
