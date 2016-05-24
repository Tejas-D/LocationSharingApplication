package com.example.tejas.finalv2.rtstack;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 10/14/13
 * Time: 12:11 PM
 */

@SuppressWarnings("ALL")
public class RTStack {

    private static final String TAG = "RTStack";     // needs to be static so we can use it from static methods

    //##################################################################################################################
    //# todo                                       RTStackListener                                                     #
    //##################################################################################################################

    public interface RTStackListener {
        void onRTStackError(Error error);

        void onRTStackSessionStateChanged(State state);

        void txRawDataAvailable(byte[] data, int length);

        void rxStreamDataAvailable(int seq, byte[] data, int length);

        void rxControlDataAvailable(int seq, byte type, byte[] data, int length);
    }

    private static final Map<Long, RTStackListener> rtStackListeners = new HashMap<Long, RTStackListener>();

    public static void addListener(long rtStackHandle, RTStackListener listener) {
        synchronized (rtStackListeners) {
            rtStackListeners.put(rtStackHandle, listener);         // replaces old value of key it already exists
        }
    }

    public static void removeListener(long rtStackHandle) {
        synchronized (rtStackListeners) {
            rtStackListeners.remove(rtStackHandle);                // returns null if key doesn't exist
        }
    }

    //##################################################################################################################
    //# todo                                          Enums                                                            #
    //##################################################################################################################
    public enum State {
        DISCONNECTED(0),
        CONNECTING(1),
        CONNECTED_TO_SERVER(2),
        CONNECTED_TO_CLIENT(3),
        DISCONNECTING(4),

        UNKNOWN(Integer.MAX_VALUE);    // this is what is returned when an unsupported value comes from the stack

        private final int value;

        State(int value) {
            this.value = value;
        }

        public static State fromValue(int value) {
            for (State e : State.values()) {
                if (e.getValue() == value) {
                    return e;
                }
            }

            Log.e(TAG, "Unsupported State: " + value);
            return UNKNOWN;
        }

        int getValue() {
            return this.value;
        }
    }

    public enum Error {
        CONNECT_TIMEOUT(0),
        SERVER_PING_TIMEOUT(1),
        CLIENT_PING_TIMEOUT(2),

        UNKNOWN(Integer.MAX_VALUE);    // this is what is returned when an unsupported value comes from the stack

        private final int value;

        Error(int value) {
            this.value = value;
        }

        public static Error fromValue(int value) {
            for (Error e : Error.values()) {
                if (e.getValue() == value) {
                    return e;
                }
            }

            Log.e(TAG, "Unsupported Error: " + value);
            return UNKNOWN;
        }

        int getValue() {
            return this.value;
        }
    }

    //##################################################################################################################
    //# todo                                        Native Methods                                                     #
    //##################################################################################################################

    private static native long doCreateNewSession(long rtStackHandle, long arg);

    private static native void doFreeSession(long rtStackHandle);

    private static native void doConnect(long rtStackHandle);

    private static native void doEndSession(long rtStackHandle);

    private static native void doSetTxRawDataCallback(long rtStackHandle);

    private static native void doSetRxStreamDataCallback(long rtStackHandle);

    private static native void doSetRxControlDataCallback(long rtStackHandle);

    private static native void doSetErrorCallback(long rtStackHandle);

    private static native void doSetStateChangeCallback(long rtStackHandle);

    private static native void doSetDebugCallback(long rtStackHandle);

    private static native void doOneSecondTick(long rtStackHandle);

    private static native void doPutTxStreamData(long rtStackHandle, byte[] data, int length);

    private static native void doPutTxControlData(long rtStackHandle, byte type, byte[] data, int length);

    private static native void doPutRxRawData(long rtStackHandle, byte[] data, int length);

    private static native void initIds();

    static {
        System.loadLibrary("RTStack");
        initIds();
    }

    //##################################################################################################################
    //# todo                                          Members                                                          #
    //##################################################################################################################

    //##################################################################################################################
    //# todo                                       Public Methods                                                      #
    //##################################################################################################################

