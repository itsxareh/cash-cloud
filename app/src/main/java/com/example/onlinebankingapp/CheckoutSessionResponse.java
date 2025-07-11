package com.example.onlinebankingapp;

public class CheckoutSessionResponse {
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private Attributes attributes;

        public Attributes getAttributes() {
            return attributes;
        }

        public void setAttributes(Attributes attributes) {
            this.attributes = attributes;
        }

        public static class Attributes {
            private String checkoutUrl;

            public String getCheckoutUrl() {
                return checkoutUrl;
            }

            public void setCheckoutUrl(String checkoutUrl) {
                this.checkoutUrl = checkoutUrl;
            }
        }
    }
}
