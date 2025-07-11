package com.example.onlinebankingapp;
// PaymentIntentRequest.java
public class PaymentIntentRequest {
    public Data data;

    public PaymentIntentRequest(Data data) {
        this.data = data;
    }

    public static class Data {
        public Attributes attributes;

        public Data(Attributes attributes) {
            this.attributes = attributes;
        }
    }

    public static class Attributes {
        public Long amount;
        public String currency;
        public String description;
        public String[] payment_method_allowed;

        public Attributes(Long amount, String currency, String description, String[] payment_method_allowed) {
            this.amount = amount;
            this.currency = currency;
            this.description = description;
            this.payment_method_allowed = payment_method_allowed;
        }
    }
}

