package entities;

import java.io.Serializable;
import java.sql.Timestamp;

public class AvailableSlotsRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Timestamp from;
    private final Timestamp to;
    private final int numberOfDiners;

    public AvailableSlotsRequest(Timestamp from, Timestamp to, int numberOfDiners) {
        this.from = from;
        this.to = to;
        this.numberOfDiners = numberOfDiners;
    }

    public Timestamp getFrom() { return from; }
    public Timestamp getTo() { return to; }
    public int getNumberOfDiners() { return numberOfDiners; }
}
