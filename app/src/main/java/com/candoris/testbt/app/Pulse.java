package com.candoris.testbt.app;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by cmb on 2/25/15.
 */
public class Pulse {
    public static final String TAG = "Pulse";
    // Static address for now
    public static final String address = "00:1C:05:00:B1:0B";
    // RFCOMM Standard
    public static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final byte msgStart = (byte)0x02;
    public static final byte msgEnd = (byte)0x03;
    // ACK=good NAK=bad
    public static final byte ACK = (byte)0x06;
    public static final byte NAK = (byte)0x15;

    // EOL CHARS ASCII
    public static final char CR = (char)13;
    public static final char LF = (char)10;

    // Retrieve Date Time from the 3150
    public static final byte[] CMDGETDATIME = { (byte)0x02, (byte)0x72, (byte)0x00, (byte)0x03 };
    // Set Date Time on the 3150
    public static byte[] CMDSETDATIME() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy");
        byte yr = (byte)(Integer.parseInt(Integer.toHexString(Integer.parseInt(simpleDateFormat.format(new Date()))),
                16) & 0xff);
        byte mo = (byte)(Integer.parseInt(String.valueOf(calendar.get(Calendar.MONTH)), 16) & 0xff);
        byte day = (byte)(Integer.parseInt(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)), 16) & 0xff);
        byte hour = (byte)(Integer.parseInt(String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)), 16) & 0xff);
        byte min = (byte)(Integer.parseInt(String.valueOf(calendar.get(Calendar.MINUTE)), 16) & 0xff);
        byte sec = (byte)(Integer.parseInt(String.valueOf(calendar.get(Calendar.SECOND)), 16) & 0xff);

        byte[] tmp = { (byte)0x02, (byte)0x72, (byte)0x06, yr, mo, day, hour, min, sec, (byte)0x03 };
        return tmp;
    }
    // Level 2 command
    public static byte[] CMDSETDATIME2() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        String ret = "DTM" + sdf.format(new Date());
        Log.e("PULSE", "CMDSETDATETIME: " + ret);
        return ret.getBytes(); // 150305101634
    }
    // Set data format and activate (DF8)
    public static final byte[] CMDSETCONFIG = { (byte)0x44, (byte)0x38 };
    // Enable ATR and set data format (DF13)
    public static final byte[] CMDSETCONFIG13 =
            { (byte)0x02, (byte)0x70,  (byte)0x04, (byte)0x02, (byte)0x0D, (byte)0x01, (byte)0x84, (byte)0x03};
    // Get Model Version info from the 3150
    public static final byte[] CMDGETMODEL = { (byte)0x02, (byte)0x74, (byte)0x02, (byte)0x05, (byte)0x05, (byte)0x03 };
    // Level 2 COMMANDS
    // Memory playback
    public static final String CMDMPC = "MPC?" + CR + LF;
    // Level 2 DateTime
    public static final String CMDDTM = "DTM?" + CR + LF;
    // Level 2 Get Header
    public static final String CMDHDR = "HDR?" + CR + LF;
    // Level 2 Playback Configuration
    public static final String CMDCFG = "CFG?" + CR + LF;


    public static OxStatus getOxStatus(BitSet bitSet) {
        OxStatus oxStatus = new OxStatus();
        oxStatus.resv = bitSet.get(6);
        oxStatus.oot = bitSet.get(5);
        oxStatus.lowPerfusion = bitSet.get(4);
        oxStatus.marginalPerfusion = bitSet.get(3);
        oxStatus.artf = bitSet.get(2);
        return oxStatus;
    }

    public static OxStatus getOxStatus2(BitSet bitSet) {
        OxStatus oxStatus = new OxStatus();
        oxStatus.resv = bitSet.get(6);
        oxStatus.smartPointAlgo = bitSet.get(5);
        oxStatus.sensorAlarm = bitSet.get(3);
        oxStatus.lowBat = bitSet.get(0);
        return oxStatus;
    }

    public static String getVersion(byte[] ver) {
        return Utils.bytes2String(ver, ver.length);
    }

}
