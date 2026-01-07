package entities;

import java.io.Serializable;
import java.sql.Timestamp;

public class TimeReportRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private int resId;
    private Timestamp reservationTime;
    private Timestamp arrivalTime;
    private Timestamp leaveTime;

    public TimeReportRow(int resId, Timestamp reservationTime, Timestamp arrivalTime, Timestamp leaveTime) {
        this.resId = resId;
        this.reservationTime = reservationTime;
        this.arrivalTime = arrivalTime;
        this.leaveTime = leaveTime;
    }

    public int getResId() { return resId; }
    public Timestamp getReservationTime() { return reservationTime; }
    public Timestamp getArrivalTime() { return arrivalTime; }
    public Timestamp getLeaveTime() { return leaveTime; }

    @Override
    public String toString() {
        return "TimeReportRow{" +
                "resId=" + resId +
                ", reservationTime=" + reservationTime +
                ", arrivalTime=" + arrivalTime +
                ", leaveTime=" + leaveTime +
                '}';
    }
}
