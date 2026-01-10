package entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class SpecialHoursRow implements Serializable {

    private LocalDate date;
    private LocalTime open;
    private LocalTime close;
    private boolean closed;
    private String reason;

    public SpecialHoursRow(LocalDate date,
                           LocalTime open,
                           LocalTime close,
                           boolean closed,
                           String reason) {
        this.date = date;
        this.open = open;
        this.close = close;
        this.closed = closed;
        this.reason = reason;
    }

    public LocalDate getDate() { return date; }
    public LocalTime getOpen() { return open; }
    public LocalTime getClose() { return close; }
    public boolean isClosed() { return closed; }
    public String getReason() { return reason; }
}
