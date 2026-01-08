package entities;

import java.io.Serializable;
import java.sql.Timestamp;

public class TimeReportRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private int resId;
    private int confCode;
    private String source;
    private Timestamp reservationTime;
    private Timestamp notifiedAt;
    private Timestamp arrivalTime;
    private Timestamp leaveTime;
    private Timestamp effectiveStart;
    private int lateMinutes;
    private int stayMinutes;
    private int overstayMinutes;

    // ✅ constructor חדש (זה מה ש-ReportsRepository יחזיר)
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

    /*// (אפשר להשאיר את הישן אם משתמשים בו במקום אחר)
    public TimeReportRow(int resId, Timestamp reservationTime, Timestamp arrivalTime, Timestamp leaveTime) {
        this.resId = resId;
        this.reservationTime = reservationTime;
        this.arrivalTime = arrivalTime;
        this.leaveTime = leaveTime;
    }*/

    public int getResId() { return resId; }
    public int getConfCode() { return confCode; }
    public String getSource() { return source; }
    public Timestamp getReservationTime() { return reservationTime; }
    public Timestamp getNotifiedAt() { return notifiedAt; }
    public Timestamp getArrivalTime() { return arrivalTime; }
    public Timestamp getLeaveTime() { return leaveTime; }
    public Timestamp getEffectiveStart() { return effectiveStart; }
    public int getLateMinutes() { return lateMinutes; }
    public int getStayMinutes() { return stayMinutes; }
    public int getOverstayMinutes() { return overstayMinutes; }

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
