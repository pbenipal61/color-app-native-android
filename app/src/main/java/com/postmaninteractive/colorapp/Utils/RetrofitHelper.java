package com.postmaninteractive.colorapp.Utils;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitHelper {

    private static final String baseUrl = "https://54t9f06ot1.execute-api.eu-central-1.amazonaws.com/api/v1/";    // Base URL for the api
    /**
     * Creates a Retrofit object
     * @return  Retrofit object
     */
    public static Retrofit generate(){
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
