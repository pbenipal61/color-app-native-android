package com.postmaninteractive.colorapp.Models;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginDataTest {

    private String username = "username";
    private String password = "password";
    private LoginData loginData;
    @BeforeEach
    void setUp() {
        loginData = new LoginData(username, password);
    }

    /**
     * Test for username getter
     */
    @Test
    void testUsername(){
        Assert.assertEquals(username, loginData.getUsername());
    }

    /**
     * Test for password getter
     */
    @Test
    void testPassword(){
        Assert.assertEquals(password, loginData.getPassword());
    }


}