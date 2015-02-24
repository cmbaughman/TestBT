package com.candoris.testbt.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends ActionBarActivity {

    private static final String TAG="TestBT MainActivity";

    public final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private BluetoothDevice mDevice = null;

    protected Handler mHandler;
    private ConnectThread mConnectThread = null;

    public TextView outp;
    // SPP UUID service
    //private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static final String address = "00:1C:05:00:B1:0B";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outp = (TextView)findViewById(R.id.outp);

        checkBTState();

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {
                if (device.getAddress() == address) {
                    mDevice = device;
                    break;
                }

            }
        }

        if (mDevice != null) {
            mConnectThread = new ConnectThread(mDevice);
            mConnectThread.start();
        }

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                byte[] writeBuff = (byte[])msg.obj;
                int begin = (int)msg.arg1;
                int end = (int)msg.arg2;

                switch (msg.what) {
                    case 1:
                        String writeMessage = new String(writeBuff);
                        writeMessage = writeMessage.substring(begin, end);
                        break;
                }
            }
        };
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private ConnectedThread mConnectedThread = null;
        private final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(BTMODULEUUID);
            }
            catch (IOException e) {
                Log.e("TestBTConnectThread", e.getMessage(), e);
            }
            mmSocket = tmp;
        }

        public void run() {
            btAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            }
            catch (IOException e) {
                Log.e("TestBTConnectThread", e.getMessage(), e);
                try {
                    mmSocket.close();
                }
                catch (IOException ie) {
                    Log.e("TestBTConnectThread", ie.getMessage(), ie);
                }
                return;
            }

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

    private class ConnectedThread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
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

        public void run() {
            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;
            while (true) {
                try {
                    bytes += mmInStream.read(buffer, bytes, buffer.length - bytes);
                    for (int i = begin; i < bytes; i++) {
                        if (buffer[i] == Utils.msgStart) {
                            mHandler.obtainMessage(1, begin, i, buffer).sendToTarget();
                            begin = i+ 1;
                            if (i == bytes -1) {
                                bytes = 0;
                                begin = 0;
                            }
                        }
                    }
                }
                catch (IOException e) {
                    Log.e("TestBTConnectedThread", e.getMessage(), e);
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            }
            catch (IOException e) {
                Log.e("TestBTConnectedThread", e.getMessage(), e);
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
}
