package com.postmaninteractive.colorapp.Models;

public class LoginData {

    private final String username;  // Username provided
    private final String password;  // Password provided

    /**
     * Created a LoginData object
     * @param username Reference to username to be stored in this object
     * @param password  Reference to password to be stored in this object
     */
    public LoginData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Login Response class
     */
     public class LoginResponse{
        private final String token;

        /**
         * Created a LoginResponse Object
         * @param token Token
         */
        public LoginResponse(String token) {
            this.token = token;
        }

        /**
         * Get the token stored
         * @return Token
         */
        public String getToken() {
            return token;
        }
    }
}
