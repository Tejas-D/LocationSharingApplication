/**
 * File Name:               SettingsView.java
 * File Description:        Creating the view that houses all of the settings available to the user
 *
 * Author:                  Tejas Dwarkaram
 * Credit:                  Nico Kiewiet, Huxley Oosthuizen, Brandon Talbot
 */

package com.example.tejas.finalv2.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import com.example.tejas.finalv2.events.ConnectedUser;
import com.example.tejas.finalv2.events.CoarseLocationOption;
import com.example.tejas.finalv2.events.CurrentSession;
import com.example.tejas.finalv2.eventbus.EventsReceiver;
import com.example.tejas.finalv2.events.SessionDisconnectRequest;
import com.example.tejas.finalv2.main.NewUserSession;
import com.example.tejas.finalv2.events.OnLocationSent;
import com.example.tejas.finalv2.R;
import com.example.tejas.finalv2.rtstack.RTStack;
import com.example.tejas.finalv2.sql.LocationInformation;
import com.example.tejas.finalv2.sql.SQLite;
import static com.example.tejas.finalv2.constants.Constants.*;
import static com.example.tejas.finalv2.enums.LocationSenders.*;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import de.greenrobot.event.EventBus;

public class SettingsView extends Fragment {

    private Button isAliceConnecting;
    private Button isBobConnecting;
    private Button onDisconnectButton;
    private Button onSendLocationButton;
    private final Handler sendLocationHandler = new Handler();
    private TextView stateConnectedTextView;
    private TextView userName;
    private TextView coarseStatusView;
    private boolean flag_alice;
    private EditText autoSendText;
    private Switch coarseLocationSwitch;
    private byte[] locationArray;
    private Switch autoSendSwitch;
    private boolean connected = false;
    private boolean sessionActive = false;
    private final EventsReceiver newReceiver = new EventsReceiver();
    private int autoSendInt = (int) TimeUnit.SECONDS.toMillis(2);
    private boolean continueScheduling;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.settings_view, container, false);

        isAliceConnecting = (Button) rootView.findViewById(R.id.connectAliceBtn);
        isBobConnecting = (Button) rootView.findViewById(R.id.connectBobBtn);

        onSendLocationButton = (Button) rootView.findViewById(R.id.sendLocationBtn);
        onDisconnectButton = (Button) rootView.findViewById(R.id.disconnectStateBtn);
        stateConnectedTextView = (TextView) rootView.findViewById(R.id.connectionStateLabel);
        userName = (TextView) rootView.findViewById(R.id.userNameTxt);
        coarseStatusView = (TextView) rootView.findViewById(R.id.coarseStatusView);
        autoSendText = (EditText) rootView.findViewById(R.id.autoSendTimeTxt);
        autoSendSwitch = (Switch) rootView.findViewById(R.id.autoSendSwitch);
        coarseLocationSwitch = (Switch) rootView.findViewById(R.id.coarseLocationSwitch);

        autoSendText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String textEntered = s.toString();

                if(!textEntered.isEmpty()){
                    autoSendInt = (int) TimeUnit.SECONDS.toMillis(Long.parseLong(textEntered));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        autoSendSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    autoSendText.setEnabled(true);
                    continueScheduling = true;

                    sendLocationHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(continueScheduling) {
                                sendLocationHandler.postDelayed(this, autoSendInt);
                            }
                            sendNewLocation();
                        }
                    });
                }else{
                    autoSendText.setEnabled(false);
                    continueScheduling = false;
                }
            }
        });


        coarseLocationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    EventBus.getDefault().post(new CoarseLocationOption(true));
                    coarseStatusView.setText(getActivity().getString(R.string.coarse_location));
                }else{
                    EventBus.getDefault().post(new CoarseLocationOption(false));
                    coarseStatusView.setText(getActivity().getString(R.string.fine_location));
                }
            }
        });

        isAliceConnecting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag_alice = true;
                EventBus.getDefault().post(new NewUserSession(ALICE_ID, flag_alice));
                sessionActive = true;
                EventBus.getDefault().post(new ConnectedUser(ALICE.getName()));
            }
        });

        isBobConnecting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag_alice = false;
                EventBus.getDefault().post(new NewUserSession(BOB_ID, flag_alice));
                sessionActive = true;
                EventBus.getDefault().post(new ConnectedUser(BOB.getName()));
            }
        });

        onDisconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new SessionDisconnectRequest());
                EventBus.getDefault().post(new OnLocationSent(locationArray, true));
            }
        });

        onSendLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View n) {
                sendNewLocation();
            }
        });

        return rootView;
    }

    private void sendNewLocation(){
        String latitudeString = Double.toString(newReceiver.latitude);
        String longitudeString = Double.toString(newReceiver.longitude);

        String appendedLocation = latitudeString + "," + longitudeString;

        locationArray = appendedLocation.getBytes();

        SQLite sqldb = new SQLite(getActivity());
        Date date = new Date();
        String TIME_TODAY = TIME_FORMAT.format(date);
        sqldb.addLocation(new LocationInformation(0, newReceiver.latitude, newReceiver.longitude, ME.getName(), TIME_TODAY));

        EventBus.getDefault().post(new OnLocationSent(locationArray, false));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(sessionActive)
            EventBus.getDefault().post(new SessionDisconnectRequest());
        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CurrentSession currentSession) {

        boolean stillConnecting = true;
        RTStack.State currentState = currentSession.getCurrentState();

        if (RTStack.State.CONNECTING.equals(currentState)) {
            stateConnectedTextView.setText(getActivity().getString(R.string.connecting));
            stillConnecting = true;
        }
        if (RTStack.State.CONNECTED_TO_SERVER.equals(currentState)) {
            stateConnectedTextView.setText(getActivity().getString(R.string.connected_to_server));
            stillConnecting = true;
        }

        if(stillConnecting){
            isAliceConnecting.setEnabled(false);
            isBobConnecting.setEnabled(false);
            onDisconnectButton.setVisibility(View.VISIBLE);
        }

        if (RTStack.State.CONNECTED_TO_CLIENT.equals(currentState)) {
            stateConnectedTextView.setText(getActivity().getString(R.string.connected_to_client));
            connected = true;
            stillConnecting = false;
        }

        if (RTStack.State.DISCONNECTING.equals(currentState)) {
            stateConnectedTextView.setText(getActivity().getString(R.string.disconnecting));
        }

        if (RTStack.State.DISCONNECTED.equals(currentState)) {
            connected = false;
            if(stillConnecting) {
                userName.setText(getActivity().getString(R.string.connection_closed));
            }else{
                userName.setText(getActivity().getString(R.string.client_disconnected));
            }
            stateConnectedTextView.setText(getActivity().getString(R.string.disconnected));
            isAliceConnecting.setEnabled(true);
            isBobConnecting.setEnabled(true);
            isAliceConnecting.setVisibility(View.VISIBLE);
            onSendLocationButton.setEnabled(false);
            autoSendSwitch.setEnabled(false);
            autoSendText.setEnabled(false);
            coarseLocationSwitch.setEnabled(false);
            coarseStatusView.setText("");
            isBobConnecting.setVisibility(View.VISIBLE);
            onDisconnectButton.setVisibility(View.INVISIBLE);
        }
        if (connected) {
            if (flag_alice) {
                userName.setText(getActivity().getString(R.string.alice_connected));
                enableElements();
            } else {
                userName.setText(getActivity().getString(R.string.bob_connected));
                enableElements();
            }
        }
    }

    public void enableElements(){
        isAliceConnecting.setVisibility(View.INVISIBLE);
        onSendLocationButton.setEnabled(true);
        autoSendSwitch.setEnabled(true);
        coarseLocationSwitch.setEnabled(true);
        isBobConnecting.setVisibility(View.INVISIBLE);
        onDisconnectButton.setVisibility(View.VISIBLE);
    }
}