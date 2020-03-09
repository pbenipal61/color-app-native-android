package com.postmaninteractive.colorapp.Utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;

import static org.junit.jupiter.api.Assertions.*;

class ApiTest {

    private MockWebServer mockWebServer = new MockWebServer();
    private Retrofit api;
    @BeforeEach
    void setUp() {

    }



    @AfterEach
    void tearDown() {
    }
}