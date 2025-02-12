package com.sp.chatmate;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query; // Add this import for query parameters

public interface GeminiService {


    @POST("v1beta/models/gemini-pro:generateContent")
    Call<GeminiResponse> generateQuiz(
            @Query("key") String apiKey,  // Include API key in query parameter
            @Body GeminiRequest request);  // Request body
}
