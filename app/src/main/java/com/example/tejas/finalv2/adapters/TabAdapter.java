/**
 * File Name:               TabAdapter.java
 * File Description:        Creating the class that will create the tabs and store the fragments on
 *                          them
 *
 * Author:                  Tejas Dwarkaram
 */

package com.example.tejas.finalv2.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.example.tejas.finalv2.fragments.LogView;
import com.example.tejas.finalv2.fragments.MapFragmentView;
import com.example.tejas.finalv2.fragments.SettingsView;
import com.example.tejas.finalv2.fragments.MessagingView;

public class TabAdapter extends FragmentPagerAdapter{


    public TabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position){
            case 0:
                return new MapFragmentView();
            case 1:
                return new LogView();
            case 2:
                return new SettingsView();
            case 3:
                return new MessagingView();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }
}