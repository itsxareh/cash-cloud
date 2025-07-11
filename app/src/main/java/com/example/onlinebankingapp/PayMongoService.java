package com.example.onlinebankingapp;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PayMongoService {
    @POST("v1/checkout_sessions")
    Call<PayMongoResponse> createCheckoutSession(@Body PayMongoRequest request);
}
