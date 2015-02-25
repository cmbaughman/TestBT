package com.candoris.testbt.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
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
    private static final String btname = "Nonin";

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
        Log.e(TAG, pairedDevices.size() + " PAIRED FOUND ******************");
        outp.append("\nConnected .....");
        outp.append("\n" + pairedDevices.size() + " PAIRED FOUND ******************");
        if (pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {
                Log.e(TAG, device.getName() + "******************");
                outp.append("\n" + device.getName() + "******************");
                outp.append("\n" + device.getAddress() + "******************");
                Log.e(TAG, device.getAddress() + "******************");
                if (device.getName().contains(btname)) {
                    Log.e(TAG, "Found " + address + " in onCreate()");
                    outp.append("\n FOUND IT " + device.getAddress() + "******************");
                    mDevice = device;
                    break;
                }

            }
        }

        if (mDevice != null) {
            Log.e(TAG, "DEVICE NOT NULL " + address + " in onCreate()");
            outp.append("\n FOUND IT " + mDevice.getAddress() + "******************");
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
                    Log.e(TAG, "STARTED SERVICE ");
                    mSerialService.connect(mDevice);
                }
            }
        }
        btView.onResume();
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
            Log.e(TAG, "HANDLING MESSAGE: " + msg.arg1);
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(DEBUG) Log.e(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
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
                case MESSAGE_READ:
                    byte[] readBuff = (byte[])msg.obj;
                    outp.append("\n" + new String(readBuff));
                    // Write handler for message formats, use functions in either Utils or Pulse class
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuff = (byte[])msg.obj;
                    btView.write(writeBuff, msg.arg1);
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
