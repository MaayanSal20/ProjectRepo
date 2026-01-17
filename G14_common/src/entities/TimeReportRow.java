package entities;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Represents a single row in a time-based reservations report.
 * This class is used for reporting and analysis of reservation
 * timing behavior, including arrival delays, stay duration,
 * and overstays.
 */
public class TimeReportRow implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Internal reservation identifier.
     */
    private int resId;

    /**
     * Reservation confirmation code.
     */
    private int confCode;

    /**
     * Source of the reservation.
     * For example web, terminal, or representative.
     */
    private String source;

    /**
     * Scheduled reservation time.
     */
    private Timestamp reservationTime;

    /**
     * Timestamp when the customer was notified.
     */
    private Timestamp notifiedAt;

    /**
     * Actual arrival time of the customer.
     */
    private Timestamp arrivalTime;

    /**
     * Actual leave time of the customer.
     */
    private Timestamp leaveTime;

    /**
     * Effective start time of the reservation.
     * This may differ from the scheduled reservation time.
     */
    private Timestamp effectiveStart;

    /**
     * Number of minutes the customer arrived late.
     */
    private int lateMinutes;

    /**
     * Total number of minutes the customer stayed.
     */
    private int stayMinutes;

    /**
     * Number of minutes the customer overstayed beyond the expected time.
     */
    private int overstayMinutes;

    /**
     * Constructs a new TimeReportRow object.
     * This constructor is typically used by the reports repository.
     *
     * @param resId internal reservation identifier
     * @param confCode reservation confirmation code
     * @param source reservation source
     * @param reservationTime scheduled reservation time
     * @param notifiedAt notification timestamp
     * @param arrivalTime actual arrival time
     * @param leaveTime actual leave time
     * @param effectiveStart effective start time
     * @param lateMinutes minutes of delay on arrival
     * @param stayMinutes total stay duration in minutes
     * @param overstayMinutes overstay duration in minutes
     */
    public TimeReportRow(int resId, int confCode, String source,
                         Timestamp reservationTime, Timestamp notifiedAt,
                         Timestamp arrivalTime, Timestamp leaveTime,
                         Timestamp effectiveStart,
                         int lateMinutes, int stayMinutes, int overstayMinutes) {

        this.resId = resId;
        this.confCode = confCode;
        this.source = source;
        this.reservationTime = reservationTime;
        this.notifiedAt = notifiedAt;
        this.arrivalTime = arrivalTime;
        this.leaveTime = leaveTime;
        this.effectiveStart = effectiveStart;
        this.lateMinutes = lateMinutes;
        this.stayMinutes = stayMinutes;
        this.overstayMinutes = overstayMinutes;
    }

    /**
     * Returns the reservation identifier.
     *
     * @return the reservation ID
     */
    public int getResId() { return resId; }

    /**
     * Returns the confirmation code.
     *
     * @return the confirmation code
     */
    public int getConfCode() { return confCode; }

    /**
     * Returns the reservation source.
     *
     * @return the source
     */
    public String getSource() { return source; }

    /**
     * Returns the scheduled reservation time.
     *
     * @return the reservation timestamp
     */
    public Timestamp getReservationTime() { return reservationTime; }

    /**
     * Returns the notification timestamp.
     *
     * @return the notification time
     */
    public Timestamp getNotifiedAt() { return notifiedAt; }

    /**
     * Returns the actual arrival time.
     *
     * @return the arrival timestamp
     */
    public Timestamp getArrivalTime() { return arrivalTime; }

    /**
     * Returns the actual leave time.
     *
     * @return the leave timestamp
     */
    public Timestamp getLeaveTime() { return leaveTime; }

    /**
     * Returns the effective start time.
     *
     * @return the effective start timestamp
     */
    public Timestamp getEffectiveStart() { return effectiveStart; }

    /**
     * Returns the number of late arrival minutes.
     *
     * @return late minutes
     */
    public int getLateMinutes() { return lateMinutes; }

    /**
     * Returns the total stay duration in minutes.
     *
     * @return stay minutes
     */
    public int getStayMinutes() { return stayMinutes; }

    /**
     * Returns the number of overstay minutes.
     *
     * @return overstay minutes
     */
    public int getOverstayMinutes() { return overstayMinutes; }

    /**
     * Returns a string representation of this time report row.
     *
     * @return string representation of the object
     */
    @Override
    public String toString() {
        return "TimeReportRow{" +
                "resId=" + resId +
                ", confCode=" + confCode +
                ", source='" + source + '\'' +
                ", reservationTime=" + reservationTime +
                ", notifiedAt=" + notifiedAt +
                ", arrivalTime=" + arrivalTime +
                ", leaveTime=" + leaveTime +
                ", effectiveStart=" + effectiveStart +
                ", lateMinutes=" + lateMinutes +
                ", stayMinutes=" + stayMinutes +
                ", overstayMinutes=" + overstayMinutes +
                '}';
    }
}
