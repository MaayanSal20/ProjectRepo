package entities;

import java.io.Serializable;

public class ServerResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        ORDERS_LIST,
        UPDATE_SUCCESS,
        UPDATE_FAILED,
        ERROR
    }

    private final Type type;

    private java.util.ArrayList<Order> orders;
    private String message;
    private String errorDetails;

    private ServerResponse(Type type) {
        this.type = type;
    }

    // Factory methods

    public static ServerResponse orders(java.util.ArrayList<Order> orders) {
        ServerResponse r = new ServerResponse(Type.ORDERS_LIST);
        r.orders = orders;
        return r;
    }

    public static ServerResponse updateSuccess() {
        return new ServerResponse(Type.UPDATE_SUCCESS);
    }

    public static ServerResponse updateFailed(String details) {
        ServerResponse r = new ServerResponse(Type.UPDATE_FAILED);
        r.errorDetails = details;
        return r;
    }

    public static ServerResponse error(String message) {
        ServerResponse r = new ServerResponse(Type.ERROR);
        r.message = message;
        return r;
    }

    // Getters

    public Type getType() { return type; }
    public java.util.ArrayList<Order> getOrders() { return orders; }
    public String getMessage() { return message; }
    public String getErrorDetails() { return errorDetails; }
}
