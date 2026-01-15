package entities;

import java.io.Serializable;

public class RestaurantTable implements Serializable {
    private int tableNum;
    private int seats;
    private boolean active;
    private boolean isOccupied;

    public RestaurantTable(int tableNum, int seats, boolean active, boolean isOccupied) {
        this.tableNum = tableNum;
        this.seats = seats;
        this.active = active;
        this.isOccupied = isOccupied;
    }
    public int getTableNum() { return tableNum; }
    public int getSeats() { return seats; }
    public boolean isActive() { return active; }
    public boolean isOccupied() { return isOccupied; }
}
