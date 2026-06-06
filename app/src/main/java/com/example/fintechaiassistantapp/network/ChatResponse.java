package com.example.fintechaiassistantapp.network;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ChatResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("intent")
    private String intent;

    @SerializedName("message")
    private String message;

    @SerializedName("advice")
    private String advice;

    @SerializedName("extracted")
    private Map<String, Object> extracted;

    public String getStatus() { return status; }
    public String getIntent() { return intent; }
    public String getMessage() { return message; }
    public String getAdvice() { return advice; }
    public Map<String, Object> getExtracted() { return extracted; }
}
