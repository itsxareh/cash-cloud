package com.example.onlinebankingapp;

public class SourceRequest {
    private Data data;

    public SourceRequest(Data data) {
        this.data = data;
    }

    public static class Data {
        private Attributes attributes;

        public Data(Attributes attributes) {
            this.attributes = attributes;
        }

        public static class Attributes {
            private String type;
            private long amount;
            private String currency;
            private Redirect redirect;

            public Attributes(String type, long amount, String currency, Redirect redirect) {
                this.type = type;
                this.amount = amount;
                this.currency = currency;
                this.redirect = redirect;
            }

            public static class Redirect {
                private String success;
                private String failed;

                public Redirect(String success, String failed) {
                    this.success = success;
                    this.failed = failed;
                }
            }
        }
    }
}