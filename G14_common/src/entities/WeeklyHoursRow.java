package entities;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * Represents the regular weekly opening hours of the restaurant.
 * Each instance describes the opening status for a specific day of the week.
 */
public class WeeklyHoursRow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Day of the week (1..7).
     * Convention can follow ISO-8601 (1 = Monday, 7 = Sunday).
     */
    private int dayOfWeek;

    /**
     * Opening time for the given day.
     * May be null if the restaurant is closed.
     */
    private LocalTime open;

    /**
     * Closing time for the given day.
     * May be null if the restaurant is closed.
     */
    private LocalTime close;

    /**
     * Indicates whether the restaurant is closed on this day.
     */
    private boolean closed;

    /**
     * Creates a new weekly hours row.
     *
     * @param dayOfWeek day of the week (1..7)
     * @param open opening time
     * @param close closing time
     * @param closed whether the restaurant is closed on this day
     */
    public WeeklyHoursRow(int dayOfWeek,
                          LocalTime open,
                          LocalTime close,
                          boolean closed) {
        this.dayOfWeek = dayOfWeek;
        this.open = open;
        this.close = close;
        this.closed = closed;
    }

    /**
     * Returns the day of the week this row represents.
     *
     * @return day of week as an integer (1 = Monday, 7 = Sunday)
     */
    public int getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * Returns the opening time for this day.
     *
     * @return opening time, or null if the restaurant is closed
     */
    public LocalTime getOpen() {
        return open;
    }

    /**
     * Returns the closing time for this day.
     *
     * @return closing time, or null if the restaurant is closed
     */
    public LocalTime getClose() {
        return close;
    }

    /**
     * Indicates whether the restaurant is closed on this day.
     *
     * @return true if closed, false otherwise
     */
    public boolean isClosed() {
        return closed;
    }

}
