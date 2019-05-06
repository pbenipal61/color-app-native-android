package com.postmaninteractive.colorapp;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.github.hussainderry.securepreferences.SecurePreferences;
import com.github.hussainderry.securepreferences.model.SecurityConfig;
import com.postmaninteractive.colorapp.Adapters.ColorItemsViewAdapter;
import com.postmaninteractive.colorapp.Models.ColorItem;
import com.postmaninteractive.colorapp.Models.StorageData;
import com.postmaninteractive.colorapp.Utils.Api;
import com.postmaninteractive.colorapp.Utils.RetrofitHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.provider.Telephony.Carriers.PASSWORD;
import static android.provider.Telephony.Mms.Part.FILENAME;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RelativeLayout rlMainLayout;
    private Api api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (this.getActionBar() != null) {
            this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setCustomView(R.layout.layout_task_bar);
            getSupportActionBar().setElevation(1);
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
        rlMainLayout.setBackgroundColor(Color.parseColor(colorString));


    }

    private ColorItem generateColorItem(String colorString, String generalName) {

        return new ColorItem(colorString, generalName);
    }


    public void setAsBackgroundColor(int color, String colorString) {


        rlMainLayout.setBackgroundColor(color);
        saveData(colorString);

    }

    private void saveData(String colorString) {

        StorageData storageData = new StorageData(colorString);
        SecurityConfig minimumConfig = new SecurityConfig.Builder(PASSWORD)
                .build();


        SecurePreferences securePreferences = SecurePreferences.getInstance(this, FILENAME, minimumConfig);
        String token = securePreferences.getString("apiToken", "default");
        int id = securePreferences.getInt("id", -1);
        if (!token.equals("default") && id != -1) {
            Log.d(TAG, "saveData: " + token + " " + id);

            Call<StorageData.StorageDataResponse> dataResponseCall = api.storeData(token, id, storageData);
            dataResponseCall.enqueue(new Callback<StorageData.StorageDataResponse>() {
                @Override
                public void onResponse(Call<StorageData.StorageDataResponse> call, Response<StorageData.StorageDataResponse> response) {

                    if (response.body() != null) {

                        Log.d(TAG, "onResponse: save data response " + response.body());

                    } else {

                        Log.d(TAG, "onResponse: save data response was null");
                    }
                }

                @Override
                public void onFailure(Call<StorageData.StorageDataResponse> call, Throwable t) {

                    Log.d(TAG, "onFailure: failed to save data");
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

        switch (id) {

            case R.id.ic_delete:
                Log.d(TAG, "onOptionsItemSelected: delete");

                break;
        }

        return true;
    }

}
