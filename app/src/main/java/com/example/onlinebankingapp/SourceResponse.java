package com.example.onlinebankingapp;

public class SourceResponse {
    private Data data;

    public Data getData() {
        return data;
    }

    public static class Data {
        private String id;
        private Attributes attributes;

        public String getId() {
            return id;
        }

        public Attributes getAttributes() {
            return attributes;
        }

        public static class Attributes {
            private String redirect_url;

            public String getRedirectUrl() {
                return redirect_url;
            }
        }
    }
}
