/**
 * File Name:               NewUserSession.java
 * File Description:        Creating the class that will initiate a connection to the RTStack
 *
 * Author:                  Tejas Dwarkaram
 * Credit:                  Nico Kiewiet, Brandon Talbot
 */

package com.example.tejas.finalv2.main;

import com.example.tejas.finalv2.events.CurrentSession;
import com.example.tejas.finalv2.events.DisplayNewLocation;
import com.example.tejas.finalv2.rtstack.RTSession;
import com.example.tejas.finalv2.rtstack.RTStack;
import com.google.android.gms.maps.model.LatLng;
import static com.example.tejas.finalv2.constants.Constants.*;
import java.io.UnsupportedEncodingException;
import de.greenrobot.event.EventBus;

public class NewUserSession implements RTStack.RTStackListener{

    private final RTSession newSession;
    private RTStack.State stateReturned;
    private LatLng currentLatLng;

    public LatLng getCurrentLatLng() {
        return currentLatLng;
    }

    private void setCurrentLatLng(LatLng currentLatLng) {
        this.currentLatLng = currentLatLng;
    }

    public NewUserSession(int userSessionID, boolean flag) {
        this.newSession = new RTSession(this, IP_ADDRESS, PORT_NUMBER, userSessionID, flag);

        newSession.connect();
    }

    public RTSession getNewSession(){
        return newSession;
    }

    @Override
    public void onRTStackError(RTStack.Error error) {
        newSession.disconnect();
    }

    @Override
    public void onRTStackSessionStateChanged(RTStack.State state) {
        setStateReturned(state);
        EventBus.getDefault().post(new CurrentSession(state));
    }

    private void setStateReturned(RTStack.State stateReturned) {
        this.stateReturned = stateReturned;
    }

    public RTStack.State getStateReturned() {
        return this.stateReturned;
    }

    @Override
    public void txRawDataAvailable(byte[] data, int length) {
    }

    @Override
    public void rxStreamDataAvailable(int seq, byte[] data, int length) {
        if(data != null) {
            LatLng convertedLatLng = null;
            try {
                convertedLatLng = convertData(data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            setCurrentLatLng(convertedLatLng);
            EventBus.getDefault().post(new DisplayNewLocation(convertedLatLng, true));
        }
    }

    private LatLng convertData(byte[] data) throws UnsupportedEncodingException {
        String locationFromBytes = new String(data, ENCODING_FORMAT);
        String[] locationSplitArray = locationFromBytes.split(",");
        double latitude = Double.parseDouble(locationSplitArray[0]);
        double longitude = Double.parseDouble(locationSplitArray[1]);

        return new LatLng(latitude, longitude);
    }

    @Override
    public void rxControlDataAvailable(int seq, byte type, byte[] data, int length) {
    }
}