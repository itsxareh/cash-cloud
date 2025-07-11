package com.example.onlinebankingapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PayMongoRequest {
    @SerializedName("data")
    public Data data;

    public static class Data {
        @SerializedName("attributes")
        public Attributes attributes;
    }

    public static class Attributes {
        @SerializedName("line_items")
        public List<LineItem> lineItems;
        @SerializedName("payment_method_types")
        public List<String> paymentMethodTypes;
        @SerializedName("reference_number")
        public String referenceNumber;
        @SerializedName("success_url")
        public String success_url;

    }

    public static class LineItem {
        @SerializedName("amount")
        public int amount;
        @SerializedName("currency")
        public String currency;
        @SerializedName("description")
        public String description;

        @SerializedName("name")
        public String name;
        @SerializedName("quantity")
        public int quantity;
    }
}

