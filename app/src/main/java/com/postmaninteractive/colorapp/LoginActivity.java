package com.postmaninteractive.colorapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.hussainderry.securepreferences.SecurePreferences;
import com.github.hussainderry.securepreferences.model.SecurityConfig;
import com.postmaninteractive.colorapp.Models.LoginData;
import com.postmaninteractive.colorapp.Models.StorageData;
import com.postmaninteractive.colorapp.Utils.Api;
import com.postmaninteractive.colorapp.Utils.RetrofitHelper;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.provider.Telephony.Carriers.PASSWORD;
import static android.provider.Telephony.Mms.Part.FILENAME;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private SecurePreferences.Editor editor;
    private Api api;
    private TextView tvUpdate;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tvUpdate = findViewById(R.id.tvUpdate);
        progressBar = findViewById(R.id.progressBar);

        Retrofit retrofit = RetrofitHelper.generate(this);
        api = retrofit.create(Api.class);

        initProcess();


    }

    private void initProcess(){



        SecurityConfig minimumConfig = new SecurityConfig.Builder(PASSWORD).build();

        SecurePreferences securePreferences = SecurePreferences.getInstance(this, FILENAME, minimumConfig);
        editor = securePreferences.edit();

        String token = securePreferences.getString("apiToken", "default");
        int id = securePreferences.getInt("id", -1);

        if (token.equals("default") || id == -1) {

            Log.d(TAG, "onCreate: api token realised " + token);
            setProgressUpdateText("Logging in...");
            login();

        } else {

            Log.d(TAG, "onCreate: token found " + token);

            loadData(token, id);

        }

    }
    private void setProgressUpdateText(String updateText){
        tvUpdate.setText(updateText);
    }
    private void failedToConnect(){
        progressBar.setVisibility(View.GONE);
        final Button retryButton = findViewById(R.id.retryButton);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.GONE);

                initProcess();
            }
        });
        retryButton.setVisibility(View.VISIBLE);
        setProgressUpdateText("Failed to connect to the server!.\n Please check your internet");
    }


    private void moveToMainActivity(String lastSavedColor) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("lastSavedColor", lastSavedColor);
        startActivity(intent);
    }

    private void login() {

        Log.d(TAG, "login: Loggin in");
        LoginData loginData = new LoginData("fstest08", "mqG36jUr");


        Call<LoginData.LoginResponse> loginCall = api.login(loginData);
        loginCall.enqueue(new Callback<LoginData.LoginResponse>() {
            @Override
            public void onResponse(Call<LoginData.LoginResponse> call, final Response<LoginData.LoginResponse> response) {
                try {
                    if (response.body() != null) {
                        LoginData.LoginResponse loginResponse = response.body();
                        Log.d(TAG, "onResponse: login token fetched " + loginResponse.getToken());
                        editor.putString("apiToken", loginResponse.getToken()).apply();
                        getStorageId(loginResponse.getToken());


                    } else {

                        Log.d(TAG, "onResponse: Response body was null" + response);
                        setProgressUpdateText("Failed to login. Trying again...");
                        login();
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }

            @Override
            public void onFailure(Call<LoginData.LoginResponse> call, Throwable t) {

                Log.d(TAG, "onFailure: Failed to login ");
                failedToConnect();
                t.printStackTrace();
            }
        });
    }

    private void getStorageId(final String token){

        setProgressUpdateText("Getting storage id...");

        Map<String, String> storageIdCallBody = new HashMap<String, String>();
        storageIdCallBody.put("data", "#ffffff");
        Call<StorageData.StorageDataResponse> getStorageIdCall = api.getStorageId(token, storageIdCallBody);
        getStorageIdCall.enqueue(new Callback<StorageData.StorageDataResponse>() {
            @Override
            public void onResponse(Call<StorageData.StorageDataResponse> call2, Response<StorageData.StorageDataResponse> response2) {

                if (response2.code() == 200) {
                    if (response2.body() != null) {

                        StorageData.StorageDataResponse storageDataResponse = response2.body();
                        Log.d(TAG, "onResponse: storage id is " + storageDataResponse.getId());
                        editor.putInt("id", storageDataResponse.getId()).apply();
                        setProgressUpdateText("Loading color pallette!");
                        moveToMainActivity(storageDataResponse.getData());

                    } else {
                        Log.d(TAG, "onResponse: getting storage id response was null " + response2);
                        setProgressUpdateText("Failed to get storage id.Trying again...");
                        getStorageId(token);
                    }
                } else {

                    Log.d(TAG, "onResponse: Request to get storage id failed " + response2);
                    setProgressUpdateText("Something went wrong while getting storage id.\nTrying again...");
                    getStorageId(token);
                }
            }

            @Override
            public void onFailure(Call<StorageData.StorageDataResponse> call2, Throwable t2) {
                Log.d(TAG, "onFailure: Failed to get storage id");
                failedToConnect();
                t2.printStackTrace();
            }
        });
    }

    private void loadData(final String token, final int id) {

        setProgressUpdateText("Loading data...");
        Log.d(TAG, "loadData: " + id + " " + token);
        Call<StorageData.StorageDataResponse> storageDataResponseCall = api.getData(token, id);
        storageDataResponseCall.enqueue(new Callback<StorageData.StorageDataResponse>() {
            @Override
            public void onResponse(Call<StorageData.StorageDataResponse> call, Response<StorageData.StorageDataResponse> response) {
                if (response.body() != null) {
                    Log.d(TAG, "onResponse: color " + response.body().getData());
                    StorageData.StorageDataResponse storageDataResponse = response.body();
                    setProgressUpdateText("Loading color pallette!");
                    moveToMainActivity(storageDataResponse.getData());
                } else {
                    Log.d(TAG, "onResponse: Loading stored colours response was null");
                    setProgressUpdateText("Something went wrong while loading previous data.\nTrying again...");
                    loadData(token, id);
                }
            }

            @Override
            public void onFailure(Call<StorageData.StorageDataResponse> call, Throwable t) {

                Log.d(TAG, "onFailure: Failed to load saved color");
                failedToConnect();
                t.printStackTrace();
            }
        });


    }
}
