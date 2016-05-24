/**
 * File Name:               OldLocationAccessed.java
 * File Description:        Creating the class that sets whether a old location request was made to
 *                          be displayed
 *
 * Author:                  Tejas Dwarkaram
 */

package com.example.tejas.finalv2.events;

public class OldLocationAccessed {

    private final byte[] locationToSend;
    private final boolean isOffline;
    private final boolean isOldLocation;

    public boolean isOldLocation() {
        return isOldLocation;
    }

    public OldLocationAccessed(byte[] locationToSend, boolean isOffline){
        this.locationToSend = locationToSend;
        this.isOffline = false;
        this.isOldLocation = true;
    }

    public byte[] getLocationToSend(){
        return locationToSend;
    }

}
