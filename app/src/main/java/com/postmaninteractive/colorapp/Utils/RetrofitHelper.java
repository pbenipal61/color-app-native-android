package com.postmaninteractive.colorapp.Utils;

import android.content.Context;

import com.postmaninteractive.colorapp.R;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitHelper {

    public static Retrofit generate(Context context){

        String baseUrl = context.getString(R.string.api_base_url);
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

    }

}
