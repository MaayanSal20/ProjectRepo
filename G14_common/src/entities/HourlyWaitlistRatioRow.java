package entities;

import java.io.Serializable;

/**
 * Represents waitlist ratio data for a specific hour of the day.
 * This class is typically used for reporting or statistical analysis
 * of waitlist usage by hour.
 */
public class HourlyWaitlistRatioRow implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Hour of the day.
     * Valid values range from 0 to 23.
     */
    private int hour;              // 0..23

    /**
     * Percentage of reservations placed on the waitlist.
     * Valid values range from 0 to 100.
     */
    private double percentWaitlist; // 0..100

    /**
     * Constructs a new HourlyWaitlistRatioRow object.
     *
     * @param hour hour of the day, from 0 to 23
     * @param percentWaitlist percentage of waitlist usage
     */
    public HourlyWaitlistRatioRow(int hour, double percentWaitlist) {
        this.hour = hour;
        this.percentWaitlist = percentWaitlist;
    }

    /**
     * Returns the hour of the day.
     *
     * @return the hour
     */
    public int getHour() { return hour; }

    /**
     * Returns the waitlist percentage for the hour.
     *
     * @return the waitlist percentage
     */
    public double getPercentWaitlist() { return percentWaitlist; }
}