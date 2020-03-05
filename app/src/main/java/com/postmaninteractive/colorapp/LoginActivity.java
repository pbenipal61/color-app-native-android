package com.postmaninteractive.colorapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.hussainderry.securepreferences.SecurePreferences;
import com.github.hussainderry.securepreferences.model.SecurityConfig;
import com.postmaninteractive.colorapp.Models.LoginData;
import com.postmaninteractive.colorapp.Models.StorageData;
import com.postmaninteractive.colorapp.Utils.Api;
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

public class LoginActivity extends AppCompatActivity {

//    private static final String TAG = "LoginActivity";      // Tag for logs
    private final int numberOfTriesForConnectionCheck = 15; // Number of times app should try to make a connection with the user before notifying the user
    private SecurePreferences securePreferences;            // Secure preferences
    private SecurePreferences.Editor editor;                // Editor for secure preferences
    private Api api;                                        // Reference to api interface
    private TextView tvUpdate;                              // Update TextView reference
    private ProgressBar progressBar;                        // ProgressBar reference
    private Button loginButton;                             // Login button reference
    private RelativeLayout rlMainLayout;                    // Main layout reference
    private LinearLayout upLayout;                          // Username password fields in a linear layout
    private EditText etvUsername;                           // Username EditText
    private EditText etvPassword;                           // Password EditText
    private final String KEY_TOKEN = "apiToken";            // Api token key for secure preferences
    private final String KEY_ID = "id";                     // id key for secure preferences
    private int triesToLoadStorageData = 0;                 // Number of times app has tried to load storage data
    private int triesToLogin = 0;                           // Number of times the app tried to login
    private int triesToGetStorageId = 0;                    // Number of times the app tried to get a storage id for the user
    private AlertDialog alertDialog;                        // AlertDialog to ask user if they want continue without saving data
    private Boolean askedUserForSaveMode = false;           // Track of if the user has been offered without save mode. The user if offered this mode only once
    private Snackbar badInternetConnectionSnackBar;         // Bad internet connection SnackBar
    private Snackbar serverConnectionFailedSnackBar;        // Reference to server connection failed SnackBar
    private Snackbar incorrectCredentialsSnackBar;          // Incorrect credentials on login response SnackBar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        rlMainLayout = findViewById(R.id.rlMainLayout);
        tvUpdate = findViewById(R.id.tvUpdate);
        progressBar = findViewById(R.id.progressBar);

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
        securePreferences = SecurePreferences.getInstance(this, FILENAME, minimumConfig);
        editor = securePreferences.edit();

