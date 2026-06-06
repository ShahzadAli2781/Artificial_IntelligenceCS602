package com.example.fintechaiassistantapp.network;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ChatRequest {
    @SerializedName("text")
    private String text;

    @SerializedName("user_data")
    private Map<String, Object> userData;

    public ChatRequest(String text, Map<String, Object> userData) {
        this.text = text;
        this.userData = userData;
    }

    public String getText() { return text; }
    public Map<String, Object> getUserData() { return userData; }
}
