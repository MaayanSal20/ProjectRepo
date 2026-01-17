package entities;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Represents a request to create a new reservation.
 * This object is sent from the client to the server and contains
 * all required information for creating a reservation, whether
 * for a subscriber or a non-subscriber.
 */
public class CreateReservationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Subscriber identifier.
     * Null if the reservation is made by a non-subscriber.
     */
    private final Integer subscriberId;   
    /**
     * Contact phone number.
     * Mandatory for non-subscribers and optional for subscribers.
     */
    private final String phone;          
    /**
     * Contact email address.
     * Mandatory for non-subscribers and optional for subscribers.
     */
    private final String email;          
    /**
     * Date and time of the reservation.
     */
    private final Timestamp reservationTime;

    /**
     * Number of diners for the reservation.
     */
    private final int numberOfDiners;

    /**
     * Constructs a new CreateReservationRequest.
     *
     * @param subscriberId subscriber identifier, or null for non-subscribers
     * @param phone contact phone number
     * @param email contact email address
     * @param reservationTime date and time of the reservation
     * @param numberOfDiners number of diners for the reservation
     */
    public CreateReservationRequest(Integer subscriberId, String phone, String email,
                                    Timestamp reservationTime, int numberOfDiners) {
        this.subscriberId = subscriberId;
        this.phone = phone;
        this.email = email;
        this.reservationTime = reservationTime;
        this.numberOfDiners = numberOfDiners;
    }

    /**
     * Returns the subscriber identifier.
     *
     * @return the subscriber ID, or null if non-subscriber
     */
    public Integer getSubscriberId() { return subscriberId; }

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
    public int getNumberOfDiners() { return numberOfDiners; }
}
