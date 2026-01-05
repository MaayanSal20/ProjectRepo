package Server;

import java.util.ArrayList;
import entities.Reservation;
import entities.ServerResponseType;
import entities.Subscriber;

public class ServerResponseBuilder {

    public static Object orders(ArrayList<Reservation> orders) {
        return new Object[]{ ServerResponseType.ORDERS_LIST, orders };
    }

    public static Object updateSuccess() {
        return new Object[]{ ServerResponseType.UPDATE_SUCCESS };
    }

    public static Object updateFailed(String details) {
        return new Object[]{ ServerResponseType.UPDATE_FAILED, details };
    }

    public static Object loginSuccess() {
        return new Object[] { ServerResponseType.LOGIN_SUCCESS };
    }

    public static Object loginFailed(String msg) {
        return new Object[] { ServerResponseType.LOGIN_FAILED, msg };
    }

    /*public static Object registerSuccess(int subscriberId) {
        return new Object[] { ServerResponseType.REGISTER_SUCCESS, subscriberId };
    }*/
    
    public static Object[] registerSuccess(entities.Subscriber s) {
        return new Object[] { entities.ServerResponseType.REGISTER_SUCCESS, s };
    }

    public static Object registerFailed(String msg) {
        return new Object[] { ServerResponseType.REGISTER_FAILED, msg };
    }
    
    public static Object error(String message) {
        return new Object[]{ ServerResponseType.ERROR, message };
    }
    
    public static Object reservationFound(Reservation order) {
        return new Object[] {
            ServerResponseType.RESERVATION_FOUND,
            order
        };
    }

    public static Object reservationNotFound(String msg) {
        return new Object[] {
            ServerResponseType.RESERVATION_NOT_FOUND,
            msg
        };
    }

    
    public static Object deleteSuccess(String str) {
        return new Object[]{ ServerResponseType.DELETE_SUCCESS, str };
    }
    
    public static Object deleteFailed(String str) {
        return new Object[]{ ServerResponseType.DELETE_FAILED, str };
    }
    
    public static Object[] reservations(ArrayList<Reservation> list) {
        return new Object[] { ServerResponseType.RESERVATIONS_LIST, list };
    }
    
   
}
