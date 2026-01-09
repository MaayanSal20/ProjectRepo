package entities;

import java.io.Serializable;
import java.sql.Timestamp;

public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    private int resId;
    private int customerId;
    private Timestamp reservationTime;
    private int numOfDin;
    private String status;
    private Timestamp arrivalTime;   // can be null
    private Timestamp leaveTime;     // can be null
    private Timestamp createdAt;
    private int confCode;
    private String source;

    // ✅ NEW: table number that was assigned for this reservation (can be null in DB)
    private Integer tableNum;

    // ✅ REQUIRED: no-args constructor (needed for "new Reservation()")
    public Reservation() {}

    public Reservation(int resId, int customerId, Timestamp reservationTime, int numOfDin,
                       String status, Timestamp arrivalTime, Timestamp leaveTime,
                       Timestamp createdAt, String source, int confCode) {
        this.resId = resId;
        this.customerId = customerId;
        this.reservationTime = reservationTime;
        this.numOfDin = numOfDin;
        this.status = status;
        this.arrivalTime = arrivalTime;
        this.leaveTime = leaveTime;
        this.createdAt = createdAt;
        this.confCode = confCode;
        this.source = source;
    }

    // ========= Getters =========
    public int getResId() { return resId; }
    public int getCustomerId() { return customerId; }
    public Timestamp getReservationTime() { return reservationTime; }
    public int getNumOfDin() { return numOfDin; }
    public String getStatus() { return status; }
    public Timestamp getArrivalTime() { return arrivalTime; }
    public Timestamp getLeaveTime() { return leaveTime; }
    public Timestamp getCreatedAt() { return createdAt; }
    public int getConfCode() { return confCode; }
    public String getSource() { return source; }
    public Integer getTableNum() { return tableNum; }

    // ========= Setters (✅ fix your error) =========
    public void setResId(int resId) { this.resId = resId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setReservationTime(Timestamp reservationTime) { this.reservationTime = reservationTime; }
    public void setNumOfDin(int numOfDin) { this.numOfDin = numOfDin; }
    public void setStatus(String status) { this.status = status; }
    public void setArrivalTime(Timestamp arrivalTime) { this.arrivalTime = arrivalTime; }
    public void setLeaveTime(Timestamp leaveTime) { this.leaveTime = leaveTime; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setConfCode(int confCode) { this.confCode = confCode; }
    public void setSource(String source) { this.source = source; }
    public void setTableNum(Integer tableNum) { this.tableNum = tableNum; }

    @Override
    public String toString() {
        return "Reservation{" +
                "resId=" + resId +
                ", customerId=" + customerId +
                ", reservationTime=" + reservationTime +
                ", numOfDin=" + numOfDin +
                ", status='" + status + '\'' +
                ", arrivalTime=" + arrivalTime +
                ", leaveTime=" + leaveTime +
                ", createdAt=" + createdAt +
                ", confCode=" + confCode +
                ", source='" + source + '\'' +
                ", tableNum=" + tableNum +
                '}';
    }
}
