package com.example.fintechaiassistantapp.repository;

import android.util.Log;
import com.example.fintechaiassistantapp.network.ApiClient;
import com.example.fintechaiassistantapp.network.ApiService;
import com.example.fintechaiassistantapp.network.ModelResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PredictionRepository {

    private static final String TAG = "PredictionRepository";
    private final ApiService apiService;

    public PredictionRepository() {
        this.apiService = ApiClient.getApiService();
    }

    public interface PredictionCallback {
        void onSuccess(ModelResponse response);
        void onError(String message);
    }

    public void fetchPrediction(String email, PredictionCallback callback) {
        Log.d(TAG, "Fetching prediction for email: " + email);
        apiService.getPrediction(email).enqueue(new Callback<ModelResponse>() {
            @Override
            public void onResponse(Call<ModelResponse> call, Response<ModelResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Prediction fetched successfully");
                    callback.onSuccess(response.body());
                } else {
                    Log.e(TAG, "Server Error: " + response.code());
                    callback.onError("Server Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ModelResponse> call, Throwable t) {
                Log.e(TAG, "Network Error: " + t.getMessage());
                callback.onError("Network Error: " + t.getMessage());
            }
        });
    }
}
