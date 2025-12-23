package entities;

import java.io.Serializable;

public class ClientRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type { GET_ORDERS, UPDATE_ORDER }

    private final Type type;

    private Integer orderNumber;
    private String newDate;
    private Integer numberOfGuests;

    public ClientRequest(Type type) {
        this.type = type;
    }

    public static ClientRequest getOrders() {
        return new ClientRequest(Type.GET_ORDERS);
    }

    public static ClientRequest updateOrder(int orderNumber, String newDate, Integer numberOfGuests) {
        ClientRequest r = new ClientRequest(Type.UPDATE_ORDER);
        r.orderNumber = orderNumber;
        r.newDate = newDate;
        r.numberOfGuests = numberOfGuests;
        return r;
    }

    public Type getType() { return type; }
    public Integer getOrderNumber() { return orderNumber; }
    public String getNewDate() { return newDate; }
    public Integer getNumberOfGuests() { return numberOfGuests; }
}
