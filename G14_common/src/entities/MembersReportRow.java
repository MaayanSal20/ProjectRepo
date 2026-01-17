package entities;

import java.io.Serializable;

/**
 * Represents a daily report row for members activity.
 * This class contains aggregated data about member reservations
 * and waitlist entries for a specific day.
 */
public class MembersReportRow implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Report day in yyyy-MM-dd format.
     */
    private String day;              // "yyyy-MM-dd"

    /**
     * Number of reservations made by members on the given day.
     */
    private int reservationsCount;   // כמה הזמנות של מנויים באותו יום

    /**
     * Number of waitlist entries made by members on the given day.
     */
    private int waitlistCount;       // כמה כניסות waitlist של מנויים באותו יום

    /**
     * Constructs a new MembersReportRow object.
     *
     * @param day report day in yyyy-MM-dd format
     * @param reservationsCount number of member reservations
     * @param waitlistCount number of member waitlist entries
     */
    public MembersReportRow(String day, int reservationsCount, int waitlistCount) {
        this.day = day;
        this.reservationsCount = reservationsCount;
        this.waitlistCount = waitlistCount;
    }

    /**
     * Returns the report day.
     *
     * @return the day
     */
    public String getDay() { return day; }

    /**
     * Returns the number of member reservations.
     *
     * @return the reservations count
     */
    public int getReservationsCount() { return reservationsCount; }

    /**
     * Returns the number of member waitlist entries.
     *
     * @return the waitlist count
     */
    public int getWaitlistCount() { return waitlistCount; }

    /**
     * Returns a string representation of this report row.
     *
     * @return string representation of the object
     */
    @Override
    public String toString() {
        return "MembersReportRow{" +
                "day='" + day + '\'' +
                ", reservationsCount=" + reservationsCount +
                ", waitlistCount=" + waitlistCount +
                '}';
    }
}