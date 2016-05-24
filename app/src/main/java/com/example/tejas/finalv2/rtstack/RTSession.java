package com.example.tejas.finalv2.rtstack;

import android.util.Log;

import com.example.tejas.finalv2.rtstack.connection.Connection;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Andras Findt
 *         Date: 2013/11/18
 *         Time: 1:15 PM
 *         This is for keeping track of a session outside of the RTStack. Will maintain its rtStackHandle
 *         as well as Connection object etc. The session will also hold instances of both recording and
 *         playback objects.
 *         <p/>
 *         <p>When the SStack Call Setup process completes, a SessionID, address and port are received. Alice and Bob both
 *         connect to this same address and port.
 */
public class RTSession implements Connection.ConnectionListener, RTStack.RTStackListener {

    //##################################################################################################################
    //# todo                                          Members                                                          #
    //##################################################################################################################

    /**
     * When the session is created, an instance of {@link Connection} is spawned to maintain a socket.
     */
    private Connection connection;
    /**
     * This is a reference to the CallAgent that created the session. This is used to notify the CallAgent of changes.
     */
    private final RTStack.RTStackListener listener;
    /**
     * {@link #callSessionId} is the Session ID created by the SStack during the Call Setup process.
     * {@link #rtStackHandle} is an internal identifier used in RTStack generated when the session is started with
     */
    private long rtStackHandle;
    private final long callSessionId;

    private final Timer oneSecondTimer = new Timer();
    private final Object oneSecondTimerLock = new Object();

    /**
     * Constant value of 1 Second. (1000 ms)
     */
    private final static long UPDATE_INTERVAL = TimeUnit.SECONDS.toMillis(1);

    private final String TAG = ((Object) this).getClass().getSimpleName();

    /**
     * The ip address of the media relay
     */
    private final String address;

    /**
     * The port of the media relay
     */
    private final int port;

    /**
     * Constructor to instantiate a session. This is called when the Call Setup process completes. Created by the
     * CallAgent.
     *
     * @param listener      Reference to the CallAgent.
     * @param address       Address for the Media Relay received in the Call Setup process through SStack.
     * @param port          Port on the Media Relay. Both Alice and Bob will have the same address and port.
     * @param callSessionId Session ID from Call Setup process. Used by CallAgent to identify this session.
     */
    public RTSession(RTStack.RTStackListener listener, String address, int port, long callSessionId, boolean isAlice) {

        this.listener = listener;

        //Save the connection information
        this.address = address;
        this.port = port;

        this.callSessionId = callSessionId;
        this.rtStackHandle = RTStack.createNewSession(this.callSessionId);

        startOneSecondTimer();

        RTStack.addListener(rtStackHandle, this);

        connection = new Connection(this, address, port, rtStackHandle);
    }

    public void connect() {
        RTStack.connect(rtStackHandle);
    }

    public synchronized void destroy() {
        if (rtStackHandle != 0) {
            destroyTimer();
            destroyConnection();

            RTStack.removeListener(this.rtStackHandle);     // remove the listener
            RTStack.freeSession(rtStackHandle);             // free the memory
            rtStackHandle = 0;
        }
    }

