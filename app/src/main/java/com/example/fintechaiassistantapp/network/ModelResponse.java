package com.example.fintechaiassistantapp.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.List;
import java.util.List;
import java.util.List;
import java.util.List;
import java.util.List;
import java.util.List;
import java.util.List;
import java.util.List;

public class ModelResponse {

    @SerializedName("final_prediction")
    private double finalPrediction = 0;

    @SerializedName("xgboost_prediction")
    private double xgboostPrediction = 0;

    @SerializedName("transaction_count")
    private int transactionCount = 0;

    @SerializedName("data_points")
    private int dataPoints = 0;

    @SerializedName("model_status")
    private String modelStatus = "Unknown";

    @SerializedName("trend")
    private String trend = "0%";

    @SerializedName("insights")
    private List<Insight> insights;

    public static class Insight {
        @SerializedName("type")
        private String type;
        @SerializedName("message")
        private String message;

        public String getType() { return type; }
        public String getMessage() { return message; }
    }

    // =========================
    // GETTERS
    // =========================

    public List<Insight> getInsights() {
        return insights;
    }

    public String getTrend() {
        return trend;
    }

    public double getFinalPrediction() {
        return finalPrediction;
    }

    public double getXgboostPrediction() {
        return xgboostPrediction;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public int getDataPoints() {
        return dataPoints;
    }

    public String getModelStatus() {
        return modelStatus;
    }
}