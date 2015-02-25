package com.candoris.testbt.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;

public class MainActivity extends ActionBarActivity implements ITextEvents {

    // Static joints
    public static final boolean DEBUG = true;
    private static final String TAG="Pulse MainActivity";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Message types sent from the BluetoothReadService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    private boolean mEnablingBT;

    public static final int UPDATE = 1;

    private String mConnectedDeviceName;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    public final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private BluetoothDevice mDevice = null;

    private ByteQueue mByteQueue = new ByteQueue(4 * 1024);
    private byte[] mReceiveBuffer = new byte[4 * 1024];
    private static BluetoothSerialService mSerialService;

    public TextView outp;
    public BTView btView;

    // SPP UUID service
    //private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String for MAC address
    private static final String address = "00:1C:05:00:B1:0B";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DEBUG) Log.e(TAG, "+++ ON CREATE +++");

        setContentView(R.layout.activity_main);

        outp = (TextView)findViewById(R.id.outp);
        btView = (BTView)findViewById(R.id.btView);
        btView.initialize(this);

        checkBTState();

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {
                if (device.getAddress() == address) {
                    Log.e(TAG, "Found " + address + " in onCreate()");
                    mDevice = device;
                    break;
                }

            }
        }

        if (mDevice != null) {
            mSerialService = new BluetoothSerialService(this, mHandler, btView);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(DEBUG) Log.d(TAG, "In onStart()******");

        mEnablingBT = false;
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        if(DEBUG) Log.d(TAG, "in onResume()*****");

        if (!mEnablingBT) {
            if (mSerialService != null) {
                if (mSerialService.getState() == BluetoothSerialService.STATE_NONE) {
                    mSerialService.start();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(DEBUG) Log.d(TAG, "in onDestroy()*****");

        if (mSerialService != null) {
            mSerialService.stop();
        }
    }

    public int getConnectionState() {
        return mSerialService.getState();
    }

    private byte[] handleEndOfLineChars( int outgoingEoL ) {
        byte[] out;

        if ( outgoingEoL == 0x0D0A ) {
            out = new byte[2];
            out[0] = 0x0D;
            out[1] = 0x0A;
        }
        else {
            if ( outgoingEoL == 0x00 ) {
                out = new byte[0];
            }
            else {
                out = new byte[1];
                out[0] = (byte)outgoingEoL;
            }
        }

        return out;
    }

    public void send(byte[] out) {

        if ( out.length == 1 ) {

            if ( out[0] == 0x0D ) {
                out = handleEndOfLineChars( 0x0D );
            }
            else {
                if ( out[0] == 0x0A ) {
                    out = handleEndOfLineChars( 0x0A );
                }
            }
        }

        if ( out.length > 0 ) {
            mSerialService.write(out);
        }
    }

    private void checkBTState() {
        // Check Bluetooth turned on
        btAdapter=BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);

            }
        }
    }

    public void textChanged(byte[] buffer, int length) {
        try {
            mByteQueue.write(buffer, 0, length);
            outp.append(new String(buffer, StandardCharsets.US_ASCII));

        }
        catch (InterruptedException e) { }
    }

    // Handler that gets joints back from BluetoothSerialService
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(DEBUG) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothSerialService.STATE_CONNECTED:
                            setTitle("Connected to " + mConnectedDeviceName);
                            break;

                        case BluetoothSerialService.STATE_CONNECTING:
                            setTitle("Connecting....");
                            outp.append("\nConnecting...");
                            break;

                        case BluetoothSerialService.STATE_LISTEN:
                        case BluetoothSerialService.STATE_NONE:
                            setTitle("Not Connected");

                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuff = (byte[])msg.obj;
                    textChanged(writeBuff, msg.arg1);
                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Utils.alertBox(getApplicationContext(), "Connected to " + mConnectedDeviceName);
                    break;
                case MESSAGE_TOAST:
                    Utils.alertBox(getApplicationContext(), msg.getData().getString(TOAST));
            }
        }
    };

    public void finishDialogNoBluetooth() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_dialog_no_bt)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setTitle(R.string.app_name)
            .setCancelable(false)
            .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) Log.d(TAG, "In onActivityResult()");

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if(requestCode == Activity.RESULT_OK) {
                    String addr = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = btAdapter.getRemoteDevice(addr);
                    mSerialService.connect(device);
                }
            case REQUEST_ENABLE_BT:
                if (resultCode != Activity.RESULT_OK) {
                    Log.d(TAG, "Bluetooth is not enabled");
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.connect:
                if (getConnectionState() == BluetoothSerialService.STATE_NONE) {
                    Intent serverIntent = new Intent(this, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                }
                else {
                    if (getConnectionState() == BluetoothSerialService.STATE_CONNECTED) {
                        mSerialService.stop();
                        mSerialService.start();
                    }
                    return true;
                }
        }

        return false;
    }
}

