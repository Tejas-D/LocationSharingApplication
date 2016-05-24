package com.example.tejas.finalv2.rtstack.connection;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Andras Findt
 *         Date: 2013/11/18
 *         Time: 11:45 AM
 *         Connection calss that maintains an instance of {@link DatagramSocket} for communication with a remote peer
 *         through the Media Relay. The remote address and port the socket binds to are determined by the Signalling Switch
 *         through the SStack Call Setup process. Both Alice and Bob will have the same address and port, however, their
 *         SessionIDs will be different. The Media Relay uses the SessionID to differentiate between Alice and Bob, and the
 *         SessionID pair always differs by 1. (Alice even, Bob odd)
 *         <p/>
 *         <p>The Connection remains alive as long as the RTSession that spawned it remains active. If RTStack receives a
 *         {@link com.seecrypt.sc3.rtstack.RTStack.State#DISCONNECTED}, it will internally end the session and notify the RTSession
 *         to gracefully shut down. This will call {@link #close()} to kill off the socket, which will also kill off the
 *         helper threads.
 */
public class Connection {

    //##################################################################################################################
    //# todo                                     ConnectionListener                                                    #
    //##################################################################################################################

    /**
     * The ConnectionListener interface will notify the RTSession when there is data coming in from the socket.
     * Any raw data will be passed from here to the session object where it will pass it along to the RTStack for
     * processing. It should also notify the session when there are problems with the connection. (host errors
     * and timeouts)
     * <p/>
     * <p>Each Connection instance will only have one listener, the RTSession creating it. If the listener is
     * null, the session no longer exists. (This should not happen)
     */
    public interface ConnectionListener {
        /**
         * This method will be called whenever the Receiver thread receives a DatagramPacket. The data will be
         * passed up to the Session object where it will be sent to RTStack for processing. If the packet
         * contains stream data, it will be sent to the Jitter Buffer and SCore.
         *
         * @param data raw data from the packet.
         */
        void rxRawData(byte[] data);

        /**
         * This method notifies the listener that the socket has timed out more than {@link #MAX_TIMEOUTS} times.
         * A timeout is logged each time the socket times out for more than {@link #SOCKET_TIMEOUT} ms.
         */
        void maxTimeoutsReached();
    }


    //##################################################################################################################
    //# todo                                          Members                                                          #
    //##################################################################################################################
    /**
     * ConnectionListener instance to communicate with the RTSession.
     */
    private final ConnectionListener listener;
    /**
     * Socket instance.
     */
    private DatagramSocket socket;
    /**
     * Reference to the session inside RTStack. Currently only used for logging purposes.
     */
    private final long sessionPtr;
    /**
     * Time in ms before a socket timeout is logged.
     */
    private final int SOCKET_TIMEOUT = 0;
    /**
     * When logged timeouts reach this number, {@link #listener} is notified and appropriate action can be taken.
     */
    private final int MAX_TIMEOUTS = 50;
    /**
     * This flag is used to kill the threads when the connection is closed. Needs to be more strictly enforced.
     */
    private volatile boolean alive;
    /**
     * Queue holding instances of packets to be sent across the network through the Media Relay. The {@link Sender}
     * thread consumes {@link DatagramPacket} objects from this queue and sends them to the network.
     */
    private final BlockingQueue<DatagramPacket> queue = new ArrayBlockingQueue<DatagramPacket>(100);
    /**
     * References to the Receiver and Sender threads.
     */
    private Thread sender;
    private Thread receiver;

    private final String mAddress;
    private final int mPort;
    private static final Object socketLock = new Object();

    /**
     * Connection is the class that maintains the socket for communicating through the Media Relay. It is
     * instantiated by RTSession when it starts. The Connection object will bind a socket and start two helper
     * threads, {@link Receiver} and {@link Sender}. As long as these threads are
     *
     * @param listener   This will be the RTSession that spawned the instance of this Connection object.
     * @param address    Address of the Media Relay received in the Call Setup process through SStack.
     * @param port       Port on the Media Relay the socket must connect to. Both Alice and Bob will have the same
     *                   address and port.
     * @param sessionPtr A value referring to the instance of this session inside RTStack.
     *                   (used for logging, currently - consider moving logging to RTStack.)
     */
    public Connection(ConnectionListener listener, String address, int port, long sessionPtr) {
        this.listener = listener;
        this.sessionPtr = sessionPtr;
        mAddress = address;
        mPort = port;
        createMediaRelayConnection();
    }

    private void createMediaRelayConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (socketLock) {
                    createAndConnectSocket();
                    createAndConnectSender();
                    createAndConnectReceiver();
                }
            }
        }, "RTStackConnection").start();
    }

    private void createAndConnectReceiver() {
        receiver = new Thread(new Sender(), "Connection.Sender");
        receiver.start();
    }

    private void createAndConnectSender() {
        sender = new Thread(new Receiver(), "Connection.Receiver");
        sender.start();
    }

    private void createAndConnectSocket() {
        try {
            socket = new DatagramSocket();

            InetSocketAddress addr = new InetSocketAddress(mAddress, mPort);
            socket.connect(addr);

            socket.setSoTimeout(SOCKET_TIMEOUT);
            alive = true;
        } catch (SocketException e) {
            alive = false;
            e.printStackTrace();
        }
    }

    //##################################################################################################################
    //# todo                                       Public Methods                                                      #
    //##################################################################################################################

    /**
     * This method determines whether the socket is ready for communication. Sometimes the socket returns
     * {@code true} on both {@code isConnected()) and {@code isClosed()}, so {@link #alive} is used to ensure
     * this method returns the correct value.
     *
     * @return boolean value true if socket can communicate, and false if not.
     */
    public boolean isConnected() {
        return alive;
    }

    /**
     * This method is typically called by the RTSession instance that spawned this Connection object.
     * It constructs a {@link DatagramPacket} out of the incoming byte array and adds it to a BlockingQueue
     * used by the {@link Sender} thread. RTStack creates packets like Ping/Pong every few seconds that are sent
     * through this mechanism. Similarly, when there is Stream Data to be pushed to another peer, RTStack will
     * package the data and pass it through this method.
     *
     * @param data This data comes out of RTStack whenever it needs to send a packet to the Media Relay.
     */
    public void addToSendQueue(byte[] data) {
        try {
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
            queue.offer(datagramPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is called during the session clean-up process and will close the socket, which will cause the
     * helper threads to end too.
     */
    public void close() {
        synchronized (socketLock) {
            alive = false;

            if (sender != null) {
                sender.interrupt();
                sender = null;
            }

            if (receiver != null) {
                receiver.interrupt();
                receiver = null;
            }

            if (socket != null) {
                socket.close();
                socket = null;
            }
        }
    }

    //##################################################################################################################
    //# todo                                              Private                                                      #
    //##################################################################################################################

    /**
     * Receiver helper thread is used to receive {@link DatagramPacket} objects from the Media Relay. This is
     * spawned once the socket is created and starts listening on the socket as long as it remains connected and
     * open.
     * <p/>
     * <p>{@code socket.receive()} is a blocking call, so if the socket is closed while it is blocking, an exception
     * is thrown and caught to stop the thread.
     */
    private class Receiver implements Runnable {

        @Override
        public void run() {
            Log.i("test", "Receiver: - " + sessionPtr + " " + " - starting thread.");
            final byte[] buffer = new byte[4096];
            int length = 0;
            int timeouts = 0;
            try {
                while (isConnected() && timeouts < MAX_TIMEOUTS) {
                    final DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
                    try {
                        if (!Thread.currentThread().isInterrupted()) {
                            socket.receive(datagram);
                        } else {
                            return;
                        }
                        length = datagram.getLength();
                        if (length > 0) {
                            byte[] data = new byte[length];
                            System.arraycopy(datagram.getData(), 0, data, 0, length);
                            timeouts = 0;

                            if (alive && listener != null && !Thread.currentThread().isInterrupted()) {
                                listener.rxRawData(data);
                            }
                        }
                    } catch (SocketTimeoutException e) {
                        timeouts++;
                        if (timeouts > MAX_TIMEOUTS) {
                            if (listener != null) {
                                listener.maxTimeoutsReached();
                            }
                        }
                        e.printStackTrace();
                    }
                }
            } catch (SocketException e) {
                Log.e("test", "Receiver: - " + sessionPtr + " " + e.getLocalizedMessage());
            } catch (IOException ioex) {
                ioex.printStackTrace();
            } finally {
                alive = false;
                Log.e("test", "Receiver: - " + sessionPtr + " - stopping thread.");
            }
        }
    }

    /**
     * Sender helper thread. This is created when the socket is created, and is used to send {@link DatagramPacket}
     * objects across the network to the Media Relay. It repeatedly takes data off the {@link #queue} to send.
     * <p/>
     * <p>{@code queue.take()} is a blocking call and will wait until data is available. If the socket is killed
     * before sending can occur, an exception is thrown and caught to stop the thread.
     */
    private class Sender implements Runnable {

        @Override
        public void run() {
            Log.i("test", "Sender: - " + sessionPtr + " " + " - starting thread.");
            try {
                while (alive) {
                    DatagramPacket datagramPacket = queue.take();

                    if (!Thread.currentThread().isInterrupted()) {
                        socket.send(datagramPacket);
                    } else {
                        return;
                    }
                }
            } catch (InterruptedException e) {
                Log.e("test", "Sender: Thread interrupted.");
            } catch (SocketException e) {
                Log.e("test", "Sender: " + e.getLocalizedMessage());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                alive = false;
                Log.e("test", "Sender: - " + sessionPtr + " - stopping thread.");
            }
        }
    }
}
