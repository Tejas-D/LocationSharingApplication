/**
 * File Name:               LogView.java
 * File Description:        Creating the Log View to display the contents of the database
 *
 * Author:                  Tejas Dwarkaram
 */

package com.example.tejas.finalv2.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.tejas.finalv2.R;
import com.example.tejas.finalv2.adapters.RecyclerViewAdapter;
import com.example.tejas.finalv2.sql.LocationInformation;
import com.example.tejas.finalv2.sql.SQLite;
import java.util.ArrayList;
import java.util.List;

public class LogView extends Fragment {

    private RecyclerView recyclerView;
    private SQLite sqldb;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) view.findViewById(R.id.listViewID);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);

        //setting onclicklistener to swipe refresh the database
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }

            void refreshItems() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        displayDB();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.log_view, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        displayDB();
    }

    private void displayDB(){
        sqldb = new SQLite(getActivity());

        List<LocationInformation> ls   = sqldb.getAllLocationDetails();

        List<LocationInformation> lebeans = new ArrayList<>();

        for(int i = 0; i< ls.size();i++) {
            LocationInformation bean = new LocationInformation(ls.get(i).getLocationID(),
                    ls.get(i).getLatitude(), ls.get(i).getLongitude(),
                    ls.get(i).getConnectedUser(), ls.get(i).getTimeSent());
            lebeans.add(bean);
        }

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(lebeans);
        recyclerView.setAdapter(adapter);
    }

}