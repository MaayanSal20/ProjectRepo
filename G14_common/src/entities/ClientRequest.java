package entities;

import java.io.Serializable;

public class ClientRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type { GET_ORDERS, UPDATE_ORDER }

    private Type type;
    private Integer orderNumber;
    private String newDate;
    private Integer numberOfGuests;

    // Constructors
    public ClientRequest() {}

    public ClientRequest(Type type) {
        this.type = type;
    }

    // Getters / Setters
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Integer getOrderNumber() { return orderNumber; }
    public void setOrderNumber(Integer orderNumber) { this.orderNumber = orderNumber; }

    public String getNewDate() { return newDate; }
    public void setNewDate(String newDate) { this.newDate = newDate; }

    public Integer getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(Integer numberOfGuests) { this.numberOfGuests = numberOfGuests; }
}
