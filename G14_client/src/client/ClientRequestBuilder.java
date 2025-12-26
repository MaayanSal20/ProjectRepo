package client;

import entities.ClientRequest;

public class ClientRequestBuilder {

    public static ClientRequest getOrders() {
        return new ClientRequest(ClientRequest.Type.GET_ORDERS);
    }

    public static ClientRequest updateOrder(int orderNumber, String newDate, Integer guests) {
        ClientRequest r = new ClientRequest(ClientRequest.Type.UPDATE_ORDER);
        r.setOrderNumber(orderNumber);
        r.setNewDate(newDate);
        r.setNumberOfGuests(guests);
        return r;
    }
}
