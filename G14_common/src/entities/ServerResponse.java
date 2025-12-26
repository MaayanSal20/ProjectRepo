package entities;

import java.io.Serializable;
import java.util.ArrayList;

public class ServerResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        ORDERS_LIST,
        UPDATE_SUCCESS,
        UPDATE_FAILED,
        ERROR
    }

    private Type type;
    private ArrayList<Order> orders;
    private String message;
    private String errorDetails;

    // Constructors
    public ServerResponse() {}

    public ServerResponse(Type type) {
        this.type = type;
    }

    // Getters / Setters
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public ArrayList<Order> getOrders() { return orders; }
    public void setOrders(ArrayList<Order> orders) { this.orders = orders; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getErrorDetails() { return errorDetails; }
    public void setErrorDetails(String errorDetails) { this.errorDetails = errorDetails; }
}
