package entities;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Represents a single entry in the restaurant waitlist.
 * This class is used to transfer waitlist data between
 * the server and the client.
 */
public class WaitlistRow implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Confirmation code assigned to the waitlist entry.
     */
    private int confCode;

    /**
     * Customer identifier.
     */
    private int customerId;

    /**
     * Customer email address.
     */
    private String email;

    /**
     * Timestamp indicating when the customer entered the waitlist queue.
     */
    private Timestamp timeEnterQueue;

    /**
     * Number of diners in the party.
     */
    private int numOfDiners;

    /**
     * Customer phone number.
     */
    private String phone;

    /**
     * Constructs a new WaitlistRow.
     *
     * @param confCode confirmation code for the waitlist entry
     * @param timeEnterQueue time the customer entered the queue
     * @param numOfDiners number of diners
     * @param customerId customer identifier
     * @param phone customer phone number
     * @param email customer email address
     */
    public WaitlistRow(int confCode,
                       Timestamp timeEnterQueue,
                       int numOfDiners,
                       int customerId,
                       String phone,
                       String email) {
        this.confCode = confCode;
        this.customerId = customerId;
        this.email = email;
        this.timeEnterQueue = timeEnterQueue;
        this.numOfDiners = numOfDiners;
        this.phone = phone;
    }

    /**
     * Returns the confirmation code.
     *
     * @return confirmation code
     */
    public int getConfCode() { return confCode; }

    /**
     * Returns the customer ID.
     *
     * @return customer ID
     */
    public int getCustomerId() { return customerId; }

    /**
     * Returns the customer's email.
     *
     * @return email address
     */
    public String getEmail() { return email; }

    /**
     * Returns the time the customer entered the waitlist.
     *
     * @return queue entry time
     */
    public Timestamp getTimeEnterQueue() { return timeEnterQueue; }

    /**
     * Returns the customer's phone number.
     *
     * @return phone number
     */
    public String getPhone() { return phone; }

    /**
     * Returns the number of diners.
     *
     * @return number of diners
     */
    public int getNumOfDiners() { return numOfDiners; }
}
