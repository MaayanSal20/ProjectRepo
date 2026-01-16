package entities;

import java.io.Serializable;

public class HourlyWaitlistRatioRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private int hour;              // 0..23
    private double percentWaitlist; // 0..100

    public HourlyWaitlistRatioRow(int hour, double percentWaitlist) {
        this.hour = hour;
        this.percentWaitlist = percentWaitlist;
    }

    public int getHour() { return hour; }
    public double getPercentWaitlist() { return percentWaitlist; }
}
