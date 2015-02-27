package com.candoris.testbt.app;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.BitSet;

/**
 * Created by cmb on 2/24/15.
 */
public class Utils {

    public static final String TAG = "Pulse - Utils";

    public static void log(String message) {
        if (message != null)
            Log.i("Pulse", message);
    }

    public static String printHex(String hex) {
        StringBuilder sb = new StringBuilder();
        int len = hex.length();
        try {
            for (int i = 0; i < len; i += 2) {
                sb.append("0x").append(hex.substring(i, i+2)).append(" ");
            }
        }
        catch (NumberFormatException e) {
            log(e.getMessage());
        }
        catch (StringIndexOutOfBoundsException e) {
            log(e.getMessage());
        }
        return sb.toString();
    }

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

    public static String bytes2String(byte[] b,  int count) {
        StringBuilder ret = new StringBuilder();

        for (int i=0; i < count; i++) {
            String thaInt = Integer.toString((int)(b[i] & 0xFF));
            ret.append(thaInt);
            ret.append(" ");
        }

        return ret.toString();
    }

    public static byte[] toHex(String hex) {
        int len = hex.length();
        byte[] result = new byte[len];
        try {
            int index = 0;
            for (int i = 0; i < len; i += 2) {
                result[index] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
                index++;
            }
        } catch (NumberFormatException e) {
            log("toHex NumberFormatException: " + e.getMessage());

        } catch (StringIndexOutOfBoundsException e) {
            log("toHex StringIndexOutOfBoundsException: " + e.getMessage());
        }
        return result;
    }

    public static byte[] getBytesFromInputStream(InputStream inputStream) {
        int leng = 0;
        byte[] bytes = null;
        try {
            leng = inputStream.available();
            bytes = new byte[(int)leng];

            int offSet = 0;
            int numRead = 0;

            while (offSet < bytes.length && (numRead=inputStream.read(bytes, offSet, bytes.length-offSet)) >= 0) {
                offSet += numRead;
            }

            if (offSet < bytes.length) {
                throw new IOException("Could not completely read inputStream!");
            }
        }
        catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return bytes;
    }

    public static byte[] concat(byte[] A, byte[] B) {
        byte[] C = new byte[A.length + B.length];
        System.arraycopy(A, 0, C, 0, A.length);
        System.arraycopy(B, 0, C, A.length, B.length);
        return C;
    }

    public static void alertBox(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static BluetoothSocket createRfcommSocket(BluetoothDevice device) {
        BluetoothSocket tmp = null;

        try {
            Class class1 = device.getClass();
            Class aclass[] = new Class[1];
            Method method = class1.getMethod("createRfcommSocket", aclass);
            Object aobj[] = new Object[1];
            aobj[0] = Integer.valueOf(1);

            tmp = (BluetoothSocket) method.invoke(device, aobj);
        }
        catch (NoSuchMethodException e) {
            Log.e(TAG, "createRfcommSocket failed ", e);
        }
        catch (InvocationTargetException e) {
            Log.e(TAG, "createRfcommSocket failed ", e);
        }
        catch (IllegalAccessException e) {
            Log.e(TAG, "createRfcommSocket failed ", e);
        }
        return tmp;
    }

}
