package Server;

import java.util.ArrayList;
import entities.Order;
import entities.ServerResponse;

/**
 * ServerResponseBuilder is a helper class used on the server side
 * to create ServerResponse objects.
 *
 * The purpose of this class is to keep all server-side response
 * construction logic in one place, instead of spreading it across
 * the server code.
 *
 * Each method builds a specific type of response that will be sent
 * back to the client.
 */
public class ServerResponseBuilder {

	/**
     * Creates a response that contains a list of orders.
     *
     * This response is typically sent after a GET_ORDERS request
     * from the client.
     *
     * @param orders the list of orders retrieved from the database
     * @return a ServerResponse containing the orders list
     */
    public static ServerResponse orders(ArrayList<Order> orders) {
        ServerResponse r = new ServerResponse(ServerResponse.Type.ORDERS_LIST);
        r.setOrders(orders);
        return r;
    }

    /**
     * Creates a response that indicates a successful update operation.
     *
     * This response is sent when an order was updated successfully
     * in the database.
     *
     * @return a ServerResponse representing update success
     */
    public static ServerResponse updateSuccess() {
        return new ServerResponse(ServerResponse.Type.UPDATE_SUCCESS);
    }

    /**
     * Creates a response that indicates a failed update operation.
     *
     * This response includes a human-readable explanation of why
     * the update failed.
     *
     * @param details a description of the failure reason
     * @return a ServerResponse representing update failure
     */
    public static ServerResponse updateFailed(String details) {
        ServerResponse r = new ServerResponse(ServerResponse.Type.UPDATE_FAILED);
        r.setErrorDetails(details);
        return r;
    }

    /**
     * Creates a response that represents a general server error.
     *
     * This response is used when an unexpected error occurs
     * while processing a client request.
     *
     * @param message a human-readable error message
     * @return a ServerResponse representing a server error
     */
    public static ServerResponse error(String message) {
        ServerResponse r = new ServerResponse(ServerResponse.Type.ERROR);
        r.setMessage(message);
        return r;
    }
}
