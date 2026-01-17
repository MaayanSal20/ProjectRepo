package entities;

import java.io.Serializable;

/**
 * Represents a restaurant table.
 * This class contains information about a table, including
 * its size, availability status, and whether it is currently occupied.
 */
public class RestaurantTable implements Serializable {

    /**
     * Table number.
     */
    private int tableNum;

    /**
     * Number of seats at the table.
     */
    private int seats;

    /**
     * Indicates whether the table is active and available for use.
     */
    private boolean active;

    /**
     * Indicates whether the table is currently occupied.
     */
    private boolean isOccupied;

    /**
     * Constructs a new RestaurantTable object.
     *
     * @param tableNum table number
     * @param seats number of seats at the table
     * @param active indicates whether the table is active
     * @param isOccupied indicates whether the table is occupied
     */
    public RestaurantTable(int tableNum, int seats, boolean active, boolean isOccupied) {
        this.tableNum = tableNum;
        this.seats = seats;
        this.active = active;
        this.isOccupied = isOccupied;
    }

    /**
     * Returns the table number.
     *
     * @return the table number
     */
    public int getTableNum() { return tableNum; }

    /**
     * Returns the number of seats.
     *
     * @return the number of seats
     */
    public int getSeats() { return seats; }

    /**
     * Indicates whether the table is active.
     *
     * @return true if the table is active, false otherwise
     */
    public boolean isActive() { return active; }

    /**
     * Indicates whether the table is currently occupied.
     *
     * @return true if the table is occupied, false otherwise
     */
    public boolean isOccupied() { return isOccupied; }
}
