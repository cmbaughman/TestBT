package com.candoris.testbt.app;

import java.util.Date;

/**
 * Created by cmb on 2/26/15.
 */
public class OxRecord {
    private OxStatus status;
    private long id;
    private Date startOfRecording;
    private Date recordedDate;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getStartOfRecording() {
        return startOfRecording;
    }

    public void setStartOfRecording(Date startOfRecording) {
        this.startOfRecording = startOfRecording;
    }

    public Date getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(Date recordedDate) {
        this.recordedDate = recordedDate;
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
