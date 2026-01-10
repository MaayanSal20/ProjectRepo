package entities;

import java.io.Serializable;

public class RestaurantTable implements Serializable {
    private int tableNum;
    private int seats;
    private boolean active;

    public RestaurantTable(int tableNum, int seats, boolean active) {
        this.tableNum = tableNum;
        this.seats = seats;
        this.active = active;
    }
    public int getTableNum() { return tableNum; }
    public int getSeats() { return seats; }
    public boolean isActive() { return active; }
}
