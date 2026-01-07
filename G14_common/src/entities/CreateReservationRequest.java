package entities;

import java.io.Serializable;
import java.sql.Timestamp;

public class CreateReservationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Integer subscriberId;   // null אם מזדמן
    private final String phone;           // חובה למזדמן / אפשר גם למנוי
    private final String email;           // כנ"ל
    private final Timestamp reservationTime;
    private final int numberOfDiners;

    public CreateReservationRequest(Integer subscriberId, String phone, String email,
                                    Timestamp reservationTime, int numberOfDiners) {
        this.subscriberId = subscriberId;
        this.phone = phone;
        this.email = email;
        this.reservationTime = reservationTime;
        this.numberOfDiners = numberOfDiners;
    }

    public Integer getSubscriberId() { return subscriberId; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public Timestamp getReservationTime() { return reservationTime; }
    public int getNumberOfDiners() { return numberOfDiners; }
}