/**
 * A multi-thread-safe produce-consumer byte array.
 * Only allows one producer and one consumer.
 */

class ByteQueue {
    public ByteQueue(int size) {
        mBuffer = new byte[size];
    }

    public int getBytesAvailable() {
        synchronized(this) {
            return mStoredBytes;
        }
    }

    public int read(byte[] buffer, int offset, int length)
            throws InterruptedException {
        if (length + offset > buffer.length) {
            throw
                    new IllegalArgumentException("length + offset > buffer.length");
        }
        if (length < 0) {
            throw
                    new IllegalArgumentException("length < 0");

        }
        if (length == 0) {
            return 0;
        }
        synchronized(this) {
            while (mStoredBytes == 0) {
                wait();
            }
            int totalRead = 0;
            int bufferLength = mBuffer.length;
            boolean wasFull = bufferLength == mStoredBytes;
            while (length > 0 && mStoredBytes > 0) {
                int oneRun = Math.min(bufferLength - mHead, mStoredBytes);
                int bytesToCopy = Math.min(length, oneRun);
                System.arraycopy(mBuffer, mHead, buffer, offset, bytesToCopy);
                mHead += bytesToCopy;
                if (mHead >= bufferLength) {
                    mHead = 0;
                }
                mStoredBytes -= bytesToCopy;
                length -= bytesToCopy;
                offset += bytesToCopy;
                totalRead += bytesToCopy;
            }
            if (wasFull) {
                notify();
            }
            return totalRead;
        }
    }

    public void write(byte[] buffer, int offset, int length)
            throws InterruptedException {
        if (length + offset > buffer.length) {
            throw
                    new IllegalArgumentException("length + offset > buffer.length");
        }
        if (length < 0) {
            throw
                    new IllegalArgumentException("length < 0");

        }
        if (length == 0) {
            return;
        }
        synchronized(this) {
            int bufferLength = mBuffer.length;
            boolean wasEmpty = mStoredBytes == 0;
            while (length > 0) {
                while(bufferLength == mStoredBytes) {
                    wait();
                }
                int tail = mHead + mStoredBytes;
                int oneRun;
                if (tail >= bufferLength) {
                    tail = tail - bufferLength;
                    oneRun = mHead - tail;
                } else {
                    oneRun = bufferLength - tail;
                }
                int bytesToCopy = Math.min(oneRun, length);
                System.arraycopy(buffer, offset, mBuffer, tail, bytesToCopy);
                offset += bytesToCopy;
                mStoredBytes += bytesToCopy;
                length -= bytesToCopy;
            }
            if (wasEmpty) {
                notify();
            }
        }
    }

    private byte[] mBuffer;
    private int mHead;
    private int mStoredBytes;
}

class BTView extends View {
    private static final String TAG = "BTView";
    private ByteQueue mByteQueue;

    /**
     * Used to temporarily hold data received from the remote process. Allocated
     * once and used permanently to minimize heap thrashing.
     */
    private byte[] mReceiveBuffer;

    /**
     * Our private message id, which we use to receive new input from the
     * remote process.
     */
    public static final int UPDATE = 1;

    private String mFileNameLog;
    private Date mOldTimeLog = new Date();
    private boolean mRecording = false;

    private TextView textView;

    private MainActivity mainActivity;

    private Runnable mCheckSize = new Runnable() {
        public void run() {
            mHandler.postDelayed(this, 1000);
        }
    };

    // Our message handler class. Implements a periodic callback.
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE) {
                update();
            }
        }
    };

    public BTView(Context context) {
        super(context);
        commonConstructor(context);
    }

    public void onResume() {
        mHandler.postDelayed(mCheckSize, 1000);
    }

    public void onPause() {
        mHandler.removeCallbacks(mCheckSize);
    }

    public void write(byte[] buffer, int length) {
        try {
            mByteQueue.write(buffer, 0, length);

        }
        catch (InterruptedException e) { }
        mHandler.sendMessage( mHandler.obtainMessage(UPDATE));
    }

    public BTView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BTView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        commonConstructor(context);
    }

    private void commonConstructor(Context context) {
        setVerticalScrollBarEnabled(true);
    }

    public void initialize(MainActivity ma) {
        mainActivity = ma;
        mReceiveBuffer = new byte[4 * 1024];
        mByteQueue = new ByteQueue(4 * 1024);
    }

    public void append(byte[] buffer, int base, int length) {
        textView.append(new String(buffer, StandardCharsets.US_ASCII), base, length);

    }

    public void update() {
        int bytesAvailable = mByteQueue.getBytesAvailable();
        int bytesToRead = Math.min(bytesAvailable, mReceiveBuffer.length);
        try {
            int bytesRead = mByteQueue.read(mReceiveBuffer, 0, bytesToRead);
            String stringRead = new String(mReceiveBuffer, 0, bytesRead);
            append(mReceiveBuffer, 0, bytesRead);
        }
        catch (InterruptedException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
