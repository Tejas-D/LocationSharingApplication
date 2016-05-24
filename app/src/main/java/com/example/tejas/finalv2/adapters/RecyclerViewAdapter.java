/**
 * File Name:               RecyclerViewAdapter.java
 * File Description:        Creating the adapter to bind the database records into the RecyclerView
 *
 * Author:                  Tejas Dwarkaram
 * Credit:                  Brandon Talbot
*/
package com.example.tejas.finalv2.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.tejas.finalv2.events.OldLocationAccessed;
import com.example.tejas.finalv2.R;
import com.example.tejas.finalv2.sql.LocationInformation;
import java.util.List;
import de.greenrobot.event.EventBus;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private final List<LocationInformation> beans;

    public RecyclerViewAdapter(List<LocationInformation> beans) {
        this.beans = beans;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_layout, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int i) {
        LocationInformation leBean = beans.get(i);
        viewHolder.bind(leBean);
    }

    @Override
    public int getItemCount() {
        return beans.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView time;
        final TextView longitude;
        final TextView latitude;
        double latitudeOfRecord;
        double longitudeOfRecord;

        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            time = (TextView) itemView.findViewById(R.id.time);
            longitude = (TextView) itemView.findViewById(R.id.longitude);
            latitude = (TextView) itemView.findViewById(R.id.latitude);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //creating a marker based on historical data
                    String latitudeString = Double.toString(latitudeOfRecord);
                    String longitudeString = Double.toString(longitudeOfRecord);

                    String appendedLocation = latitudeString + "," + longitudeString;

                    byte[] locationArray = appendedLocation.getBytes();

                    EventBus.getDefault().post(new OldLocationAccessed(locationArray, false));
                }
            });
        }

        public void bind(LocationInformation leBean) {
            this.latitudeOfRecord = leBean.getLatitude();
            this.longitudeOfRecord = leBean.getLongitude();

            //setting the fields to the retrieved values
            name.setText(leBean.getConnectedUser());
            time.setText(leBean.getTimeSent());
            longitude.setText(leBean.getLongitude() + "");
            latitude.setText(leBean.getLatitude() + "");
        }
    }
}