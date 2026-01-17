package entities;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Represents a reservation in the system.
 * This class holds all relevant information about a reservation,
 * including customer details, timing, table assignment, status,
 * and reminder tracking.
 */
public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Internal reservation identifier.
     */
    private int resId;

    /**
     * Customer or subscriber identifier.
     */
    private int customerId;

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
     * Actual arrival time of the diners.
     * Can be null if the diners have not arrived yet.
     */
    private Timestamp arrivalTime;   // can be null

    /**
     * Actual leave time of the diners.
     * Can be null if the diners have not left yet.
     */
    private Timestamp leaveTime;     // can be null

    /**
     * Timestamp when the reservation was created.
     */
    private Timestamp createdAt;

    /**
     * Reservation confirmation code.
     */
    private int confCode;

    /**
     * Source of the reservation.
     * For example, web, mobile app, or representative.
     */
    private String source;

    /**
     * Assigned table number.
     * Can be null if no table has been assigned yet.
     */
    private Integer tableNum;

    /**
     * Indicates whether a reminder was sent to the customer.
     */
    private boolean reminderSent;

    /**
     * Timestamp when the reminder was sent.
     * Can be null if no reminder was sent.
     */
    private Timestamp reminderSentAt;

    /**
     * No-arguments constructor.
     * Required for serialization and frameworks usage.
     */
    public Reservation() {}

    /**
     * Constructs a new Reservation object with full details.
     *
     * @param resId internal reservation identifier
     * @param customerId customer or subscriber identifier
     * @param reservationTime date and time of the reservation
     * @param numOfDin number of diners
     * @param status current reservation status
     * @param arrivalTime actual arrival time, may be null
     * @param leaveTime actual leave time, may be null
     * @param createdAt creation timestamp
     * @param source reservation source
     * @param confCode reservation confirmation code
     * @param tableNum assigned table number, may be null
     * @param reminderSent indicates whether a reminder was sent
     * @param reminderSentAt timestamp when reminder was sent, may be null
     */
    public Reservation(int resId,
                       int customerId,
                       Timestamp reservationTime,
                       int numOfDin,
                       String status,
                       Timestamp arrivalTime,
                       Timestamp leaveTime,
                       Timestamp createdAt,
                       String source,
                       int confCode,
                       Integer tableNum,
                       boolean reminderSent,
                       Timestamp reminderSentAt) {

        this.resId = resId;
        this.customerId = customerId;
        this.reservationTime = reservationTime;
        this.numOfDin = numOfDin;
        this.status = status;
        this.arrivalTime = arrivalTime;
        this.leaveTime = leaveTime;
        this.createdAt = createdAt;
        this.source = source;
        this.confCode = confCode;
        this.tableNum = tableNum;
        this.reminderSent = reminderSent;
        this.reminderSentAt = reminderSentAt;
    }

    // ========= Getters =========

    /**
     * Returns the reservation identifier.
     *
     * @return the reservation ID
     */
    public int getResId() { return resId; }

    /**
     * Returns the customer identifier.
     *
     * @return the customer ID
     */
    public int getCustomerId() { return customerId; }

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
     * Returns the reservation status.
     *
     * @return the status
     */
    public String getStatus() { return status; }

    /**
     * Returns the arrival time.
     *
     * @return the arrival timestamp, or null if not arrived
     */
    public Timestamp getArrivalTime() { return arrivalTime; }

    /**
     * Returns the leave time.
     *
     * @return the leave timestamp, or null if not left
     */
    public Timestamp getLeaveTime() { return leaveTime; }

    /**
     * Returns the creation timestamp.
     *
     * @return the creation timestamp
     */
    public Timestamp getCreatedAt() { return createdAt; }

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
     * Returns the assigned table number.
     *
     * @return the table number, or null if not assigned
     */
    public Integer getTableNum() { return tableNum; }

    /**
     * Indicates whether a reminder was sent.
     *
     * @return true if reminder was sent, false otherwise
     */
    public boolean isReminderSent() { return reminderSent; }

    /**
     * Returns the reminder sent timestamp.
     *
     * @return the reminder timestamp, or null if not sent
     */
    public Timestamp getReminderSentAt() { return reminderSentAt; }

    // ========= Setters =========

    /**
     * Sets the reservation identifier.
     *
     * @param resId the reservation ID
     */
    public void setResId(int resId) { this.resId = resId; }

    /**
     * Sets the customer identifier.
     *
     * @param customerId the customer ID
     */
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    /**
     * Sets the reservation date and time.
     *
     * @param reservationTime the reservation timestamp
     */
    public void setReservationTime(Timestamp reservationTime) { this.reservationTime = reservationTime; }

    /**
     * Sets the number of diners.
     *
     * @param numOfDin number of diners
     */
    public void setNumOfDin(int numOfDin) { this.numOfDin = numOfDin; }

    /**
     * Sets the reservation status.
     *
     * @param status the reservation status
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Sets the arrival time.
     *
     * @param arrivalTime the arrival timestamp
     */
    public void setArrivalTime(Timestamp arrivalTime) { this.arrivalTime = arrivalTime; }

    /**
     * Sets the leave time.
     *
     * @param leaveTime the leave timestamp
     */
    public void setLeaveTime(Timestamp leaveTime) { this.leaveTime = leaveTime; }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt the creation timestamp
     */
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    /**
     * Sets the confirmation code.
     *
     * @param confCode the confirmation code
     */
    public void setConfCode(int confCode) { this.confCode = confCode; }

    /**
     * Sets the reservation source.
     *
     * @param source the source
     */
    public void setSource(String source) { this.source = source; }
    
    
    /**
     * Sets the table number assigned to this reservation.
     * Can be null if no table has been assigned yet.
     *
     * @param tableNum the table number to assign
     */
    public void setTableNum(Integer tableNum) {
        this.tableNum = tableNum;
    }

    /**
     * Sets whether a reminder has been sent for this reservation.
     *
     * @param reminderSent true if a reminder was sent, false otherwise
     */
    public void setReminderSent(boolean reminderSent) {
        this.reminderSent = reminderSent;
    }

    /**
     * Sets the timestamp indicating when the reminder was sent.
     * Can be null if no reminder has been sent.
     *
     * @param reminderSentAt time the reminder was sent
     */
    public void setReminderSentAt(Timestamp reminderSentAt) {
        this.reminderSentAt = reminderSentAt;
    }

    /**
     * Returns a string representation of this reservation.
     * Useful for debugging and logging.
     *
     * @return a string containing all reservation details
     */
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
                ", reminderSent=" + reminderSent +
                ", reminderSentAt=" + reminderSentAt +
                '}';
    }
}

