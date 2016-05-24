/**
 * File Name:               EventsReceiver.java
 * File Description:        Creating the class to receive all of the interactions with the EventBus
 *
 * Author:                  Tejas Dwarkaram
 * Credit:                  Brandon Talbot
 */

package com.example.tejas.finalv2.eventbus;

import com.example.tejas.finalv2.events.ConnectedUser;
import com.example.tejas.finalv2.events.CoarseLocationOption;
import com.example.tejas.finalv2.events.CurrentSession;
import com.example.tejas.finalv2.events.DisplayNewLocation;
import com.example.tejas.finalv2.events.SessionDisconnectRequest;
import com.example.tejas.finalv2.main.NewUserSession;
import com.example.tejas.finalv2.events.OldLocationAccessed;
import com.example.tejas.finalv2.events.OnLocationSent;
import com.example.tejas.finalv2.events.PeerLocationSender;
import com.example.tejas.finalv2.rtstack.RTSession;
import com.example.tejas.finalv2.rtstack.RTStack;
import com.google.android.gms.maps.model.LatLng;
import de.greenrobot.event.EventBus;

public class EventsReceiver {

    public double latitude;
    public double longitude;
    private LatLng recentlySentLatLng;
    private String whoIsConnected;
    private boolean oldLocationAccessed;
    private boolean coarseLocationEnabled;
    private boolean isPeerConnected;

    private RTStack.State currentState;

    private RTSession newSession;

    public EventsReceiver() {
        EventBus eventBus = EventBus.getDefault();

        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(final NewUserSession event) {
        NewUserSession sessionEvent = event;
        this.newSession = event.getNewSession();
        this.currentState = event.getStateReturned();
        this.recentlySentLatLng = event.getCurrentLatLng();
    }


    @SuppressWarnings("unused")
    public void onEvent(CurrentSession event) {
        this.currentState = event.getCurrentState();
    }

    @SuppressWarnings("unused")
    public void onEvent(PeerLocationSender event) {
        this.latitude = event.getLatitude();
        this.longitude = event.getLongitude();
    }

    @SuppressWarnings("unused")
    public void onEvent(SessionDisconnectRequest event) {
        this.newSession.disconnect();
    }

    @SuppressWarnings("unused")
    public void onEvent(OnLocationSent event) {
        this.isPeerConnected = event.isPeerConnected();
        byte[] locationArray = event.getLocationToSend();
        newSession.txStreamData(locationArray, locationArray.length);
    }

    @SuppressWarnings("unused")
    public void onEvent(OldLocationAccessed event) {
        this.oldLocationAccessed = event.isOldLocation();
        LatLng convertedLatLng = convertData(event.getLocationToSend());
        if(convertedLatLng != null){
            EventBus.getDefault().post(new DisplayNewLocation(convertedLatLng, false));
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(DisplayNewLocation event) {
        this.recentlySentLatLng = event.getNewLatLng();
        event.setIsOldLocationRequest(oldLocationAccessed);
        oldLocationAccessed = false;
        event.setIsPeerConnected(isPeerConnected);
        event.setConnectClient(whoIsConnected);
    }

    @SuppressWarnings("unused")
    public void onEvent(ConnectedUser event) {
        this.whoIsConnected = event.getWhoIsConnected();
    }

    @SuppressWarnings("unused")
    public void onEvent(CoarseLocationOption event){
        this.coarseLocationEnabled = event.isEnableCoarseLocation();
    }

    private LatLng convertData(byte[] data) {
        String locationFromBytes = new String(data);
        String[] locationSplitArray = locationFromBytes.split(",");
        double latitude = Double.parseDouble(locationSplitArray[0]);
        double longitude = Double.parseDouble(locationSplitArray[1]);

        return new LatLng(latitude, longitude);
    }

}