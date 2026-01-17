package Server;

import java.util.ArrayList;

import java.util.List;
import entities.Reservation;

import entities.Reservation;
import entities.ServerResponseType;
import entities.WaitlistJoinResult;


/**
 * ServerResponseBuilder is a utility class used to create
 * standardized server responses.
 *
 * Each method returns an Object array whose first element
 * is a ServerResponseType and the remaining elements are
 * the response payload.
 */
public class ServerResponseBuilder {

	
	/**
	 * Builds a response containing a list of all reservations.
	 *
	 * @param orders list of reservations
	 * @return server response with reservations list
	 */
	public static Object orders(ArrayList<Reservation> orders) {
        return new Object[]{ ServerResponseType.RESERVATIONS_LIST_ALL, orders };
    }

	/**
	 * Builds a response indicating a successful update operation.
	 *
	 * @return server response for successful update
	 */
    public static Object updateSuccess() {
        return new Object[]{ ServerResponseType.UPDATE_SUCCESS, "Updated successfully." };
    }

    
    /**
     * Builds a response indicating a failed update operation.
     *
     * @param msg failure message
     * @return server response for failed update
     */
    public static Object updateFailed(String msg) {
        return new Object[]{ ServerResponseType.UPDATE_FAILED, msg };
    }

    
    /**
     * Builds a response indicating a failed login attempt.
     *
     * @param msg failure message
     * @return server response for login failure
     */
    public static Object loginFailed(String msg) {
        return new Object[] { ServerResponseType.LOGIN_FAILED, msg };
    }

    
    /**
     * Builds a response indicating successful subscriber registration.
     *
     * @param s registered subscriber
     * @return server response for successful registration
     */
    public static Object[] registerSuccess(entities.Subscriber s) {
        return new Object[] { entities.ServerResponseType.REGISTER_SUCCESS, s };
    }

    /**
     * Builds a response indicating a failed registration attempt.
     *
     * @param msg failure message
     * @return server response for registration failure
     */
    public static Object registerFailed(String msg) {
        return new Object[] { ServerResponseType.REGISTER_FAILED, msg };
    }

    /**
     * Builds a generic error response.
     *
     * @param message error description
     * @return server error response
     */
    public static Object error(String message) {
        return new Object[]{ ServerResponseType.ERROR, message };
    }

    /**
     * Builds a response containing a found reservation.
     *
     * @param order reservation that was found
     * @return server response with reservation data
     */
    public static Object reservationFound(Reservation order) {
        return new Object[] { ServerResponseType.RESERVATION_FOUND, order };
    }

    /**
     * Builds a response indicating that a reservation was not found.
     *
     * @param msg explanation message
     * @return server response for reservation not found
     */
    public static Object reservationNotFound(String msg) {
        return new Object[] { ServerResponseType.RESERVATION_NOT_FOUND, msg };
    }
    
    /**
     * Builds a response indicating that an operation on a reservation
     * is not allowed.
     *
     * @param msg explanation message
     * @return server response for disallowed reservation action
     */
    public static Object reservationNotAllowed(String msg) {
        return new Object[] { ServerResponseType.CANCELATION_NOT_ALLOWED, msg};
    }

    /**
     * Builds a response indicating a successful delete operation.
     *
     * @param str confirmation message
     * @return server response for successful deletion
     */
    public static Object deleteSuccess(String str) {
        return new Object[]{ ServerResponseType.DELETE_SUCCESS, str };
    }

    /**
     * Builds a response containing a list of reservations.
     *
     * @param list reservation list
     * @return server response with reservation list
     */
    public static Object[] reservations(ArrayList<Reservation> list) {
        return new Object[] { ServerResponseType.RESERVATIONS_LIST, list };
    }

    /**
     * Builds a response indicating a failed subscriber login.
     *
     * @param msg failure message
     * @return server response for subscriber login failure
     */
    public static Object SubscriberLoginFailed(String msg) {
        return new Object[] { ServerResponseType.SUBSCRIBER_LOGIN_FAILED, msg };
    }

    /**
     * Builds a response indicating a successful subscriber login.
     *
     * @param msg success message
     * @return server response for subscriber login success
     */
    public static Object SubscriberLoginSuccess(String msg) {
        return new Object[] { ServerResponseType.SUBSCRIBER_LOGIN_SUCCESS, msg };
    }

    /**
     * Builds a response containing a list of available time slots.
     *
     * @param slots list of available slots
     * @return server response with slots list
     */
    public static Object slotsList(List<String> slots) {
        return new Object[] { ServerResponseType.SLOTS_LIST, slots };
    }
    
    /**
     * Builds a response indicating successful reservation creation.
     *
     * @param r created reservation
     * @param notificationText message to be sent to the customer
     * @return server response for successful creation
     */
    public static Object createSuccess(Reservation r, String notificationText) {
        return new Object[] { ServerResponseType.CREATE_SUCCESS, r, notificationText };
    }

    
    /**
     * Builds a response indicating failed reservation creation.
     *
     * @param msg failure message
     * @return server response for creation failure
     */
    public static Object createFailed(String msg) {
        return new Object[] { ServerResponseType.CREATE_FAILED, msg };
    }
    
    /**
     * Builds a response indicating failed reservation creation,
     * including suggested alternative time slots.
     *
     * @param msg failure message
     * @param suggestedSlots alternative available slots
     * @return server response for creation failure with suggestions
     */
    public static Object createFailed(String msg, List<String> suggestedSlots) {
        return new Object[] { ServerResponseType.CREATE_FAILED, msg, suggestedSlots };
    }
    
    /**
     * Builds a response indicating successful payment.
     *
     * @param receipt payment receipt
     * @return server response for successful payment
     */
    public static Object paySuccess(entities.PaymentReceipt receipt) {
        return new Object[]{ entities.ServerResponseType.PAY_SUCCESS, receipt };
    }

    /**
     * Builds a response indicating failed payment.
     *
     * @param msg failure message
     * @return server response for payment failure
     */
    public static Object payFailed(String msg) {
        return new Object[]{ entities.ServerResponseType.PAY_FAILED, msg };
    }

    /**
     * Builds a response indicating successful waitlist registration.
     *
     * @param res waitlist result data
     * @return server response for waitlist success
     */
    public static Object waitlistSuccess(WaitlistJoinResult res) {
        return new Object[]{ ServerResponseType.WAITINGLIST_SUCCESS, res };
    }

    /**
     * Builds a response indicating a waitlist operation error.
     *
     * @param res waitlist result data
     * @return server response for waitlist error
     */
    public static Object waitlistError(WaitlistJoinResult res) {
        return new Object[]{ ServerResponseType.WAITINGLIST_ERROR, res };
    }

    /**
     * Builds a waitlist success response with a message only.
     *
     * @param msg success message
     * @return server response for waitlist success
     */
    public static Object waitlistSuccessMsg(String msg) {
        return new Object[]{ ServerResponseType.WAITINGLIST_SUCCESS, msg };
    }

    /**
     * Builds a waitlist error response with a message only.
     *
     * @param msg error message
     * @return server response for waitlist error
     */
    public static Object waitlistErrorMsg(String msg) {
        return new Object[]{ ServerResponseType.WAITINGLIST_ERROR, msg };
    }

}
