package com.postmaninteractive.colorapp;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
    private Snackbar snackbar = null;                           // Reference to a snackbar
    private ProgressBar progressBar;                            // Reference to the progress bar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (this.getActionBar() != null) {

            ActionBar actionBar = this.getSupportActionBar();
            Objects.requireNonNull(actionBar).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.layout_task_bar);
            actionBar.setElevation(1);
            ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#93E9FA"));
            actionBar.setBackgroundDrawable(colorDrawable);
        }


        Retrofit retrofit = RetrofitHelper.generate(this);
        api = retrofit.create(Api.class);

        List<ColorItem> colorItems = new ArrayList<>();
        colorItems.add(generateColorItem("#E91E63", "Pink"));
        colorItems.add(generateColorItem("#FFEB3B", "Yellow"));
        colorItems.add(generateColorItem("#795548", "Brown"));
        colorItems.add(generateColorItem("#009688", "Teal"));
        colorItems.add(generateColorItem("#3F51B5", "Indigo"));
        colorItems.add(generateColorItem("#78909c", "Blue Grey"));
        colorItems.add(generateColorItem("#ff5722", "Deep Orange"));
        colorItems.add(generateColorItem("#ffc107", "Amber"));
        colorItems.add(generateColorItem("#DEB887", "Burlywood"));
        colorItems.add(generateColorItem("#FF7F50", "Coral"));
        colorItems.add(generateColorItem("#B8860B", "Dark Golden Rod"));
        colorItems.add(generateColorItem("#F0FFF0", "Honey Dew"));


        RecyclerView rvColorItems = findViewById(R.id.rvColorItems);
        ColorItemsViewAdapter adapter = new ColorItemsViewAdapter(this, colorItems);
        int numberOfColumns = 3;
        rvColorItems.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        rvColorItems.setAdapter(adapter);

        rlMainLayout = findViewById(R.id.rlMainLayout);
        String colorString = getIntent().getStringExtra("lastSavedColor");
        if (colorString != null) {
            rlMainLayout.setBackgroundColor(Color.parseColor(colorString));
        }
        progressBar = findViewById(R.id.progressBar);


    }


    /**
     * Saves data to the server
     *
     * @param colorString Color to be saved onto the server
     */
    private void saveData(final String colorString) {

        StorageData storageData = new StorageData(colorString);

        if (securePreferences == null) {
            SecurityConfig minimumConfig = new SecurityConfig.Builder(PASSWORD)
                    .build();
            securePreferences = SecurePreferences.getInstance(this, FILENAME, minimumConfig);
        }

        String token = securePreferences.getString("apiToken", "default");
        int id = securePreferences.getInt("id", -1);
        if (!token.equals("default") && id != -1) {
            Log.d(TAG, "saveData: " + token + " " + id);

            Call<StorageData.StorageDataResponse> dataResponseCall = api.storeData(token, id, storageData);
            dataResponseCall.enqueue(new Callback<StorageData.StorageDataResponse>() {
                @Override
                public void onResponse(Call<StorageData.StorageDataResponse> call, Response<StorageData.StorageDataResponse> response) {

                    if (response.body() != null) {
                        showSnackBar(false);
                        Log.d(TAG, "onResponse: save data response " + response.body());

                    } else {

                        Log.d(TAG, "onResponse: save data response was null");
                        saveData(colorString);
                    }
                }

                @Override
                public void onFailure(Call<StorageData.StorageDataResponse> call, Throwable t) {

                    Log.d(TAG, "onFailure: failed to save data");
                    showSnackBar(true);
                    t.printStackTrace();
                }
            });
        } else {


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
            Log.d(TAG, "onOptionsItemSelected: delete");
            showDeleteStorageAlert();
        }

        return true;
    }

    /**
     * Shows alert for resetting the app
     */
    private void showDeleteStorageAlert() {

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
        if (securePreferences == null) {
            SecurityConfig minimumConfig = new SecurityConfig.Builder(PASSWORD)
                    .build();
            securePreferences = SecurePreferences.getInstance(this, FILENAME, minimumConfig);
        }
        final SecurePreferences.Editor editor = securePreferences.edit();
        String token = securePreferences.getString("apiToken", "default");
        int id = securePreferences.getInt("id", -1);

        Log.d(TAG, "deleteStorage: " + token + " " + id);

        Call<String> call = api.deleteStorage(token, id);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                Log.d(TAG, "onResponse: delete response " + response);
                if (response.body() == null) {
                    deleteStorage();
                } else {
                    showSnackBar(false);
                    editor.putString("apiToken", "default").apply();
                    editor.putInt("id", -1).apply();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

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


        rlMainLayout.setBackgroundColor(color);
        saveData(colorString);

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
