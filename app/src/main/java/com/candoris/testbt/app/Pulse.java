package com.candoris.testbt.app;

import java.util.BitSet;
import java.util.Calendar;
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

    // Retrieve Date Time from the 3150
    public static final byte[] CMDGETDATIME = { (byte)0x02, (byte)0x72, (byte)0x00, (byte)0x03 };
    // Set Date Time on the 3150
    public static byte[] CMDSETDATIME() {
        Calendar calendar = Calendar.getInstance();
        byte yr = (byte)Integer.parseInt(String.valueOf(calendar.get(Calendar.YEAR)), 16);
        byte mo = (byte)Integer.parseInt(String.valueOf(calendar.get(Calendar.MONTH)+1), 16);
        byte day = (byte)Integer.parseInt(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)), 16);
        byte hour = (byte)Integer.parseInt(String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)), 16);
        byte min = (byte)Integer.parseInt(String.valueOf(calendar.get(Calendar.MINUTE)), 16);
        byte sec = (byte)Integer.parseInt(String.valueOf(calendar.get(Calendar.SECOND)), 16);

        byte[] tmp = { (byte)0x02, (byte)0x72, (byte)0x06, yr, mo, day, hour, min, sec, (byte)0x03 };

        return tmp;
    }
    // Set data format and activate (DF8)
    public static final byte[] CMDSETCONFIG = { (byte)0x44, (byte)0x38 };
    // Enable ATR and set data format (DF13)
    public static final byte[] CMDSETCONFIG13 =
            { (byte)0x02, (byte)0x70,  (byte)0x04, (byte)0x02, (byte)0x0D, (byte)0x01, (byte)0x84, (byte)0x03};

    public static final byte[] CMDGETMODEL = { (byte)0x02, (byte)0x74, (byte)0x02, (byte)0x05, (byte)0x05, (byte)0x03 };

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
