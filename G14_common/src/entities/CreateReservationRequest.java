package entities;

import java.io.Serializable;
import java.sql.Timestamp;

public class CreateReservationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Timestamp reservationTime;
    private final int numberOfDiners;
    private final int customerId;

    public CreateReservationRequest(Timestamp reservationTime, int numberOfDiners, int customerId) {
        this.reservationTime = reservationTime;
        this.numberOfDiners = numberOfDiners;
        this.customerId = customerId;
    }

    public Timestamp getReservationTime() { return reservationTime; }
    public int getNumberOfDiners() { return numberOfDiners; }
    public int getCustomerId() { return customerId; }
}