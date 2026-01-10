package entities;

import java.io.Serializable;
import java.time.LocalTime;

public class WeeklyHoursRow implements Serializable {

    private int dayOfWeek;   // 1..7
    private LocalTime open;
    private LocalTime close;
    private boolean closed;

    public WeeklyHoursRow(int dayOfWeek,
                          LocalTime open,
                          LocalTime close,
                          boolean closed) {
        this.dayOfWeek = dayOfWeek;
        this.open = open;
        this.close = close;
        this.closed = closed;
    }

    public int getDayOfWeek() { return dayOfWeek; }
    public LocalTime getOpen() { return open; }
    public LocalTime getClose() { return close; }
    public boolean isClosed() { return closed; }
}
