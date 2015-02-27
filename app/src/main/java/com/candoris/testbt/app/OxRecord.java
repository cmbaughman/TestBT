package com.candoris.testbt.app;

/**
 * Created by cmb on 2/26/15.
 */
public class OxRecord {
    private OxStatus status;
    private String heartRate;
    private String spO2;
    private OxStatus status2;

    public OxStatus getStatus() {
        return status;
    }

    public void setStatus(OxStatus status) {
        this.status = status;
    }

    public String getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(String heartRate) {
        this.heartRate = heartRate;
    }

    public String getSpO2() {
        return spO2;
    }

    public void setSpO2(String spO2) {
        this.spO2 = spO2;
    }

    public OxStatus getStatus2() {
        return status2;
    }

    public void setStatus2(OxStatus status2) {
        this.status2 = status2;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Status: ").append(this.status.toString());
        sb.append("\nHeart Rate: ").append(this.heartRate);
        sb.append("\nSpO2: ").append(this.spO2);
        sb.append("\nStatus2: ").append(this.status2.toString());
        return sb.toString();
    }
}
