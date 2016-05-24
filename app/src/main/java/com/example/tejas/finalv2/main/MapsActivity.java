/**
 * File Name:               MapsActivity.java
 * File Description:        Creating the main activity of the application that will create the
 *                          tabbed layout
 *
 * Author:                  Tejas Dwarkaram
 */

package com.example.tejas.finalv2.main;

import android.app.ActionBar;
import static com.example.tejas.finalv2.constants.Constants.*;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import com.example.tejas.finalv2.R;
import com.example.tejas.finalv2.adapters.TabAdapter;

public class MapsActivity extends FragmentActivity implements ActionBar.TabListener {

    private ViewPager viewPage;
    private ActionBar actionBar;

    private final String[] tabNames = {this.getString(R.string.map), this.getString(R.string.log), this.getString(R.string.settings), this.getString(R.string.chat)};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        viewPage = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        TabAdapter myAdapter = new TabAdapter(getSupportFragmentManager());

        viewPage.setAdapter(myAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Adding Tabs
        for (String tab_name : tabNames) {
            actionBar.addTab(actionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }

        viewPage.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        viewPage.setOffscreenPageLimit(4);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        viewPage.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        viewPage.setCurrentItem(tab.getPosition());
    }
}
