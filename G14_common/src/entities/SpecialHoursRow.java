package entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents special operating hours for a specific date.
 * This class is used to define exceptions to regular opening hours,
 * such as holidays, special events, or full-day closures.
 */
public class SpecialHoursRow implements Serializable {

    /**
     * The date for which the special hours apply.
     */
    private LocalDate date;

    /**
     * Opening time on the specified date.
     */
    private LocalTime open;

    /**
     * Closing time on the specified date.
     */
    private LocalTime close;

    /**
     * Indicates whether the restaurant is closed on this date.
     */
    private boolean closed;

    /**
     * Reason for the special hours or closure.
     */
    private String reason;

    /**
     * Constructs a new SpecialHoursRow object.
     *
     * @param date the date for the special hours
     * @param open opening time
     * @param close closing time
     * @param closed indicates whether the restaurant is closed
     * @param reason reason for the special hours or closure
     */
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

    /**
     * Returns the date.
     *
     * @return the date
     */
    public LocalDate getDate() { return date; }

    /**
     * Returns the opening time.
     *
     * @return the opening time
     */
    public LocalTime getOpen() { return open; }

    /**
     * Returns the closing time.
     *
     * @return the closing time
     */
    public LocalTime getClose() { return close; }

    /**
     * Indicates whether the restaurant is closed on this date.
     *
     * @return true if closed, false otherwise
     */
    public boolean isClosed() { return closed; }

    /**
     * Returns the reason for the special hours or closure.
     *
     * @return the reason
     */
    public String getReason() { return reason; }
}
