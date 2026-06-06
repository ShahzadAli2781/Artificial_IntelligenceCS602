package com.example.fintechaiassistantapp.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("predict/{email}")
    Call<ModelResponse> getPrediction(@Path("email") String email);

    @POST("add_expense")
    Call<Void> addExpense(@Body ExpenseRequest request);

    @POST("add_expense_auto")
    Call<AiExpenseResponse> addExpenseAuto(@Body AiExpenseRequest request);

    @POST("chat")
    Call<ChatResponse> chat(@Body ChatRequest request);
}
