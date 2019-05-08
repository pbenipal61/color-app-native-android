package com.postmaninteractive.colorapp;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.v7.widget.RecyclerView;

import org.junit.Rule;
import org.junit.Test;

public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class);


    @Test
    public void changeBackgroundFromPosition(){
        int position = 9;
        Espresso.onView(ViewMatchers.withId(R.id.rvColorItems))
                .perform(RecyclerViewActions.actionOnItemAtPosition(position, ViewActions.click()));



    }
}
