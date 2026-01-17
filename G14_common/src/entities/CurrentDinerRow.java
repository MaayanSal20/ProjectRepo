package entities;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Represents a row of current diner information.
 * This class is used to transfer data related to active or
 * ongoing reservations, including customer details and
 * arrival information.
 */
public class CurrentDinerRow implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Internal reservation identifier.
     */
    private int resId;

    /**
     * Date and time of the reservation.
     */
    private Timestamp reservationTime;

    /**
     * Number of diners for the reservation.
     */
    private int numOfDin;

    /**
     * Current reservation status.
     */
    private String status;

    /**
     * Customer or subscriber identifier.
     */
    private int customerId;

    /**
     * Actual arrival time of the diners.
     */
    private Timestamp arrivalTime;

    /**
     * Contact phone number of the customer.
     */
    private String phone;

    /**
     * Contact email address of the customer.
     */
    private String email;

    /**
     * Constructs a new CurrentDinerRow object.
     *
     * @param resId internal reservation identifier
     * @param reservationTime date and time of the reservation
     * @param numOfDin number of diners
     * @param status current reservation status
     * @param customerId customer or subscriber identifier
     * @param arrivalTime actual arrival time
     * @param phone contact phone number
     * @param email contact email address
     */
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

    /**
     * Returns the reservation identifier.
     *
     * @return the reservation ID
     */
    public int getResId() { return resId; }

    /**
     * Returns the reservation date and time.
     *
     * @return the reservation timestamp
     */
    public Timestamp getReservationTime() { return reservationTime; }

    /**
     * Returns the number of diners.
     *
     * @return the number of diners
     */
    public int getNumOfDin() { return numOfDin; }

    /**
     * Returns the current reservation status.
     *
     * @return the status
     */
    public String getStatus() { return status; }

    /**
     * Returns the customer identifier.
     *
     * @return the customer ID
     */
    public int getCustomerId() { return customerId; }

    /**
     * Returns the arrival time of the diners.
     *
     * @return the arrival timestamp
     */
    public Timestamp getArrivalTime() { return arrivalTime; }

    /**
     * Returns the contact phone number.
     *
     * @return the phone number
     */
    public String getPhone() { return phone; }

    /**
     * Returns the contact email address.
     *
     * @return the email address
     */
    public String getEmail() { return email; }
}