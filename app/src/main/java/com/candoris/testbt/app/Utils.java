package com.candoris.testbt.app;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by cmb on 2/24/15.
 */
public class Utils {

    public static final String TAG = "Utils";

    public static final byte msgStart = (byte)0x02;
    public static final byte msgEnd = (byte)0x03;


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
