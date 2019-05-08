package com.postmaninteractive.colorapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
    private final int numberOfTriesForConnectionCheck = 15;       // Number of times app should try to make a connection with the user before notifying the user
    private SecurePreferences.Editor editor;                // Editor for secure preferences
    private Api api;                                        // Reference to api interface
    private TextView tvUpdate;                              // Update TextView reference
    private ProgressBar progressBar;                        // ProgressBar reference
    private Button loginButton;                             // Login button reference
    private RelativeLayout rlMainLayout;                    // Main layout reference
    private Snackbar serverConnectionFailedSnackBar = null; // Reference to server connection failed SnackBar
    private int triesToLoadStorageData = 0;                 // Number of times app has tried to load storage data
    private int triesToLogin = 0;                           // Number of times the app tried to login
    private int triesToGetStorageId = 0;                    // Number of times the app tried to get a storage id for the user
    private AlertDialog alertDialog;                        // AlertDialog to ask user if they want continue without saving data
    private Boolean askedUserForSaveMode = false;           // Track of if the user has been offered without save mode. The user if offered this mode only once

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        rlMainLayout = findViewById(R.id.rlMainLayout);
        tvUpdate = findViewById(R.id.tvUpdate);
        progressBar = findViewById(R.id.progressBar);
        loginButton = findViewById(R.id.loginButton);

        Retrofit retrofit = RetrofitHelper.generate();
        api = retrofit.create(Api.class);

        initProcess();
    }

    /**
     * Initialises the entire process of logging in and loading data
     */
    private void initProcess() {

        // Builds the security config for secure preferences
        SecurityConfig minimumConfig = new SecurityConfig.Builder(PASSWORD).build();

        // Creates secure preferences based on provided config if needed otherwise uses one if already exists
        SecurePreferences securePreferences = SecurePreferences.getInstance(this, FILENAME, minimumConfig);
        editor = securePreferences.edit();

        String token = securePreferences.getString("apiToken", "default");
        int id = securePreferences.getInt("id", -1);

        if (token.equals("default") && id == -1) {
            progressBar.setVisibility(GONE);

            // Resize the Login button based on the screen dimensions
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) loginButton.getLayoutParams();
            params.height = ResizeHelper.getScreenDimensions(this)[1] / 10;
            params.width = ResizeHelper.getScreenDimensions(this)[0] / 2;
            loginButton.setLayoutParams(params);

            loginButton.setVisibility(View.VISIBLE);
            // Message to notify user about logging in
            String loginMessage = "Login to continue";
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

            // If token already exists and id doesn't then a storage id is fetched
            getStorageId(token);

        } else {

            // If both id and token exists then the stored data is loaded from the storage
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

        //Login process

        // Increments number of times login has been tried
        triesToLogin++;

        LoginData loginData = new LoginData("fstest08", "mqG36jUr");
        setProgressUpdateText("Logging in ...");

        // The login call to the server
        Call<LoginData.LoginResponse> loginCall = api.login(loginData);
        loginCall.enqueue(new Callback<LoginData.LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginData.LoginResponse> call, @NonNull final Response<LoginData.LoginResponse> response) {
                Log.d(TAG, "onResponse: "+ response.body());
                if (response.body() != null) {
                    // If appropriate response is noticed then received token is saved to the secure preferences
                    showFailedServerConnectionSnackBar(false);
                    LoginData.LoginResponse loginResponse = response.body();
                    Log.d(TAG, "onResponse: api token is " + loginResponse.getToken());
                    editor.putString("apiToken", loginResponse.getToken()).apply();

                    // This token is passed onto getStorageId function to get a new storage id
                    getStorageId(loginResponse.getToken());

                } else {

                    // If appropriate response isn't noticed then number of tries for login is checked
                    // and user is notified about the situation
                    if (triesToLogin == numberOfTriesForConnectionCheck) {
                        if (!askedUserForSaveMode) {
                            showWithoutSaveModeAlert(null, -1, ProcessParts.LOGIN);
                        } else {
                            // Another attempt to login is made if the login fails
                            login();
                        }
                        showFailedServerConnectionSnackBar(true);
                    } else {
                        login();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginData.LoginResponse> call, @NonNull Throwable t) {

                // If something goes wrong or the app fails to connect to internet then the user is notified
                failedToConnect();
                t.printStackTrace();
            }
        });
    }

    /**
     * Gets a storage id and stores base color (White) onto the server
     *
     * @param token Token to use for Authorization header
     */
    private void getStorageId(final String token) {

        // User is notified about this
        //Message on trying to load a storage id
        String loadingStorageIdMessage = "Getting a storage ...";
        setProgressUpdateText(loadingStorageIdMessage);

        // Number of tries of getting storage id is incremented
        triesToGetStorageId++;
        Log.d(TAG, "getStorageId: Getting storage try " + triesToGetStorageId);

        // Default data to be stored into the soon to be created storage
        Map<String, String> storageIdCallBody = new HashMap<>();
        storageIdCallBody.put("data", "#ffffff");

        // Creating storage and getting id, call
        Call<StorageData.StorageDataResponse> getStorageIdCall = api.getStorageId(token, storageIdCallBody);
        getStorageIdCall.enqueue(new Callback<StorageData.StorageDataResponse>() {
            @Override
            public void onResponse(@NonNull  Call<StorageData.StorageDataResponse> call,@NonNull Response<StorageData.StorageDataResponse> response) {


                    if (response.body() != null) {

                        // If appropriate response is found then id is saved
                        showFailedServerConnectionSnackBar(false);
                        StorageData.StorageDataResponse storageDataResponse = response.body();
                        editor.putInt("id", storageDataResponse.getId()).apply();
                        moveToMainActivity(storageDataResponse.getData(), true);
                    } else {

                        // If appropriate response is not found then user is notified accordingly.
                        // Another attempt to get id is made
                        Log.d(TAG, "onResponse: Request to get storage id failed " + response);
                        if (triesToGetStorageId == numberOfTriesForConnectionCheck) {
                            if (!askedUserForSaveMode) {
                                showWithoutSaveModeAlert(token, -1, ProcessParts.GET_STORAGE_ID);
                            } else {

                                // Another attempt to get id
                                getStorageId(token);
                            }
                            showFailedServerConnectionSnackBar(true);
                        } else {
                            getStorageId(token);
                        }
                    }

            }

            @Override
            public void onFailure(@NonNull Call<StorageData.StorageDataResponse> call2,@NonNull Throwable t2) {

                // If something goes wrong or the app fails to connect to internet then the user is notified
                Log.d(TAG, "onFailure: Failed to get storage id");
                failedToConnect();
                t2.printStackTrace();
            }
        });
    }

    /**
     * Loads data for provided id
     *
     * @param token Token to use for Authorization header
     * @param id    Id of the storage
     */
    private void loadData(final String token, final int id) {
        // Message on trying to load data from the server
        String loadingDataMessage = "Loading last color ...";
        setProgressUpdateText(loadingDataMessage);

        // Number of tries of loading stored data in storage is incremented
        triesToLoadStorageData++;

        // Load stored data from storage, call
        Call<StorageData.StorageDataResponse> storageDataResponseCall = api.getData(token, id);
        storageDataResponseCall.enqueue(new Callback<StorageData.StorageDataResponse>() {
            @Override
            public void onResponse(@NonNull Call<StorageData.StorageDataResponse> call,@NonNull Response<StorageData.StorageDataResponse> response) {
                if (response.body() != null) {

                    // If appropriate response is found then the stored data is passed onto MainActivity as a color string
                    showFailedServerConnectionSnackBar(false);
                    Log.d(TAG, "onResponse: color " + response.body().getData());
                    StorageData.StorageDataResponse storageDataResponse = response.body();
                    moveToMainActivity(storageDataResponse.getData(), true);
                } else {

                    // If appropriate response is not found then the user is notified accordingly.
                    if (triesToLoadStorageData == numberOfTriesForConnectionCheck) {
                        if (!askedUserForSaveMode) {
                            showWithoutSaveModeAlert(token, id, ProcessParts.LOAD_DATA);
                        } else {

                            // Another attempt is made to load said data
                            loadData(token, id);
                        }
                        showFailedServerConnectionSnackBar(true);
                    } else {
                        loadData(token, id);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<StorageData.StorageDataResponse> call,@NonNull Throwable t) {

                // If something goes wrong and the app fails to connect to internet then the user is notified
                failedToConnect();
                t.printStackTrace();
            }
        });
    }

    /**
     * Utility function to update the text on the screen and keep the user updated about the process
     *
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

        // ProgressBar is set to gone
        progressBar.setVisibility(GONE);

        // Retry button is resized according to the dimensions of the screen
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
        setProgressUpdateText("");
        retryButton.setVisibility(View.VISIBLE);
        showFailedServerConnectionSnackBar(false);

        // User is notified by the retry button and the SnackBar this time and update TextView is set to an empty string
        setProgressUpdateText("");
        showFailedServerConnectionSnackBar(true);
    }

    /**
     * Utility function to load MainActivity
     *
     * @param lastSavedColor Color loaded from storage
     */
    private void moveToMainActivity(String lastSavedColor, Boolean saveMode) {

        // User is notified
        // Message when MainActivity is being loaded
        String colorPaletteMessage = "Loading color palette!";
        setProgressUpdateText(colorPaletteMessage);

        // MainActivity is called
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        if (lastSavedColor != null) {
            intent.putExtra("lastSavedColor", lastSavedColor);
        }
        intent.putExtra("saveMode", saveMode);
        startActivity(intent);
        finish();
    }

    /**
     * Utility function to show a SnackBar when server connection fails
     *
     * @param show If to show the bar or dismiss it
     */
    private void showFailedServerConnectionSnackBar(Boolean show) {

        // SnackBar is made if it doesn't already exist
        if (serverConnectionFailedSnackBar == null) {
            String message = "Server appears to be down! Trying to connect ...";
            serverConnectionFailedSnackBar = SnackBarHelper.generate(rlMainLayout, message, Snackbar.LENGTH_INDEFINITE);
        }

        // SnackBar is shown or dismissed according to passed parameter toShow
        if (show) {
            serverConnectionFailedSnackBar.show();
        } else {
            serverConnectionFailedSnackBar.dismiss();
        }
    }

    /**
     * Utility method for creating alert to ask user if they want to continue without saving their data
     *
     * @param token Token passed . Can be null
     * @param id    id passed . Cannot be null but can be -1 as equivalent of null
     * @param part  Part from where this dialog is called
     */
    private void showWithoutSaveModeAlert(final String token, final int id, final ProcessParts part) {
        askedUserForSaveMode = true;

        // Builds the necessary dialog if needed
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setTitle("Server appears to be down. Continue without saving any changes ?");


            builder.setPositiveButton("Yes. Please", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    moveToMainActivity(null, false);
                }
            });

            builder.setNegativeButton("Nope. I'll wait", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    switch (part) {
                        case LOGIN:
                            login();
                            break;

                        case GET_STORAGE_ID:
                            getStorageId(token);
                            break;
                        case LOAD_DATA:
                            loadData(token, id);
                            break;
                    }
                }
            });
            alertDialog = builder.create();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
        }

        // Alert Dialog is shown
        alertDialog.show();

    }

    private enum ProcessParts {                             // Parts of the entire logging process
        LOGIN, GET_STORAGE_ID, LOAD_DATA
    }
}
