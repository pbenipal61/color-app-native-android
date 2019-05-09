package com.postmaninteractive.colorapp.Utils;


import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitHelper {

    private static final String baseUrl = "https://cld8zbgxoc.execute-api.eu-central-1.amazonaws.com/api/v1/";    // Base URL for the api
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
