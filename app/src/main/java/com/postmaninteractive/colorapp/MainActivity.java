package com.postmaninteractive.colorapp;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.postmaninteractive.colorapp.Adapters.ColorItemsViewAdapter;
import com.postmaninteractive.colorapp.Models.ColorItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<ColorItem> colorItems;
    private int numberOfColumns = 3;
    private static final String TAG = "MainActivity";
    private RelativeLayout rlMainLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        colorItems = new ArrayList<>();
        colorItems.add(generateColorItem("#E91E63", "Pink"));
        colorItems.add(generateColorItem("#FFEB3B", "Yellow"));
        colorItems.add(generateColorItem("#795548","Brown"));
        colorItems.add(generateColorItem("#009688", "Teal"));
        colorItems.add(generateColorItem("#3F51B5","Indigo"));
        colorItems.add(generateColorItem("#78909c", "Blue Grey"));
        colorItems.add(generateColorItem("#ff5722", "Deep Orange"));
        colorItems.add(generateColorItem("#ffc107", "Amber"));
        colorItems.add(generateColorItem("#DEB887", "Burlywood"));
        colorItems.add(generateColorItem("#FF7F50","Coral"));
        colorItems.add(generateColorItem("#B8860B","Dark Golden Rod"));
        colorItems.add(generateColorItem("#F0FFF0", "Honey Dew"));


        RecyclerView rvColorItems = findViewById(R.id.rvColorItems);
        ColorItemsViewAdapter adapter = new ColorItemsViewAdapter(this, colorItems);
        rvColorItems.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        rvColorItems.setAdapter(adapter);

        rlMainLayout = findViewById(R.id.rlMainLayout);


    }

    private ColorItem generateColorItem(String colorString, String generalName){

        return new ColorItem(colorString, generalName);
    }


    public void setAsBackgroundColor(int color){

        rlMainLayout.setBackgroundColor(color);
    }
}
