package com.example.onlinebankingapp;

import com.google.gson.annotations.SerializedName;

public class PayMongoResponse {
    @SerializedName("data")
    public Data data;

    public static class Data {
        @SerializedName("attributes")
        public Attributes attributes;
    }

    public static class Attributes {
        @SerializedName("checkout_url")
        public String checkoutUrl;
        @SerializedName("success_url")
        public String success_url;

    }
}
