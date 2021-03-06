package com.postmaninteractive.colorapp;

import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.postmaninteractive.colorapp.Adapters.ColorItemsViewAdapter;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.instanceOf;


@MediumTest
@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);

    /**
     * Test for presence of recycler view
     * @throws Exception
     */
    @Test
    public void ensureRecyclerViewIsPresent() throws Exception {
        MainActivity activity = rule.getActivity();
        View viewById = activity.findViewById(R.id.rvColorItems);
        Assert.assertNotNull(viewById);
        Assert.assertEquals(viewById, instanceOf(RecyclerView.class));
        RecyclerView recyclerView = (RecyclerView) viewById;
        ColorItemsViewAdapter adapter = (ColorItemsViewAdapter) recyclerView.getAdapter();
        assert adapter != null;
        Assert.assertTrue(adapter.getItemCount() > 5);

    }


}