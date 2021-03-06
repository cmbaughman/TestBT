package com.candoris.testbt.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.BitSet;
import java.util.UUID;

/**
 * Created by cmb on 2/25/15.
 */
public class BluetoothSerialService {

    private static final String TAG = "Pulse BluetoothService";
    private static final boolean D = true;

    private final BluetoothAdapter btAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private boolean mAllowInsecureConnections;
    private Context mContext;

    // Constants for WristOx2 Connection state machine
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public BluetoothSerialService(Context context, Handler handler) {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
        mContext = context;
        mAllowInsecureConnections = true;
    }

    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return mState;
    }

    // session in listening (server) mode. Called by the Activity onResume()
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_LISTEN);

        if(mAcceptThread ==null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    // Starts connect thread
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    // starts connected thread, begins managing connection
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    // stop ALL threads
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if(mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI.
     */
    private void connectionFailed() {
        setState(STATE_NONE);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, mContext.getString(R.string.toast_unable_to_connect) );
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI.
     */
    private void connectionLost() {
        setState(STATE_NONE);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, mContext.getString(R.string.toast_connection_lost) );
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    public void setAllowInsecureConnections( boolean allowInsecureConnections ) {
        mAllowInsecureConnections = allowInsecureConnections;
    }

    public boolean getAllowInsecureConnections() {
        return mAllowInsecureConnections;
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private ConnectedThread mConnectedThread = null;
        private final UUID BTMODULEUUID = Pulse.BTMODULEUUID;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                if (mAllowInsecureConnections) {
                    Method method;
                    method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    tmp = (BluetoothSocket) method.invoke(device, 1);
                } else {
                    tmp = device.createRfcommSocketToServiceRecord(BTMODULEUUID);
                }
            }
            catch (IOException e) {
                Log.e("TestBTConnectThread", e.getMessage(), e);
            }
            catch (Exception ne) {
                Log.e(TAG, "create() failed", ne);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            btAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            }
            catch (IOException e) {
                connectionFailed();
                try {
                    mmSocket.close();
                }
                catch (IOException ie) {
                    Log.e(TAG, "Unable to close socket after connection error", ie);
                }
                return;
            }

            synchronized (BluetoothSerialService.this) {
                mConnectThread = null;
            }

            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            }
            catch (IOException e) {
                Log.e("TestBTConnectThread", e.getMessage(), e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e) {
                Log.e("TestBTConnectedThread", e.getMessage(), e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        /*
         *
         */
        public void run() {
            if (D) Log.d(TAG, "BEGIN ConnectedThread");
            StringBuilder stringBuilder = null;
            BitSet bitSet = null;
            OxRecord oxRecord = null;
            byte[] buffer = null;
            int begin = 0;
            int bytes = 0;
            while (true) {
                try {
                    buffer = new byte[mmInStream.available()];
                    bytes = mmInStream.read(buffer);

                    stringBuilder = new StringBuilder();
                    oxRecord = new OxRecord();

                    for (int i=begin; i < bytes; i++) {
                        stringBuilder.append(buffer[i]);
                        stringBuilder.append(" ");
                        Log.e(TAG, "Total bytes received: " + bytes);
                        if (bytes == 1) {
                            Log.e(TAG, "1 bytes received " + buffer[i]);
                        }

                        if (bytes == 2) {
                            Log.e(TAG, "2 bytes received " + buffer[i]);
                        }

                        if (bytes == 4) {
                            Log.e(TAG, "********* 4 Bytes so it is a RT OxRecord Type ******************");
                            try {
                                switch (i) {
                                    case 0:
                                        //Status
                                        bitSet = Utils.parseByte(buffer[i]);
                                        oxRecord.setStatus(Pulse.getOxStatus(bitSet));
                                        break;
                                    case 1:
                                        // Pulse
                                        oxRecord.setHeartRate(Integer.toString(buffer[i]));
                                        break;
                                    case 2:
                                        // SpO2
                                        oxRecord.setSpO2(Integer.toString(buffer[i]));
                                        break;
                                    case 3:
                                        // Status2
                                        bitSet = Utils.parseByte(buffer[i]);
                                        oxRecord.setStatus2(Pulse.getOxStatus2(bitSet));
                                        break;
                                    default:
                                        Log.e(TAG, "Unhandled value i=" + i + " : " + buffer[i]);
                                        break;
                                }
                            }
                            catch(Exception e) {
                                Log.e(TAG, "Caught exception " + e.getMessage(), e);
                            }
                        }

                        /*
                        if (bytes > 132) {
                            // Config
                            OxConfig oxConfig = OxConfig.createOxConfig(buffer, bytes);
                        }
                        */
                    }

                    // This is a quick hack i will fix when I get these into a new method
                    if (bytes == 4) {
                        Log.e(TAG, "OxRecord: " + oxRecord.toString());
                        mHandler.obtainMessage(MainActivity.MESSAGE_READ, 8, -1, oxRecord).sendToTarget();
                    }

                    if (bytes == 10) {
                        Log.e(TAG, "********************** Got the 10 byte Date back! ******************");
                        Log.e(TAG, Utils.bytes2String(buffer, bytes));
                    }

                    if (bytes == 11) {
                        Log.e(TAG, "********************** Got the 11 byte model back! ******************");
                        Log.e(TAG, "*****Version Call returned " + Pulse.getVersion(buffer));
                    }

                    if (bytes >= 15) { // Device is in playback mode
                        int segInt = 0;
                        int seg2 = 0;
                        byte[] modl = null;
                        byte[] dt = null;
                        OxHeader header = new OxHeader();

                        for (int iter=0; iter < bytes; iter++) {
                            // Check first byte for CP mode response
                            if (buffer[iter] == Pulse.ACK) {
                                Log.e(TAG, "Received ACK**********************");
                            }
                            else if (buffer[iter] == Pulse.NAK) {
                                Log.e(TAG, "Received NAK*********************");
                            }

                            if (buffer[iter] == Pulse.CR){
                                Log.e(TAG, "Received CR*********************");
                            }
                            else if (buffer[iter] == Pulse.LF){
                                Log.e(TAG, "Received LF*********************");

                                for (int j = iter+1; j < bytes; j++) {
                                    if (buffer[j] == Pulse.CR && buffer[j+1] == Pulse.LF) {
                                        if (segInt == 0) {
                                            segInt = j+2;
                                            Log.e(TAG, "Iter: " + iter+1 + " j: " + segInt);
                                            modl = Arrays.copyOfRange(buffer, iter+1, segInt);
                                        }
                                        else if (seg2 == 0) {
                                            seg2 = j+2;
                                            Log.e(TAG, "Iter: " + segInt+1 + " j: " + seg2);
                                            dt = Arrays.copyOfRange(buffer, segInt+1, seg2);
                                            break;
                                        }
                                        else {
                                            header.setModelNumber(new String(modl, 0, modl.length));
                                            header.setCurrentDate(new String(dt, 0, dt.length));
                                        }
                                    }
                                }

                                if (header.getCurrentDate() != null && header.getModelNumber() != null) {
                                    mHandler.obtainMessage(MainActivity.MESSAGE_READ, 15, -1, header).sendToTarget();
                                    break;
                                }
                            }
                            else {
                                Log.e(TAG, "Received " + new String(new byte[]{buffer[iter]}));
                            }

                        }

                        Log.e(TAG, "AS STRING: " + new String(buffer, 0, bytes));
                        Log.e(TAG, "AS Utils.bytes2String() " + Utils.bytes2String(buffer, bytes));
                    }

                }
                catch (IOException e) {
                    Log.e("TestBTConnectedThread", e.getMessage(), e);
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            if (D) {
                Log.e(TAG, "*********** Calling write ****************");
                Log.e(TAG, "WRITING: " + Utils.bytes2String(bytes, bytes.length) + " ****************");
            }

            try {
                mmOutStream.write(bytes);
                mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, bytes.length, -1, bytes)
                        .sendToTarget();
            }
            catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            }
            catch (IOException e) {
                Log.e("TestBTConnectedThread", e.getMessage(), e);
            }
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mServerSocket;
        public static final String SDP_NAME = "NoninService";

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {
                tmp = btAdapter.listenUsingRfcommWithServiceRecord(SDP_NAME, Pulse.BTMODULEUUID);

            }
            catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            mServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;

            while(mState != STATE_CONNECTED) {
                try {
                    socket = mServerSocket.accept();
                }
                catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                    break;
                }

                if (socket != null) {
                    synchronized (BluetoothSerialService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                try {
                                    mServerSocket.close();
                                }
                                catch (IOException e) { break; }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "AcceptThread Listener activated.");
        }

        public void cancel() {
            try {
                mServerSocket.close();
            }
            catch (IOException e) {
                Log.e(TAG, "Couldnt close mServerSocket ", e);
            }
        }
    }
}
