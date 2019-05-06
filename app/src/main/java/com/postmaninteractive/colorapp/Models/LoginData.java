package com.postmaninteractive.colorapp.Models;

public class LoginData {

    private final String username, password;

    public LoginData(String username, String password) {
        this.username = username;
        this.password = password;
    }
     public class LoginResponse{
        private String token;

        public LoginResponse(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }
}
