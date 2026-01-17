package entities;

import java.io.Serializable;


/**
 * Represents the possible statuses of a waitlist entry.
 * Used to describe the current state of a customer
 * waiting for or being assigned a table.
 */
public enum WaitlistStatus implements Serializable {

    /**
     * Customer has entered the waitlist and is waiting for a table.
     */
    WAITING,

    /**
     * A table has been offered and the system is waiting
     * for the customer to confirm arrival.
     */
    OFFERED,

    /**
     * Customer arrived within the allowed time window
     * and accepted the table.
     */
    ACCEPTED,

    /**
     * Customer did not arrive within the required time
     * and the offer expired.
     */
    EXPIRED,

    /**
     * Customer cancelled the waitlist entry manually.
     */
    CANCELLED,

    /**
     * Customer was seated immediately without waiting.
     */
    SEATED_NOW,

    /**
     * The waitlist request failed due to validation,
     * database, or system errors.
     */
    FAILED
}