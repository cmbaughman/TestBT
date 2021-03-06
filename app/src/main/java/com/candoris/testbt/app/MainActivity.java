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
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
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
    public TextView outPulse;
    public TextView outOx;
    public TextView lblOx;
    public TextView outModel;
    public TextView outDate;
    public Button btnGetDateTime;
    public Button btnSetDateTime;
    public Button btnGetModel;
    public Button btnPlayBack;
    public Button btnGetConfig;

    // SPP UUID service
    //private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String for MAC address
    private static final String address = "00:1C:05:00:B1:0B";
    private static final String btname = "Nonin";

    // NOTE: These is for recording OxRecords
    private static final boolean RECORD_ON = true;
    OxDataSource dataSource = null;
    Date dateStartedRecording = new Date();
    Date recorded = null;
    // ******* End Recording props

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DEBUG) Log.e(TAG, "+++ ON CREATE +++");

        setContentView(R.layout.activity_main);

        outp = (TextView)findViewById(R.id.outp);
        outPulse = (TextView)findViewById(R.id.outPulse);
        outOx = (TextView)findViewById(R.id.outOx);
        lblOx = (TextView)findViewById(R.id.lblOx);
        outDate = (TextView)findViewById(R.id.outDate);
        outModel = (TextView)findViewById(R.id.outModel);
        btnGetDateTime = (Button)findViewById(R.id.btnGetDateTime);
        btnSetDateTime = (Button)findViewById(R.id.btnSetDateTime);
        btnGetModel = (Button)findViewById(R.id.btnGetModel);
        btnPlayBack = (Button)findViewById(R.id.btnPlayBack);
        btnGetConfig = (Button)findViewById(R.id.btnGetCfg);

        lblOx.setText(Html.fromHtml("SpO<sup>2</sup>"));
        outp.setMovementMethod(new ScrollingMovementMethod());

        // remove this
        OxRecord ot = new OxRecord();
        ot.setHeartRate("100");
        ot.setSpO2("101");
        saveOxRecord(ot);
        List<OxRecord> tst = dataSource.getAllOxRecords();
        outp.append("\n\n WORKS!!! " + tst.get(0).getId() + " \n\n");

        checkBTState();

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        outp.append("\nConnecting .....");

        if (pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {

                outp.append("\nFound " + device.getName() + " ***** " + device.getAddress());

                if (device.getName().contains(btname)) {
                    mDevice = device;
                    break;
                }

            }
        }

        if (mDevice != null) {
            Log.e(TAG, "DEVICE NOT NULL " + address + " in onCreate()");
            outp.append("\n Connecting to " + mDevice.getAddress());
            mSerialService = new BluetoothSerialService(this, mHandler);
        }

        btnGetDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This command gets date/time from the 3150
                send(Pulse.CMDGETDATIME);
            }
        });

        btnSetDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] dbytes = Pulse.CMDSETDATIME();
                Log.e(TAG, "The date is: " + Utils.bytes2String(dbytes, dbytes.length));
                send(Pulse.CMDSETDATIME());
            }
        });

        btnGetModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send(Pulse.CMDHDR.getBytes());
            }
        });

        btnPlayBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send(Pulse.CMDMPC.getBytes());
            }
        });

        btnGetConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send(Pulse.CMDCFG.getBytes());
                send(Pulse.CMDSETDF13);
            }
        });
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
                    Log.d(TAG, "STARTED BluetoothSerialService ");
                    mSerialService.connect(mDevice);
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

        dataSource.close();
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

    public void write(byte[] buffer, int length) {
        try {
            mByteQueue.write(buffer, 0, length);
        }
        catch (InterruptedException e) {
            Log.e(TAG, "write() error " + e.getMessage(), e);
        }
        mHandler.sendMessage( mHandler.obtainMessage(UPDATE));
    }

    private void checkBTState() {
        // Check Bluetooth turned on
        btAdapter=BluetoothAdapter.getDefaultAdapter();
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
                            // Send initial command sequence for data format #8
                            outp.append("\nConnected. Sending initial data command. ");
                            mSerialService.write(Pulse.CMDSETDF);
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
                    if (msg.arg1 == 8) {
                        // Data Format #8
                        OxRecord oxRecord = (OxRecord)msg.obj;
                        outPulse.setText(oxRecord.getHeartRate());
                        outOx.setText(oxRecord.getSpO2());
                        outp.append("\n" + oxRecord.toString());

                        // For recording measurements
                        saveOxRecord(oxRecord);
                    }
                    else if (msg.arg1 == 15) {
                        // Level 2 Get Model
                        OxHeader header = (OxHeader)msg.obj;
                        outModel.setText(header.getModelNumber());
                        outDate.setText(header.getCurrentDate());
                    }
                    else {
                        Log.e(TAG, "MESSAGE_READ: " + msg.obj);
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

    public void saveOxRecord(OxRecord oxRec) {
        if (dataSource == null) {
            dataSource = new OxDataSource(this);

            try {
                dataSource.open();
            }
            catch (SQLException e) {
                Log.e(TAG, e.getMessage(), e);
            }

            recorded = new Date();
            dataSource.createOxRecord(oxRec.getHeartRate(), oxRec.getSpO2(), dateStartedRecording, recorded);
            Log.d(TAG, "Data recorded.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
