package Server;

import java.util.ArrayList;
import entities.Order;
import entities.ServerResponseType;

public class ServerResponseBuilder {

    public static Object orders(ArrayList<Order> orders) {
        return new Object[]{ ServerResponseType.ORDERS_LIST, orders };
    }

    public static Object updateSuccess() {
        return new Object[]{ ServerResponseType.UPDATE_SUCCESS };
    }

    public static Object updateFailed(String details) {
        return new Object[]{ ServerResponseType.UPDATE_FAILED, details };
    }

    public static Object[] loginSuccess() {
        return new Object[] { ServerResponseType.LOGIN_SUCCESS };
    }

    public static Object[] loginFailed(String msg) {
        return new Object[] { ServerResponseType.LOGIN_FAILED, msg };
    }

    public static Object[] registerSuccess(int subscriberId) {
        return new Object[] { ServerResponseType.REGISTER_SUCCESS, subscriberId };
    }

    public static Object[] registerFailed(String msg) {
        return new Object[] { ServerResponseType.REGISTER_FAILED, msg };
    }
    
    public static Object error(String message) {
        return new Object[]{ ServerResponseType.ERROR, message };
    }
    
    
    public static ServerResponse deleteSuccess() {
    	 return new ServerResponse(ServerResponse.Type.DELETE_SUCCESS);
    }
}
