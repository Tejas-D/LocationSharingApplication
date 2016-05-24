package com.example.tejas.finalv2.events;

/**
 * Created by tejas on 2015/08/14.
 */
public class PeerLocationSender {

    private double latitude;
    private double longitude;

    public PeerLocationSender(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {

        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

}
