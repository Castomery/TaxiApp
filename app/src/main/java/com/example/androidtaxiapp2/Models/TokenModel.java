package com.example.androidtaxiapp2.Models;

public class TokenModel {
    private String token;

    public TokenModel(){}

    public TokenModel(String token){
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
