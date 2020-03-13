package com.postmaninteractive.colorapp.Utils;

import org.junit.After;
import org.junit.Before;

import java.io.IOException;

import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;


class ApiTest {

    MockWebServer mockWebServer = new MockWebServer();
    Retrofit retrofit = RetrofitHelper.generate();


    @Before
    void setUp(){
        try {
            mockWebServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    void tearDown(){
        try {
            mockWebServer.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    

}