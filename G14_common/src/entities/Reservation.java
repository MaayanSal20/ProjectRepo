package entities;//just to try

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

    public Reservation (int resId, int customerId, Timestamp reservationTime, int numOfDin,
                 String status, Timestamp arrivalTime, Timestamp leaveTime, Timestamp createdAt) {
        this.resId = resId;
        this.customerId = customerId;
        this.reservationTime = reservationTime;
        this.numOfDin = numOfDin;
        this.status = status;
        this.arrivalTime = arrivalTime;
        this.leaveTime = leaveTime;
        this.createdAt = createdAt;
    }

    public int getResId() { return resId; }
    public int getCustomerId() { return customerId; }
    public Timestamp getReservationTime() { return reservationTime; }
    public int getNumOfDin() { return numOfDin; }
    public String getStatus() { return status; }
    public Timestamp getArrivalTime() { return arrivalTime; }
    public Timestamp getLeaveTime() { return leaveTime; }
    public Timestamp getCreatedAt() { return createdAt; }

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
                '}';
    }
}
