/**
 * File Name:               MapFragmentView.java
 * File Description:        Creating the view to display the map with the relevant markers
 * <p/>
 * Author:                  Tejas Dwarkaram
 * Credit:                  Brandon Talbot, Huxley Oosthuizen
 */


package com.example.tejas.finalv2.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import static com.example.tejas.finalv2.constants.Constants.*;
import com.example.tejas.finalv2.utils.CustomCenterButton;
import com.example.tejas.finalv2.events.DisplayNewLocation;
import com.example.tejas.finalv2.events.OnMapFragmentCreate;
import com.example.tejas.finalv2.events.PeerLocationSender;
import com.example.tejas.finalv2.R;
import com.example.tejas.finalv2.sql.LocationInformation;
import com.example.tejas.finalv2.sql.SQLite;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import de.greenrobot.event.EventBus;

public class MapFragmentView extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, com.google.android.gms.location.LocationListener {

    private MapView mapView;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location location;
    private boolean enableCoarseLocation;
    private boolean oldLocationAccessed;
    private String whoIsConnected;
    private EventBus eventBus;
    private static boolean mResolvingError;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventBus = EventBus.getDefault();

        //registering the class with the eventBus
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.map_fragment_view, container, false);

        mapView = (MapView) rootView.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMap = mapView.getMap();

        View myLocationButton;
        //noinspection ResourceType
        myLocationButton = rootView.findViewById(CENTER_BUTTON_ID);
        new CustomCenterButton(myLocationButton);

        EventBus.getDefault().post(new OnMapFragmentCreate(this));

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        checkPriorityTypeRequested(true);

        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        setRetainInstance(true);

        mGoogleApiClient.connect();
    }

    private void checkPriorityTypeRequested(boolean highPriorityRequested) {
        mLocationRequest = LocationRequest.create()
                .setInterval(TimeUnit.SECONDS.toMillis(10))        // 10 seconds, in milliseconds
                .setFastestInterval(TimeUnit.SECONDS.toMillis(1)); // 1 second, in milliseconds

        //checking what priority type was requested
        if (highPriorityRequested) {
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        } else {
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
    }

    private void handleNewLocation(Location location) {
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        EventBus.getDefault().post(new PeerLocationSender(currentLatitude, currentLongitude));

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        createMarker(latLng, BitmapDescriptorFactory.HUE_RED, false);
    }

    private void createMarker(LatLng latLong, float thisNewColor, boolean isPeerLocation) {
        if (mMap != null) {
            MarkerOptions options = new MarkerOptions().title(getActivity().getString(R.string.current_user)).position(latLong).draggable(false).icon(BitmapDescriptorFactory.defaultMarker(thisNewColor));
            if (isPeerLocation) {
                mMap.clear();
                handleNewLocation(location);
                options.title(getActivity().getString(R.string.peer_connected));
                Date date = new Date();
                String TIME_TODAY = TIME_FORMAT.format(date);
                SQLite sqldb = new SQLite(getActivity());
                sqldb.addLocation(new LocationInformation(0, latLong.latitude, latLong.longitude, whoIsConnected, TIME_TODAY));

            } else if (oldLocationAccessed) {
                options.title(getActivity().getString(R.string.old_location));
                oldLocationAccessed = false;
            }
            mMap.addMarker(options).showInfoWindow();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLong, 17);
            mMap.animateCamera(cameraUpdate);
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().post(new OnMapFragmentCreate(this));
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DisplayNewLocation displayNewLocation) {

        this.whoIsConnected = displayNewLocation.getConnectClient();

        this.enableCoarseLocation = displayNewLocation.isCoarseLocationEnabled();

        this.oldLocationAccessed = displayNewLocation.isOldLocationRequest();

        if (enableCoarseLocation) {
            checkPriorityTypeRequested(false);
        } else {
            checkPriorityTypeRequested(true);
        }

        LatLng newLatLngToDisplay = displayNewLocation.getNewLatLng();

        if (oldLocationAccessed) {
            createMarker(newLatLngToDisplay, BitmapDescriptorFactory.HUE_BLUE, false);
            displayNewLocation.setIsOldLocationRequest(false);
        } else {
            createMarker(newLatLngToDisplay, BitmapDescriptorFactory.HUE_GREEN, true);
            displayNewLocation.setIsOldLocationRequest(false);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        this.location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        handleNewLocation(location);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                mResolvingError = true;
                // show the localized error dialog.
                connectionResult.startResolutionForResult(getActivity(), REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        }else{
            showErrorDialog(connectionResult.getErrorCode());
            mResolvingError = true;
        }
    }


    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public static void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            onDialogDismissed();
        }
    }
}
