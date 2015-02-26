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

    /**
     * Use this to get back a bitset representation of the byte,
     * then access the specific bits with parseByte(b).get(2); or parseByte(b).get(3);
     * for the 2nd and 3rd bits respectively.
     * @param b byte
     * @return java.util.BitSet
     */
    public static BitSet parseByte(byte b) {
        BitSet bitSet = new BitSet(8);
        for (int i=0; i < 8; i++) {
            bitSet.set(i, (b & 1) == 1);
            b >>= 1;
        }
        return bitSet;
    }
}
