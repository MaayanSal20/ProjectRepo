package entities;

import java.io.Serializable;
import java.sql.Timestamp;

public class CurrentDinerRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private int resId;
    private Timestamp reservationTime;
    private int numOfDin;
    private String status;
    private int customerId;
    private Timestamp arrivalTime;
    private String phone;
    private String email;

    public CurrentDinerRow(int resId,
                           Timestamp reservationTime,
                           int numOfDin,
                           String status,
                           int customerId,
                           Timestamp arrivalTime,
                           String phone,
                           String email) {
        this.resId = resId;
        this.reservationTime = reservationTime;
        this.numOfDin = numOfDin;
        this.status = status;
        this.customerId = customerId;
        this.arrivalTime = arrivalTime;
        this.phone = phone;
        this.email = email;
    }

    public int getResId() { return resId; }
    public Timestamp getReservationTime() { return reservationTime; }
    public int getNumOfDin() { return numOfDin; }
    public String getStatus() { return status; }
    public int getCustomerId() { return customerId; }
    public Timestamp getArrivalTime() { return arrivalTime; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
}
