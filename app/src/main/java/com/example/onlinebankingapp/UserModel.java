package com.example.onlinebankingapp;

import com.google.firebase.Timestamp;

public class UserModel {
    private String countryCode;
    private String phoneNumber;
    private String fullName;
    private String email;
    private Timestamp joinedAt;
    public UserModel(){

    }
    public UserModel(String countryCode, String phoneNumber, String fullName, String email, Timestamp joinedAt) {
        this.countryCode = countryCode;
        this.phoneNumber = phoneNumber;
        this.fullName = fullName;
        this.email = email;
        this.joinedAt = joinedAt;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }
}