        processController();
    }

    /**
     * Controls various steps of the process
     */
    private void processController(){

        String token = securePreferences.getString(KEY_TOKEN, "default");
        int id = securePreferences.getInt(KEY_ID, -1);


        //  Uncomment the following block to hardcode the storage id
//                id = 211;
//                editor.putInt("id", 211).apply();

        if (token.equals("default")) {
            progressBar.setVisibility(View.GONE);
            // Show the login form
            upLayout  =findViewById(R.id.upLayout);
            upLayout.setVisibility(View.VISIBLE);
            // Asking to user to log in
            if(loginButton == null) {
                loginButton = findViewById(R.id.loginButton);
            }
            loginButton.setVisibility(View.VISIBLE);
            String loginMessage = "Login to continue";
            final Snackbar loginSnackBar = SnackBarHelper.generate(rlMainLayout, loginMessage, Snackbar.LENGTH_INDEFINITE);
            loginSnackBar.show();
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loginSnackBar.dismiss();
                    upLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    tvUpdate.setVisibility(View.VISIBLE);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(loginButton.getWindowToken(),0);
                    login();
                }
            });


        } else if (id == -1) {

            // If token already exists and id doesn't then a storage id is fetched
            progressBar.setVisibility(View.VISIBLE);
            getStorageId(token);


        } else {

            // If both id and token exists then the stored data is loaded from the storage on the server
            progressBar.setVisibility(View.VISIBLE);
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

        // Login data is structured
        if(etvPassword == null ) {
            etvPassword = findViewById(R.id.etvPassword);
        }
        if(etvUsername == null) {
            etvUsername = findViewById(R.id.etvUsername);
        }

        LoginData loginData = new LoginData(etvUsername.getText().toString(), etvPassword.getText().toString());

        setProgressUpdateText("Logging in ...");

        // The login call to the server
        Call<LoginData.LoginResponse> loginCall = api.login(loginData);
        loginCall.enqueue(new Callback<LoginData.LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginData.LoginResponse> call, @NonNull final Response<LoginData.LoginResponse> response) {

                if (response.body() != null) {

                        // If appropriate response is noticed then received token is saved to the secure preferences
                        showFailedServerConnectionSnackBar(false);
                        LoginData.LoginResponse loginResponse = response.body();
                        editor.putString(KEY_TOKEN, loginResponse.getToken()).apply();

                        // Check with process controller
                        processController();

                } else {

                    // Check if login failed and take necessary steps
                    if(response.code() >= 400 && response.code() < 500){

                        upLayout.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        tvUpdate.setVisibility(View.GONE);

                        if(incorrectCredentialsSnackBar == null){
                            incorrectCredentialsSnackBar = SnackBarHelper.generate(rlMainLayout, "Incorrect credentials!");
                        }
                        incorrectCredentialsSnackBar.show();

                        return;
                    }
                    // If appropriate response isn't noticed then number of tries for login is checked
                    // and user is notified about the situation
                    if (triesToLogin == numberOfTriesForConnectionCheck) {
                        if (!askedUserForSaveMode) {
                            showWithoutSaveModeAlert("default", -1, ProcessParts.LOGIN);
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
                failedInternetConnection();
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

        // Default data to be stored into the soon to be created storage
        Map<String, String> storageIdCallBody = new HashMap<>();
        storageIdCallBody.put("data", "#ffffff");

        // Creating storage and getting id, call
        Call<StorageData.StorageDataResponse> getStorageIdCall = api.getStorageId(token, storageIdCallBody);
        getStorageIdCall.enqueue(new Callback<StorageData.StorageDataResponse>() {
            @Override
            public void onResponse(@NonNull Call<StorageData.StorageDataResponse> call, @NonNull Response<StorageData.StorageDataResponse> response) {


                if (response.body() != null) {

                    // If appropriate response is found then id is saved
                    // If server connection SnackBar exists on the screen then its dismissed
                    showFailedServerConnectionSnackBar(false);
                    StorageData.StorageDataResponse storageDataResponse = response.body();
                    editor.putInt(KEY_ID, storageDataResponse.getId()).apply();
                    moveToMainActivity(storageDataResponse.getData(), true);
                } else {

                    // If appropriate response is not found then user is notified accordingly.
                    // Another attempt to get id is made with appropriate process
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
            public void onFailure(@NonNull Call<StorageData.StorageDataResponse> call2, @NonNull Throwable t2) {

                // If something goes wrong or the app fails to connect to internet then the user is notified
                failedInternetConnection();
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
            public void onResponse(@NonNull Call<StorageData.StorageDataResponse> call, @NonNull Response<StorageData.StorageDataResponse> response) {
                if (response.body() != null) {

                    // If appropriate response is found then the stored data is passed onto MainActivity as a color string
                    showFailedServerConnectionSnackBar(false);
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
            public void onFailure(@NonNull Call<StorageData.StorageDataResponse> call, @NonNull Throwable t) {

                // If something goes wrong and the app fails to connect to internet then the user is notified
                failedInternetConnection();
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
    private void failedInternetConnection() {

        // ProgressBar is set to gone
        progressBar.setVisibility(View.GONE);

        // Retry button
        final Button retryButton = findViewById(R.id.retryButton);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.GONE);
                badInternetConnectionSnackBar.dismiss();
                initProcess();
            }
        });

        // User is notified by the retry button and the SnackBar this time and update TextView is set to an empty string
        setProgressUpdateText("");
        retryButton.setVisibility(View.VISIBLE);
        if (badInternetConnectionSnackBar == null) {
            badInternetConnectionSnackBar = SnackBarHelper.generate(rlMainLayout, "Please check your internet connection.", Snackbar.LENGTH_INDEFINITE);
        }
        badInternetConnectionSnackBar.show();


    }

    /**
     * Utility function to load MainActivity
     *
     * @param lastSavedColor Color loaded from storage
     * @param saveMode       Weather the user is accessing the MainActivity with save mode
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
     * @param token Token passed . Default is "default" or null
     * @param id    id passed . Cannot be null but can be -1 as equivalent of null
     * @param part  Part from where this dialog is called
     */
    private void showWithoutSaveModeAlert(final String token, final int id, final ProcessParts part) {

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

        // askedUserForSaveMode is set to true
        askedUserForSaveMode = true;

    }

    /**
     * Parts of the entire logging process
     */
    private enum ProcessParts {
        LOGIN, GET_STORAGE_ID, LOAD_DATA
    }
}
