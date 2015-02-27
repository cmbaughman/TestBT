package com.candoris.testbt.app;

/**
 * Created by cmb on 2/26/15.
 */
public class OxStatus {
    public boolean artf = false;
    public boolean resv = false;
    public boolean oot = false;
    public boolean lowPerfusion = false;
    public boolean marginalPerfusion = false;
    public boolean sensorAlarm = false;
    public boolean smartPointAlgo = false;
    public boolean lowBat = false;

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" Artifact condition: ").append(this.artf);
        stringBuilder.append(" Out Of Track: ").append(this.oot);
        stringBuilder.append(" Low Perfusion: ").append(this.lowPerfusion);
        stringBuilder.append(" Marginal Perfustion: ").append(this.marginalPerfusion);
        stringBuilder.append(" Sensor Alarm: ").append(this.sensorAlarm);
        stringBuilder.append(" Smart Point Algorithm: ").append(this.smartPointAlgo);
        stringBuilder.append(" Low Battery: ").append(this.lowBat);
        return stringBuilder.toString();
    }
}
