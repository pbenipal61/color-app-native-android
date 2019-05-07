package com.postmaninteractive.colorapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.hussainderry.securepreferences.SecurePreferences;
import com.github.hussainderry.securepreferences.model.SecurityConfig;
import com.postmaninteractive.colorapp.Models.LoginData;
import com.postmaninteractive.colorapp.Models.StorageData;
import com.postmaninteractive.colorapp.Utils.Api;
import com.postmaninteractive.colorapp.Utils.ResizeHelper;
import com.postmaninteractive.colorapp.Utils.RetrofitHelper;
import com.postmaninteractive.colorapp.Utils.SnackBarHelper;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.provider.Telephony.Carriers.PASSWORD;
import static android.provider.Telephony.Mms.Part.FILENAME;
import static android.view.View.GONE;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";      // Tag for logs
    private SecurePreferences.Editor editor;                // Editor for secure preferences
    private Api api;                                        // Reference to api interface
    private TextView tvUpdate;                              // Update textview reference
    private ProgressBar progressBar;                        // ProgressBar reference
    private Button loginButton;                             // Login button reference
    private RelativeLayout rlMainLayout;                    // Main layout reference
    private int numberOfTriesForConnectionCheck = 10;       // Number of times app should try to make a connection with the user before notifying the user
    private Snackbar serverConnectionFailedSnackbar = null; // Reference to server connection failed snackbar
    private int triesToLoadStorageData = 0;                 // Number of times app has tried to load storage data
    private int triesToLogin = 0;                           // Number of times the app tried to login
    private int triesToGetStorageId = 0;                    // Number of times the app tried to get a storage id for the user
    private String loginMessage = "Login to continue";      // Message to notify user about logging in
     private String colorPaletteMessage ="Loading color palette!";   // Message when MainActivity is being loaded
    private String loadingDataMessage = "Loading data...";  // Message on trying to load data from the server
    private String loadingStorageIdMessage="Getting storage id..."; //Message on trying to load a storage id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        rlMainLayout = findViewById(R.id.rlMainLayout);
        tvUpdate = findViewById(R.id.tvUpdate);
        progressBar = findViewById(R.id.progressBar);
        loginButton = findViewById(R.id.loginButton);

        Retrofit retrofit = RetrofitHelper.generate(this);
        api = retrofit.create(Api.class);


        initProcess();

    }

    /**
     * Initialises the entire process of logging in and loading data
     */
    private void initProcess() {


        SecurityConfig minimumConfig = new SecurityConfig.Builder(PASSWORD).build();
        SecurePreferences securePreferences = SecurePreferences.getInstance(this, FILENAME, minimumConfig);
        editor = securePreferences.edit();

        String token = securePreferences.getString("apiToken", "default");
        int id = securePreferences.getInt("id", -1);

        if (token.equals("default") && id == -1) {

            progressBar.setVisibility(GONE);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) loginButton.getLayoutParams();
            params.height = ResizeHelper.getScreenDimensions(this)[1] / 10;
            params.width = ResizeHelper.getScreenDimensions(this)[0] / 2;
            loginButton.setLayoutParams(params);


            loginButton.setVisibility(View.VISIBLE);
            final Snackbar loginSnackBar = SnackBarHelper.generate(rlMainLayout, loginMessage, Snackbar.LENGTH_INDEFINITE);
            loginSnackBar.show();
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loginSnackBar.dismiss();
                    loginButton.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);


                    login();
                }
            });


        } else if (id == -1) {


            getStorageId(token);

        } else {

            progressBar.setVisibility(View.VISIBLE);
            Log.d(TAG, "onCreate: token found " + token);
            loginButton.setVisibility(View.GONE);
            loadData(token, id);

        }

    }




    /**
     * Logs the user in and proceeds to loading storage data
     */
    private void login() {

        triesToLogin++;
        Log.d(TAG, "login: Loggin in");
        LoginData loginData = new LoginData("fstest08", "mqG36jUr");
        setProgressUpdateText("Logging in...");

        Call<LoginData.LoginResponse> loginCall = api.login(loginData);
        loginCall.enqueue(new Callback<LoginData.LoginResponse>() {
            @Override
            public void onResponse(Call<LoginData.LoginResponse> call, final Response<LoginData.LoginResponse> response) {
                try {
                    if (response.body() != null) {

                        showFailedServerConnectionSnackbar(false);
                        LoginData.LoginResponse loginResponse = response.body();
                        Log.d(TAG, "onResponse: api token is " +  loginResponse.getToken());
                        editor.putString("apiToken", loginResponse.getToken()).apply();
                        getStorageId(loginResponse.getToken());

                    } else {


                        if (triesToLogin == numberOfTriesForConnectionCheck) {
                            triesToLogin = 0;

                            showFailedServerConnectionSnackbar(true);
                        }
                        login();
                    }

                } catch (Exception e) {

                    failedToConnect();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<LoginData.LoginResponse> call, Throwable t) {
                failedToConnect();
                t.printStackTrace();
            }
        });
    }



    /**
     * Gets a storage id and stores base color (White) onto the server
     * @param token Token to use for Authorization header
     */
    private void getStorageId(final String token) {

        triesToGetStorageId++;

        setProgressUpdateText(loadingStorageIdMessage);

        Map<String, String> storageIdCallBody = new HashMap<>();
        storageIdCallBody.put("data", "#ffffff");
        Call<StorageData.StorageDataResponse> getStorageIdCall = api.getStorageId(token, storageIdCallBody);
        getStorageIdCall.enqueue(new Callback<StorageData.StorageDataResponse>() {
            @Override
            public void onResponse(Call<StorageData.StorageDataResponse> call2, Response<StorageData.StorageDataResponse> response2) {


                    if (response2.body() != null) {
                        showFailedServerConnectionSnackbar(false);
                        StorageData.StorageDataResponse storageDataResponse = response2.body();

                        editor.putInt("id", storageDataResponse.getId()).apply();
                        setProgressUpdateText(colorPaletteMessage);
                        moveToMainActivity(storageDataResponse.getData());

                    } else {


                        Log.d(TAG, "onResponse: Request to get storage id failed " + response2);
//                    setProgressUpdateText("Something went wrong while getting storage id.\nTrying again...");
                        getStorageId(token);

                        if (triesToGetStorageId == numberOfTriesForConnectionCheck) {
                            triesToGetStorageId = 0;
                            showFailedServerConnectionSnackbar(true);
                        }


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



    /**
     * Loads data for provided id
     * @param token Token to use for Authorization header
     * @param id Id of the storage
     */
    private void loadData(final String token, final int id) {

        triesToLoadStorageData++;
        setProgressUpdateText(loadingDataMessage);
        Log.d(TAG, "loadData: " + id + " " + token);
        Call<StorageData.StorageDataResponse> storageDataResponseCall = api.getData(token, id);
        storageDataResponseCall.enqueue(new Callback<StorageData.StorageDataResponse>() {
            @Override
            public void onResponse(Call<StorageData.StorageDataResponse> call, Response<StorageData.StorageDataResponse> response) {
                try {
                    if (response.body() != null) {

                        showFailedServerConnectionSnackbar(false);
                        Log.d(TAG, "onResponse: color " + response.body().getData());
                        StorageData.StorageDataResponse storageDataResponse = response.body();
                        setProgressUpdateText(colorPaletteMessage);
                        moveToMainActivity(storageDataResponse.getData());

                    } else {

                        Log.d(TAG, "onResponse: Loading stored colours response was null");
//                    setProgressUpdateText("Something went wrong while loading previous data. Trying again...");

                        if (triesToLoadStorageData == numberOfTriesForConnectionCheck) {
                            triesToLoadStorageData = 0;
                            showFailedServerConnectionSnackbar(true);
                        }

                        loadData(token, id);

                    }
                }
                catch(Exception e){
                    failedToConnect();
                    e.printStackTrace();
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

    /**
     * Utility function to update the text on the screen and keep the user updated about the process
     * @param updateText Message to be printed on the screen
     */
    private void setProgressUpdateText(String updateText) {

        tvUpdate.setText(updateText);
    }

    /**
     * Kicks in when the application fails to connect to the server.
     * Makes the retry button visible
     */
    private void failedToConnect() {
        progressBar.setVisibility(GONE);
        final Button retryButton = findViewById(R.id.retryButton);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) retryButton.getLayoutParams();

        int[] dims = ResizeHelper.getScreenDimensions(this);
        if (params.height != dims[1] / 10 || params.width != dims[0] / 2) {
            params.height = dims[1] / 10;
            params.width = dims[0] / 2;
            retryButton.setLayoutParams(params);
        }

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);
                retryButton.setVisibility(GONE);

                initProcess();
            }
        });
        retryButton.setVisibility(View.VISIBLE);
        setProgressUpdateText("");
        showFailedServerConnectionSnackbar(true);
    }


    /**
     * Utility function to load MainActivity
     * @param lastSavedColor Color loaded from storage
     */
    private void moveToMainActivity(String lastSavedColor) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("lastSavedColor", lastSavedColor);
        startActivity(intent);
    }




    /**
     * Utility function to show a snackbar when server connection fails
     * @param show If to show the bar or dismiss it
     */
    private void showFailedServerConnectionSnackbar(Boolean show) {

        if (serverConnectionFailedSnackbar == null) {
            String message = "Server appears to be down!";
            serverConnectionFailedSnackbar = SnackBarHelper.generate(rlMainLayout, message, Snackbar.LENGTH_INDEFINITE);
        }

        if(show){
            serverConnectionFailedSnackbar.show();
        }else{
            serverConnectionFailedSnackbar.dismiss();
        }


    }
}
