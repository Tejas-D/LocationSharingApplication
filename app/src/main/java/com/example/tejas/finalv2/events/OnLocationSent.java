/**
 * File Name:               OnLocationSent.java
 * File Description:        Creating the class that stores the data that needs to be sent to the
 *                          peer device
 *
 * Author:                  Tejas Dwarkaram
 */

package com.example.tejas.finalv2.events;

public class OnLocationSent {

    private final byte[] locationToSend;
    private final boolean isOffline;
    private boolean peerConnected;

    public boolean isPeerConnected() {
        return peerConnected;
    }

    public OnLocationSent(byte[] locationToSend, boolean isOffline){
        this.locationToSend = locationToSend;
        this.isOffline = isOffline;
    }

    public byte[] getLocationToSend(){
        return locationToSend;
    }
}