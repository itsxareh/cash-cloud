package com.example.onlinebankingapp;

import java.util.List;

public class CheckoutSessionRequest {
    private Data data;

    public CheckoutSessionRequest(double amountValue, String type, List<LineItem> lineItems, List<String> paymentMethodTypes) {
        this.data = new Data(amountValue, type, lineItems, paymentMethodTypes);
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private Attributes attributes;

        public Data(double amountValue, String type, List<LineItem> lineItems, List<String> paymentMethodTypes) {
            this.attributes = new Attributes(amountValue, type, lineItems, paymentMethodTypes);
        }

        public Attributes getAttributes() {
            return attributes;
        }

        public void setAttributes(Attributes attributes) {
            this.attributes = attributes;
        }

        public static class Attributes {
            private double amount;
            private String currency = "PHP"; // Set a default value or make this dynamic
            private String type;
            private List<LineItem> line_items;
            private List<String> payment_method_types;

            public Attributes(double amount, String type, List<LineItem> lineItems, List<String> paymentMethodTypes) {
                this.amount = amount;
                this.type = type;
                this.line_items = lineItems;
                this.payment_method_types = paymentMethodTypes;
            }

            public double getAmount() {
                return amount;
            }

            public void setAmount(double amount) {
                this.amount = amount;
            }

            public String getCurrency() {
                return currency;
            }

            public void setCurrency(String currency) {
                this.currency = currency;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public List<LineItem> getLineItems() {
                return line_items;
            }

            public void setLineItems(List<LineItem> lineItems) {
                this.line_items = lineItems;
            }

            public List<String> getPaymentMethodTypes() {
                return payment_method_types;
            }

            public void setPaymentMethodTypes(List<String> paymentMethodTypes) {
                this.payment_method_types = paymentMethodTypes;
            }
        }
    }

    public static class LineItem {
        private String name;
        private int quantity;
        private double amount;

        public LineItem(String name, int quantity, double amount) {
            this.name = name;
            this.quantity = quantity;
            this.amount = amount;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }
}
