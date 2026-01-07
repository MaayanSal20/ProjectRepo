package entities;

import java.io.Serializable;

public class MembersReportRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private String day;              // "yyyy-MM-dd"
    private int reservationsCount;   // כמה הזמנות של מנויים באותו יום
    private int waitlistCount;       // כמה כניסות waitlist של מנויים באותו יום

    public MembersReportRow(String day, int reservationsCount, int waitlistCount) {
        this.day = day;
        this.reservationsCount = reservationsCount;
        this.waitlistCount = waitlistCount;
    }

    public String getDay() { return day; }
    public int getReservationsCount() { return reservationsCount; }
    public int getWaitlistCount() { return waitlistCount; }

    @Override
    public String toString() {
        return "MembersReportRow{" +
                "day='" + day + '\'' +
                ", reservationsCount=" + reservationsCount +
                ", waitlistCount=" + waitlistCount +
                '}';
    }
}
