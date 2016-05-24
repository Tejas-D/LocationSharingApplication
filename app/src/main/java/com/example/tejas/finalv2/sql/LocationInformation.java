/**
 * File Name:               LocationInformation.java
 * File Description:        Creating the class that will get and set information for the database
 *
 * Author:                  Tejas Dwarkaram
 */

package com.example.tejas.finalv2.sql;

public class LocationInformation {

    private int locationID;
    private double latitude;
    private double longitude;
    private String timeSent;

    private String connectedUser;

    public LocationInformation(){}

    public LocationInformation(int locationID, double latitude, double longitude, String connectedUser,
                               String timeSent){
        super();
        this.locationID = locationID;
        this.latitude = latitude;
        this.longitude = longitude;
        this.connectedUser = connectedUser;
        this.timeSent = timeSent;
    }

    public String getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(String timeSent) {
        this.timeSent = timeSent;
    }

    public int getLocationID() {
        return locationID;
    }

    public void setLocationID(int locationID) {
        this.locationID = locationID;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getConnectedUser() {
        return connectedUser;
    }

    public void setConnectedUser(String connectedUser) {
        this.connectedUser = connectedUser;
    }

    @Override
    public String toString() {
        return "LocationInformation{" +
                "locationID=" + locationID +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", connectedUser=" + connectedUser +
                ", timeSent=" + timeSent +
                '}';
    }
}
