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


    /**
     * Logs in
     *
     * @param loginData LoginData object
     * @return LoginResponse object
     */
    @POST("login")
    Call<LoginData.LoginResponse> login(@Body LoginData loginData);


    /**
     * Gets a storage id
     *
     * @param token Token to be used in Authorization header
     * @param body  Body
     * @return Data stored in StorageDataResponse object
     */
    @POST("storage")
    Call<StorageData.StorageDataResponse> getStorageId(@Header("Authorization") String token, @Body Map<String, String> body);

    /**
     * Store data onto the server
     *
     * @param token       Token to be used in Authorization header
     * @param id          Id of the storage being referred to
     * @param storageData Data stored in StorageDataResponse object
     * @return Id as part of StorageDataResponse
     */
    @Headers({"Content-Type: application/json"})
    @PUT("storage/{id}")
    Call<StorageData.StorageDataResponse> storeData(@Header("Authorization") String token, @Path("id") int id, @Body StorageData storageData);

    /**
     * Get data from the storage
     *
     * @param token Token to be used in Authorization header
     * @param id    Id of the storage being referred to
     * @return Data stored in StorageDataResponse object
     */
    @GET("storage/{id}")
    Call<StorageData.StorageDataResponse> getData(@Header("Authorization") String token, @Path("id") int id);

    /**
     * Delete the storage
     *
     * @param token Token to be used in Authorization header
     * @param id    Id of the storage being referred to
     * @return A string that reads 'Ok' if delete successful
     */
    @DELETE("storage/{id}")
    Call<String> deleteStorage(@Header("Authorization") String token, @Path("id") int id);
}
