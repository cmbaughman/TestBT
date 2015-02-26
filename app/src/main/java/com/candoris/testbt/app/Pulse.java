package com.candoris.testbt.app;

import java.util.BitSet;
import java.util.UUID;

/**
 * Created by cmb on 2/25/15.
 */
public class Pulse {
    // Static address for now
    public static final String address = "00:1C:05:00:B1:0B";
    // RFCOMM Standard
    public static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final byte msgStart = (byte)0x02;
    public static final byte msgEnd = (byte)0x03;

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

}
