package entities;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Represents a request for available reservation time slots.
 * 
 * This object is sent from the client to the server in order to
 * query available reservation slots within a given time range
 * and for a specific number of diners.
 *
 */
public class AvailableSlotsRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 
     * Start time of the requested time range.
     */
    private final Timestamp from;

    /** 
     * End time of the requested time range.
     */
    private final Timestamp to;

    /** 
     * Number of diners for the reservation request.
     */
    private final int numberOfDiners;

    /**
     * Constructs a new {@code AvailableSlotsRequest}.
     *
     * @param from            the start time of the requested time range
     * @param to              the end time of the requested time range
     * @param numberOfDiners  the number of diners for the reservation
     */
    public AvailableSlotsRequest(Timestamp from, Timestamp to, int numberOfDiners) {
        this.from = from;
        this.to = to;
        this.numberOfDiners = numberOfDiners;
    }

    /**
     * Returns the start time of the requested time range.
     *
     * @return the start timestamp
     */
    public Timestamp getFrom() {
        return from;
    }

    /**
     * Returns the end time of the requested time range.
     *
     * @return the end timestamp
     */
    public Timestamp getTo() {
        return to;
    }

    /**
     * Returns the number of diners for the reservation request.
     *
     * @return the number of diners
     */
    public int getNumberOfDiners() {
        return numberOfDiners;
    }
}
