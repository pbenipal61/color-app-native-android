package com.postmaninteractive.colorapp;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.github.hussainderry.securepreferences.SecurePreferences;
import com.github.hussainderry.securepreferences.model.SecurityConfig;
import com.postmaninteractive.colorapp.Adapters.ColorItemsViewAdapter;
import com.postmaninteractive.colorapp.Models.ColorItem;
import com.postmaninteractive.colorapp.Models.StorageData;
import com.postmaninteractive.colorapp.Utils.Api;
import com.postmaninteractive.colorapp.Utils.RetrofitHelper;
import com.postmaninteractive.colorapp.Utils.SnackBarHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.provider.Telephony.Carriers.PASSWORD;
import static android.provider.Telephony.Mms.Part.FILENAME;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";           // Tag for log events
    private RelativeLayout rlMainLayout;                        // Reference to main layout
    private Api api;                                            // Reference to api interface
    private SecurePreferences securePreferences = null;         // Reference to secure preferences used to store data
    private Snackbar snackbar = null;                           // Reference to a SnackBar
    private ProgressBar progressBar;                            // Reference to the progress bar
    private Boolean saveMode;                                   // Check if running in save mode
    private String KEY_COLOR = "colorKey";                      // Used as key for saving to instance
    private String currentBGColorString;                        // Current background color string
    private String KEY_SAVE_MODE = "saveMode";                  // Save mode key reference



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (this.getActionBar() != null) {

            // ActionBar is customised
            ActionBar actionBar = this.getSupportActionBar();
            Objects.requireNonNull(actionBar).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.layout_task_bar);
            actionBar.setElevation(1);
            ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#93E9FA"));
            actionBar.setBackgroundDrawable(colorDrawable);
        }


        Retrofit retrofit = RetrofitHelper.generate();
        api = retrofit.create(Api.class);

        // ColorItems list is created
        List<ColorItem> colorItems = new ArrayList<>();
        colorItems.add(generateColorItem("#E91E63", "Pink"));
        colorItems.add(generateColorItem("#FFEB3B", "Yellow"));
        colorItems.add(generateColorItem("#795548", "Brown"));
        colorItems.add(generateColorItem("#009688", "Teal"));
        colorItems.add(generateColorItem("#3F51B5", "Indigo"));
        colorItems.add(generateColorItem("#78909c", "Blue Grey"));
        colorItems.add(generateColorItem("#ff5722", "Deep Orange"));
        colorItems.add(generateColorItem("#ffc107", "Amber"));
        colorItems.add(generateColorItem("#DEB887", "BurlyWood"));
        colorItems.add(generateColorItem("#FF7F50", "Coral"));
        colorItems.add(generateColorItem("#B8860B", "Dark Golden Rod"));
        colorItems.add(generateColorItem("#F0FFF0", "Honey Dew"));


        // Recycler view is populated using the adapter in a grid view
        RecyclerView rvColorItems = findViewById(R.id.rvColorItems);
        ColorItemsViewAdapter adapter = new ColorItemsViewAdapter(this, colorItems);
        int numberOfColumns = 3;
        rvColorItems.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        rvColorItems.setAdapter(adapter);

        rlMainLayout = findViewById(R.id.rlMainLayout);

        // LastSavedColor, which was passed from LoginActivity, is checked and if its not null then
        // it is parsed and set as background color of the main layout
        String colorString = getIntent().getStringExtra("lastSavedColor");
        if (colorString != null) {
            rlMainLayout.setBackgroundColor(Color.parseColor(colorString));
        }

        saveMode = getIntent().getBooleanExtra(KEY_SAVE_MODE, false);
        if(!saveMode){
            SnackBarHelper.generate(rlMainLayout, "Please mind that any changes won't be saved.", Snackbar.LENGTH_LONG).show();
        }
        progressBar = findViewById(R.id.progressBar);


    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // BG color string is stored to the activity state
        outState.putString(KEY_COLOR, currentBGColorString);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // If the state is being restored then previously saved color will be automatically applied
        if(savedInstanceState != null){
            String savedColor = savedInstanceState.getString(KEY_COLOR, "#ffffff");
            rlMainLayout.setBackgroundColor(Color.parseColor(savedColor));
            currentBGColorString = savedColor;
        }
    }

    /**
     * Saves data to the server
     *
     * @param colorString Color to be saved onto the server
     */
    private void saveData(final String colorString) {

        // StorageData object is created to store newest color to the storage on the server
        StorageData storageData = new StorageData(colorString);

        // SecurePreferences are prepared
        if (securePreferences == null) {
            SecurityConfig minimumConfig = new SecurityConfig.Builder(PASSWORD)
                    .build();
            securePreferences = SecurePreferences.getInstance(this, FILENAME, minimumConfig);
        }

        // Token and id are fetched from the server
        String token = securePreferences.getString("apiToken", "default");
        int id = securePreferences.getInt("id", -1);

        // If token and id exists then process proceeds
        if (!token.equals("default") && id != -1) {
            Log.d(TAG, "saveData: " + token + " " + id);

            Call<StorageData.StorageDataResponse> dataResponseCall = api.storeData(token, id, storageData);
            dataResponseCall.enqueue(new Callback<StorageData.StorageDataResponse>() {
                @Override
                public void onResponse(@NonNull Call<StorageData.StorageDataResponse> call, @NonNull Response<StorageData.StorageDataResponse> response) {
                    if (response.body() != null) {
                        showSnackBar(false);
                        Log.d(TAG, "onResponse: save data response " + response.body());
                    } else {
                        Log.d(TAG, "onResponse: save data response was null");
                        saveData(colorString);
                    }
                }
                @Override
                public void onFailure(@NonNull Call<StorageData.StorageDataResponse> call, @NonNull Throwable t) {
                    Log.d(TAG, "onFailure: failed to save data");
                    showSnackBar(true);
                    t.printStackTrace();
                }
            });
        } else {

            // If token and id doesn't exist then user is notified
            SnackBarHelper.generate(rlMainLayout, "Something went wrong . Failed to communicate with the server.").show();
            Log.d(TAG, "saveData: Something is missing" + token + " " + id);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.layout_main_activity_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.ic_delete) {
            if(!saveMode){

                // User notified about reset not possible
                SnackBarHelper.generate(rlMainLayout, "Can't reset", Snackbar.LENGTH_SHORT).show();
            }else{

                // User shown the alert dialog to continue reset
                showDeleteStorageAlert();
            }
        }
        return true;
    }

    /**
     * Shows alert for resetting the app
     */
    private void showDeleteStorageAlert() {

        // Reset alert dialog is prepared and shown
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Are you sure you want to reset the app?");
        builder.setCancelable(true);
        builder.setPositiveButton("Yep", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressBar.setVisibility(View.VISIBLE);
                deleteStorage();
            }
        });

        builder.setNegativeButton("Woops! Didn't mean to do that!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Deletes the storage on the server and resets the application
     */
    private void deleteStorage() {

        // SecurePreferences are prepared
        if (securePreferences == null) {
            SecurityConfig minimumConfig = new SecurityConfig.Builder(PASSWORD)
                    .build();
            securePreferences = SecurePreferences.getInstance(this, FILENAME, minimumConfig);
        }

        // Editor for secure preferences is prepared
        final SecurePreferences.Editor editor = securePreferences.edit();
        String token = securePreferences.getString("apiToken", "default");
        int id = securePreferences.getInt("id", -1);

        // Api call
        Call<String> call = api.deleteStorage(token, id);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.body() == null) {

                    // If failed to delete storage then another attempt is made
                    deleteStorage();
                } else {

                    // Secure preferences are updated with not valid values for token and id
                    showSnackBar(false);
                    editor.putString("apiToken", "default").apply();
                    editor.putInt("id", -1).apply();

                    // User is taken to LoginActivity to complete the reset process
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

                // If something goes wrong or app fails to connect to internet the user is notified
                showSnackBar(true);
            }
        });

    }

    /**
     * Utility function to generate ColorItem object on demand
     *
     * @param colorString Color to be stored in the object
     * @param generalName General name of the color
     * @return ColorItem object
     */
    private ColorItem generateColorItem(String colorString, String generalName) {
        return new ColorItem(colorString, generalName);
    }

    /**
     * Utility function to change background color.
     * It also calls the function to store the current color string on the server
     *
     * @param color       Color reference
     * @param colorString Color string
     */
    public void setAsBackgroundColor(int color, String colorString) {

        // Main background color is set since rlMainLayout is the main layout
        rlMainLayout.setBackgroundColor(color);
        currentBGColorString = colorString;
        if(saveMode) {
            saveData(colorString);
        }
    }

    /**
     * Utility function to show the SnackBar
     *
     * @param toShow If to show the SnackBar or dismiss it
     */
    private void showSnackBar(Boolean toShow) {
        if (snackbar == null) {
            String message = "Failed to connect to the server. Please check internet connection.";
            snackbar = SnackBarHelper.generate(rlMainLayout, message, Snackbar.LENGTH_INDEFINITE);
        }
        if (toShow) {
            snackbar.show();
        } else {
            snackbar.dismiss();
        }
    }
}
