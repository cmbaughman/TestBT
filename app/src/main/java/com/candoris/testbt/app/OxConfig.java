package com.candoris.testbt.app;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by cmb on 3/3/15.
 */
public class OxConfig {
    private String bluetoothAutoActivate = null; // 1
    private String activateOpt = null; // 1
    private String dataStorageRate = null; // 1
    private String displayOption = null;
    private String startTime1 = null; // 10 (YYMMDDhhmm)
    private String stopTime1 = null;
    private String startTime2 = null;
    private String stopTime2 = null;
    private String startTime3 = null;
    private String stopTime3 = null;
    private String ID = null; // 50
    private int softPartNum = 0; // 4 digits
    private String softRev = null; // 3
    private String softRevDate = null;

    public OxConfig() { }

    public static OxConfig createOxConfig(byte[] buffer, int bytes) {
        final String TAG = "OxConfig";
        byte[] tmp = null;
        OxConfig config = new OxConfig();

        Log.e(TAG, "Bytes: " + bytes);

        for (int i = 0; i < bytes; i++) {
            switch (i) {
                case 1:
                    config.bluetoothAutoActivate = new String(new byte[]{buffer[i]}, 0, 1);
                    break;
                case 2:
                    config.activateOpt = new String(new byte[]{buffer[i]}, 0, 1);
                    break;
                case 3:
                    config.dataStorageRate = new String(new byte[]{buffer[i]}, 0, 1);
                    break;
                case 4:
                    config.displayOption = new String(new byte[]{buffer[i]}, 0, 1);
                    break;
                case 5:
                    tmp = Arrays.copyOfRange(buffer, i, i+10);
                    config.startTime1 = new String(tmp, 0, tmp.length);
                    break;
                case 15:
                    tmp = Arrays.copyOfRange(buffer, i, i+10);
                    config.stopTime1 = new String(tmp, 0, tmp.length);
                    break;
                case 25:
                    tmp = Arrays.copyOfRange(buffer, i, i+10);
                    config.startTime2 = new String(tmp, 0, tmp.length);
                    break;
                case 35:
                    tmp = Arrays.copyOfRange(buffer, i, i+10);
                    config.stopTime2 = new String(tmp, 0, tmp.length);
                    break;
                case 45:
                    tmp = Arrays.copyOfRange(buffer, i, i+10);
                    config.startTime3 = new String(tmp, 0, tmp.length);
                    break;
                case 55:
                    tmp = Arrays.copyOfRange(buffer, i, i+10);
                    config.stopTime3 = new String(tmp, 0, tmp.length);
                    break;
                case 65:
                    tmp = Arrays.copyOfRange(buffer, i, i+50);
                    config.ID = new String(tmp, 0, tmp.length);
                    break;
                case 115:
                    tmp = Arrays.copyOfRange(buffer, i, i+4);
                    config.softPartNum = Integer.parseInt(new String(tmp, 0, tmp.length));
                    break;
                case 119:
                    tmp = Arrays.copyOfRange(buffer, i, i+3);
                    config.softRev = new String(tmp, 0, tmp.length);
                    break;
                case 122:
                    tmp = Arrays.copyOfRange(buffer, i, i+6);
                    // YYMMDD
                    config.softRevDate = new String(tmp, 0, tmp.length);
                    break;
                default:
                    break;
            }
        }

        return config;
    }

    public String getBluetoothAutoActivate() {
        return bluetoothAutoActivate;
    }

    public void setBluetoothAutoActivate(String bluetoothAutoActivate) {
        this.bluetoothAutoActivate = bluetoothAutoActivate;
    }

    public String getActivateOpt() {
        return activateOpt;
    }

    public void setActivateOpt(String activateOpt) {
        this.activateOpt = activateOpt;
    }

    public String getDataStorageRate() {
        return dataStorageRate;
    }

    public void setDataStorageRate(String dataStorageRate) {
        this.dataStorageRate = dataStorageRate;
    }

    public String getDisplayOption() {
        return displayOption;
    }

    public void setDisplayOption(String displayOption) {
        this.displayOption = displayOption;
    }

    public String getStartTime1() {
        return startTime1;
    }

    public void setStartTime1(String startTime1) {
        this.startTime1 = startTime1;
    }

    public String getStopTime1() {
        return stopTime1;
    }

    public void setStopTime1(String stopTime1) {
        this.stopTime1 = stopTime1;
    }

    public String getStartTime2() {
        return startTime2;
    }

    public void setStartTime2(String startTime2) {
        this.startTime2 = startTime2;
    }

    public String getStopTime2() {
        return stopTime2;
    }

    public void setStopTime2(String stopTime2) {
        this.stopTime2 = stopTime2;
    }

    public String getStartTime3() {
        return startTime3;
    }

    public void setStartTime3(String startTime3) {
        this.startTime3 = startTime3;
    }

    public String getStopTime3() {
        return stopTime3;
    }

    public void setStopTime3(String stopTime3) {
        this.stopTime3 = stopTime3;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getSoftPartNum() {
        return softPartNum;
    }

    public void setSoftPartNum(int softPartNum) {
        this.softPartNum = softPartNum;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" BT Auto Activate: ").append(this.bluetoothAutoActivate);
        stringBuilder.append(" Activation Opt: ").append(this.activateOpt);
        stringBuilder.append(" Data Storage Rate: ").append(this.dataStorageRate);
        stringBuilder.append(" Display Option: ").append(this.displayOption);
        stringBuilder.append(" Start 1 ").append(this.startTime1);
        stringBuilder.append(" Stop 1 ").append(this.stopTime1);
        stringBuilder.append(" Start 2 ").append(this.startTime2);
        stringBuilder.append(" Stop 2 ").append(this.stopTime2);
        stringBuilder.append(" Start 3 ").append(this.startTime3);
        stringBuilder.append(" Stop 3 ").append(this.stopTime3);
        stringBuilder.append(" ID ").append(this.ID);
        stringBuilder.append(" Part Num ").append(this.softPartNum);
        stringBuilder.append(" Rev ").append(this.softRev);
        stringBuilder.append(" Rev Date ").append(this.softRevDate);
        return stringBuilder.toString();
    }
}
