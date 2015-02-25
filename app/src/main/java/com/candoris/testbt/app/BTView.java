package com.candoris.testbt.app;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class BTView extends View {
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
        textView = new TextView(context);
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
