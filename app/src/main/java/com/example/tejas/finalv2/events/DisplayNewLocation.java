/**
 * File Name:               DisplayNewLocation.java
 * File Description:        Creating the class that sets all of the flags required for displaying
 *                          a new location
 *
 * Author:                  Tejas Dwarkaram
 */

package com.example.tejas.finalv2.events;

import com.google.android.gms.maps.model.LatLng;

public class DisplayNewLocation {

    private final LatLng newLatLng;
    private String connectClient;
    private boolean coarseLocationEnabled;
    private boolean isPeerConnected;
    private boolean isOldLocationRequest;

    public void setIsPeerConnected(boolean isPeerConnected) {
        this.isPeerConnected = isPeerConnected;
    }

    public boolean isOldLocationRequest() {
        return isOldLocationRequest;
    }

    public void setIsOldLocationRequest(boolean isOldLocationRequest) {
        this.isOldLocationRequest = isOldLocationRequest;
    }

    public boolean isCoarseLocationEnabled() {
        return coarseLocationEnabled;
    }

    public String getConnectClient() {
        return connectClient;
    }

    public void setConnectClient(String connectClient) {
        this.connectClient = connectClient;
    }

    public DisplayNewLocation(LatLng newLatLng, boolean isPeerConnected) {
        this.newLatLng = newLatLng;
        this.isPeerConnected = isPeerConnected;
    }

    public LatLng getNewLatLng() {

        return newLatLng;
    }
}