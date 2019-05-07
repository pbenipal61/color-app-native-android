package com.postmaninteractive.colorapp.Utils;

import android.content.Context;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitHelper {

    private static String baseUrl = "https://cld8zbgxoc.execute-api.eu-central-1.amazonaws.com/api/v1/";    // Base URL for the api
    /**
     * Creats a Retrofit object
     * @param context Context from where the function is called
     * @return  Retrofit object
     */
    public static Retrofit generate(Context context){
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

    }

}