    public static long createNewSession(long sessionId) {
        long rtStackHandle = doCreateNewSession(sessionId, 0);

        doSetTxRawDataCallback(rtStackHandle);
        doSetRxStreamDataCallback(rtStackHandle);
        doSetRxControlDataCallback(rtStackHandle);
        doSetErrorCallback(rtStackHandle);
        doSetStateChangeCallback(rtStackHandle);
        //doSetDebugCallback(rtStackHandle);

        return rtStackHandle;
    }

    public static void freeSession(long rtStackHandle) {      // NB: DANGEROUS doing this separately to removeListener
        if (rtStackHandle == 0) {
            throw new RuntimeException("null rtstack handle!!!");
        }
        Log.d(TAG, "free rt stack: " + rtStackHandle);
        doFreeSession(rtStackHandle);
    }

    public static void connect(long rtStackHandle) {
        doConnect(rtStackHandle);
    }

    public static void endSession(long rtStackHandle) {
        if (rtStackHandle == 0) {
            throw new RuntimeException("null rtstack handle!!!");
        }
        doEndSession(rtStackHandle);
    }

    public static void oneSecondTick(long rtStackHandle) {
        if (rtStackHandle == 0) {
            throw new RuntimeException("null rtstack handle!!!");
        }
        doOneSecondTick(rtStackHandle);
    }

    public static void putTxStreamData(long rtStackHandle, byte[] data, int length) {
        if (rtStackHandle == 0) {
            throw new RuntimeException("null rtstack handle!!!");
        }
        doPutTxStreamData(rtStackHandle, data, length);
    }

    public static void putTxControlData(long rtStackHandle, byte type, byte[] data, int length) {
        if (rtStackHandle == 0) {
            throw new RuntimeException("null rtstack handle!!!");
        }
        doPutTxControlData(rtStackHandle, type, data, length);
    }

    public static void putRxRawData(long rtStackHandle, byte[] data, int length) {
        if (rtStackHandle == 0) {
            throw new RuntimeException("null rtstack handle!!!");
        }
        doPutRxRawData(rtStackHandle, data, length);
    }

    public static byte onTxRawData(long rtStackHandle, byte[] data, int length) {
        RTStackListener listener = null;

        synchronized (rtStackListeners) {
            listener = rtStackListeners.get(rtStackHandle);
        }

        if (listener != null)
            listener.txRawDataAvailable(data, length);

        // todo: need to return the result of the listener call : 0 for ok and 1 for socket error

        return 0;
    }

    public static void onRxStreamData(long rtStackHandle, int seq, byte[] data, int length) {
        RTStackListener listener = null;

        synchronized (rtStackListeners) {
            listener = rtStackListeners.get(rtStackHandle);
        }

        if (listener != null)
            listener.rxStreamDataAvailable(seq, data, length);
    }

    public static void onRxControlData(long rtStackHandle, int seq, byte type, byte[] data, int length) {
        RTStackListener listener = null;

        synchronized (rtStackListeners) {
            listener = rtStackListeners.get(rtStackHandle);
        }

        if (listener != null)
            listener.rxControlDataAvailable(seq, type, data, length);
    }

    public static void onError(long rtStackHandle, int error) {
        RTStackListener listener = null;

        synchronized (rtStackListeners) {
            listener = rtStackListeners.get(rtStackHandle);
        }

        if (listener != null) {
            Error errorEnum = Error.fromValue(error);
            listener.onRTStackError(errorEnum);
        }
    }

    public static void onSessionStateChange(long rtStackHandle, int state) {
        RTStackListener listener = null;

        synchronized (rtStackListeners) {
            listener = rtStackListeners.get(rtStackHandle);
        }

        if (listener != null) {
            State stateEnum = State.fromValue(state);
            listener.onRTStackSessionStateChanged(stateEnum);
        }
    }

    public static void onRTStackDebug(byte[] debug, int len) {
        Log.d(TAG, new String(debug));
    }
}


