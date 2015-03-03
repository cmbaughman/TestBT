package com.candoris.testbt.app;

/**
 * Created by cmb on 3/3/15.
 */
public class OxHeader {
    private String modelNumber = null;
    private String currentDate = null;
    private String oxy = null;
    private String usb = null;
    private String sab = null;

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String getOxy() {
        return oxy;
    }

    public void setOxy(String oxy) {
        this.oxy = oxy;
    }

    public String getUsb() {
        return usb;
    }

    public void setUsb(String usb) {
        this.usb = usb;
    }

    public String getSab() {
        return sab;
    }

    public void setSab(String sab) {
        this.sab = sab;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" Model: ").append(this.modelNumber);
        stringBuilder.append(" Current Date: ").append(this.currentDate);
        stringBuilder.append(" OXY: ").append(this.oxy);
        stringBuilder.append(" USB: ").append(this.usb);
        stringBuilder.append(" SAB: ").append(this.sab);
        return stringBuilder.toString();
    }
}