    private void startOneSecondTimer() {
        oneSecondTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (oneSecondTimerLock) {
                    if (rtStackHandle != 0) {
                        RTStack.oneSecondTick(rtStackHandle);
                    }
                }
            }
        }, 0, 1000);
    }

    private void destroyTimer() {
        synchronized (oneSecondTimerLock) {
            oneSecondTimer.cancel();
            oneSecondTimer.purge();
        }
    }

    private void destroyConnection() {
        connection.close();                         // stop allowing data from the UDP socket

        connection = null;
    }

    /**
     * This method is called by RTStack when an error occurs.
     *
     * @param error The error from {@link RTStack.Error}
     */
    @Override
    public void onRTStackError(RTStack.Error error) {
        Log.e(TAG, "onRTStackError " + rtStackHandle + " - " + error.name());
        switch (error) {
            case CLIENT_PING_TIMEOUT:
                break;
            case CONNECT_TIMEOUT:
                break;
            case SERVER_PING_TIMEOUT:
                break;
        }
        if (listener != null) {
            //todo fire off connection state change to notify ui.
        }
    }

    /**
     * This method is called by RTStack whenever the session state changes.
     * <p/>
     * <p>{@link RTStack.State#CONNECTED_TO_CLIENT}
     * This happens when both clients have connected to the server. Once a tunnel between the peers is established,
     * we need to exchange key material to ensure the rest of our communication is secure.
     * <p>{@link RTStack.State#CONNECTING}
     * When this occurs, the RTStack will start sending out packets to connect to the Media Relay.
     * <p>{@link RTStack.State#CONNECTED_TO_SERVER}
     * Upon successful connection to the server, this state fires. It
     * <p>{@link RTStack.State#DISCONNECTED}
     * When this occurs we want to remove the session completely and close the connection
     * <p>{@link RTStack.State#DISCONNECTING}
     * When the session ends, we want to free up the session information.
     *
     * @param state The state the session is currently in from {@link RTStack.State}
     */
    @Override
    public void onRTStackSessionStateChanged(RTStack.State state) {
        Log.d(TAG, "onRTStackSessionStateChanged " + rtStackHandle + " - " + state);

        if (listener != null) {
            listener.onRTStackSessionStateChanged(state);
        }
    }

    /**
     * The RTStack calls this method when it has some data to be transferred to the network.v
     *
     * @param data
     * @param length
     */
    @Override
    public void txRawDataAvailable(byte[] data, int length) {
        synchronized (this) {
            if (connection != null) {
                connection.addToSendQueue(data);
                Log.e("EventSendLocation-", new String(data));
            }
        }
    }

    /**
     * The RTStack calls this method when a received packet contains stream data. This stream data must be passed
     * to the Jitter Buffer and processed.
     * <p/>
     * (currently just used to pass the data to UI)
     *
     * @param seq    This is the sequence number of the packet in the stream. This is used to order the packets.
     * @param data   Stream data received from the network.
     * @param length Length of the received data.
     */
    @Override
    public void rxStreamDataAvailable(int seq, byte[] data, int length) {
        if (this.listener != null) {
            this.listener.rxStreamDataAvailable(seq, data, length);
        }
    }

    @Override
    public void rxControlDataAvailable(int seq, byte type, byte[] data, int length) {
        if (this.listener != null) {
            this.listener.rxControlDataAvailable(seq, type, data, length);
        }
    }

    //##################################################################################################################
    //# todo                                           Enums                                                           #
    //##################################################################################################################


    //##################################################################################################################
    //# todo                                     ConnectionListener                                                    #
    //##################################################################################################################

    /**
     * When raw data is received from the network, this method is called by the {@link Connection} object. This
     * data is then passed to RTStack to determine the type of the packet
     *
     * @param data raw data from the packet.
     */
    @Override
    public void rxRawData(byte[] data) {
        RTStack.putRxRawData(rtStackHandle, data, data.length);
    }

    /**
     * This method is called when the socket has timed out more than {@link Connection#MAX_TIMEOUTS} times.
     * A timeout is logged each time the socket times out for more than {@link Connection#SOCKET_TIMEOUT} ms.
     */
    @Override
    public void maxTimeoutsReached() {
        disconnect();
    }

    //##################################################################################################################
    //# todo                                       Public Methods                                                      #
    //##################################################################################################################

    /**
     * This method is used to determine whether the session is still alive. Should include the SessionState.
     *
     * @return {@code true} if the session is still active.
     */
    public boolean isActive() {
        synchronized (this) {
            Log.d(TAG, "connection.isConnected() " + connection.isConnected());
            //todo make this also include some other thing like not timed out or some indication that the connection lives.
            return connection != null && connection.isConnected();
        }
    }

    /**
     * This method is used to send data to the network. Any stream data we want to send to the remote peer is sent to
     * RTStack for packetisation. Once done, {@link #txRawDataAvailable(byte[], int)} is called.
     * <p/>
     * <p>This method is called by the Audio manager once data is encoded and encrypted and ready to be sent.
     *
     * @param data Array of bytes to be sent to the network.
     */
    public void txStreamData(byte[] data, int length) {
        RTStack.putTxStreamData(rtStackHandle, data, length);
    }

    public void txControlData(byte type, byte[] data) {
        RTStack.putTxControlData(rtStackHandle, type, data, data.length);
    }

    /**
     * This closes and reopens a new udp socket
     */
    public void reconnect() {
        //make sure no data written while creating
        connection.close();
        connection = new Connection(this, this.address, this.port, rtStackHandle);
    }

    public void disconnect() {
        RTStack.endSession(rtStackHandle);     // sends the disconnect up to the server
    }

    //##################################################################################################################
    //# todo                                            Private                                                        #
    //##################################################################################################################
}
