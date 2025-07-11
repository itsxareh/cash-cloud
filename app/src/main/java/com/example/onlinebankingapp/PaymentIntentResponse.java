package com.example.onlinebankingapp;
public class PaymentIntentResponse {
    public Data data;

    public static class Data {
        public String id;
        public Attributes attributes;

        public static class Attributes {
            public String client_key;
            public String status;
        }
    }
}

