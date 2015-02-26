package com.candoris.testbt.app;

/**
 * Created by cmb on 2/26/15.
 */
public class OxRecord {
    private int startByte;
    private int statusByte;
    private int plethByte;
    private int floatByte;
    private int chkByte;

    public int getStartByte() {
        return startByte;
    }

    public void setStartByte(int startByte) {
        this.startByte = startByte;
    }

    public int getStatusByte() {
        return statusByte;
    }

    public void setStatusByte(int statusByte) {
        this.statusByte = statusByte;
    }

    public int getPlethByte() {
        return plethByte;
    }

    public void setPlethByte(int plethByte) {
        this.plethByte = plethByte;
    }

    public int getFloatByte() {
        return floatByte;
    }

    public void setFloatByte(int floatByte) {
        this.floatByte = floatByte;
    }

    public int getChkByte() {
        return chkByte;
    }

    public void setChkByte(int chkByte) {
        this.chkByte = chkByte;
    }
}
