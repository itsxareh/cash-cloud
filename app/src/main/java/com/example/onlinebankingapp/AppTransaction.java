package com.example.onlinebankingapp;

public class AppTransaction implements Comparable<AppTransaction> {
    private double amount;
    private long dateTime;
    private String type;
    private String description;
    private String reference;

    public AppTransaction() {
    }

    public AppTransaction(double amount, long dateTime, String type, String description, String reference) {
        this.amount = amount;
        this.dateTime = dateTime;
        this.type = type;
        this.description = description;
        this.reference = reference;
    }
    public int compareTo(AppTransaction other) {
        return Long.compare(other.dateTime, this.dateTime);
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getDate() {
        return dateTime;
    }

    public void setDate(long dateTime) {
        this.dateTime = dateTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
