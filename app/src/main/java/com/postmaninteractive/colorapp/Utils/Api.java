package com.postmaninteractive.colorapp.Utils;

import com.postmaninteractive.colorapp.Models.LoginData;
import com.postmaninteractive.colorapp.Models.StorageData;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface Api {


    @POST("login")
    Call<LoginData.LoginResponse> login(@Body LoginData loginData);


    @POST("storage")
    Call<StorageData.StorageDataResponse> getStorageId(@Header ("Authorization") String token, @Body Map<String, String> body);

    @Headers({"Content-Type: application/json"})
    @PUT("storage/{id}")
    Call<StorageData.StorageDataResponse> storeData(@Header ("Authorization") String token,@Path("id") int id, @Body StorageData storageData);

    @GET("storage/{id}")
    Call<StorageData.StorageDataResponse> getData(@Header ("Authorization") String token, @Path("id") int id);

    @DELETE("storage/{id}")
    Call<String> deleteStorage(@Path("id") int id);
}
