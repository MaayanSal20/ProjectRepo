package Server;

import java.util.ArrayList;
import java.util.List;
import entities.Reservation;

import entities.Reservation;
import entities.ServerResponseType;

public class ServerResponseBuilder {

	public static Object orders(ArrayList<Reservation> orders) {
        return new Object[]{ ServerResponseType.RESERVATIONS_LIST_ALL, orders };
    }

    public static Object updateSuccess() {
        return new Object[]{ ServerResponseType.UPDATE_SUCCESS, "Updated successfully." };
    }

    public static Object updateFailed(String msg) {
        return new Object[]{ ServerResponseType.UPDATE_FAILED, msg };
    }

    public static Object loginFailed(String msg) {
        return new Object[] { ServerResponseType.LOGIN_FAILED, msg };
    }

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
        return new Object[] { ServerResponseType.RESERVATION_FOUND, order };
    }

    public static Object reservationNotFound(String msg) {
        return new Object[] { ServerResponseType.RESERVATION_NOT_FOUND, msg };
    }
    
    public static Object reservationNotAllowed(String msg) {
        return new Object[] { ServerResponseType.CANCELATION_NOT_ALLOWED, msg};
    }

    public static Object deleteSuccess(String str) {
        return new Object[]{ ServerResponseType.DELETE_SUCCESS, str };
    }

    public static Object[] reservations(ArrayList<Reservation> list) {
        return new Object[] { ServerResponseType.RESERVATIONS_LIST, list };
    }

    public static Object SubscriberLoginFailed(String msg) {
        return new Object[] { ServerResponseType.SUBSCRIBER_LOGIN_FAILED, msg };
    }

    public static Object SubscriberLoginSuccess(String msg) {
        return new Object[] { ServerResponseType.SUBSCRIBER_LOGIN_SUCCESS, msg };
    }

    // ✅ חדש: החזרת רשימת זמנים פנויים
    public static Object slotsList(List<String> slots) {
        return new Object[] { ServerResponseType.SLOTS_LIST, slots };
    }
    public static Object createSuccess(Reservation r, String notificationText) {
        return new Object[] { ServerResponseType.CREATE_SUCCESS, r, notificationText };
    }

    public static Object createFailed(String msg) {
        return new Object[] { ServerResponseType.CREATE_FAILED, msg };
    }
    
    public static Object createFailed(String msg, List<String> suggestedSlots) {
        return new Object[] { ServerResponseType.CREATE_FAILED, msg, suggestedSlots };
    }
    
    public static Object paySuccess(entities.PaymentReceipt receipt) {
        return new Object[]{ entities.ServerResponseType.PAY_SUCCESS, receipt };
    }

    public static Object payFailed(String msg) {
        return new Object[]{ entities.ServerResponseType.PAY_FAILED, msg };
    }


}
