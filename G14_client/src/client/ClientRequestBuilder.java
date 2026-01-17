package client;

import entities.AvailableSlotsRequest;
import entities.ClientRequestType;
import entities.CreateReservationRequest;
import entities.Subscriber;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Builds request objects sent from the client to the server.
 *
 * Each method creates an Object array that represents a specific
 * client request type and its required parameters.
 */
public class ClientRequestBuilder {
 
	 /**
     * Creates a request to fetch all reservations.
     *
     * @return request object for retrieving all reservations
     */
    public static Object[] getAllReservations() {
        return new Object[]{ ClientRequestType.GET_RESERVATIONS };
    }

    /**
     * Creates a request to fetch reservation details by confirmation code.
     *
     * @param confCode reservation confirmation code
     * @return request object for retrieving reservation information
     */
    public static Object[] getReservationInfo(int confCode) {
        return new Object[]{ ClientRequestType.GET_RESERVATION_INFO, confCode };
    }

    /**
     * Creates a request to cancel a reservation.
     *
     * @param confCode reservation confirmation code
     * @return request object for deleting a reservation
     */
    public static Object[] cancelReservation(int confCode) {
        return new Object[]{ ClientRequestType.DELETE_RESERVATION, confCode };
    }

    /**
     * Creates a representative login request.
     *
     * @param username representative username
     * @param password representative password
     * @return login request object
     */

    public static Object[] repLogin(String username, String password) {
        return new Object[]{ ClientRequestType.REP_LOGIN, username, password };
    }

    /**
     * Creates a request to register a new subscriber.
     *
     * @param s subscriber details
     * @return subscriber registration request object
     */
    public static Object[] registerSubscriber(Subscriber s) {
        return new Object[]{ ClientRequestType.REGISTER_SUBSCRIBER, s.getName(), s.getPhone(), s.getEmail() };
    }

    /**
     * Creates a subscriber login request.
     *
     * @param subscriberId subscriber identifier
     * @return subscriber login request object
     */
    public static Object[] subscriberLogin(int subscriberId) {
        return new Object[]{ ClientRequestType.SUBSCRIBER_LOGIN, subscriberId };
    }

    /**
     * Creates a request to fetch all active reservations.
     *
     * @return request object for active reservations
     */
    public static Object[] getActiveOrders() {
        return new Object[]{ ClientRequestType.GET_ACTIVE_RESERVATIONS };
    }
    
    /**
     * Creates a request to fetch the current number of diners.
     *
     * @return request object for current diners
     */
    public static Object[] getCurrentDiners() {
        return new Object[]{ ClientRequestType.GET_CURRENT_DINERS };
    }

    /**
     * Creates a request to retrieve available reservation time slots.
     *
     * @param req request parameters for available slots
     * @return available slots request object
     */
    public static Object[] getAvailableSlots(AvailableSlotsRequest req) {
        return new Object[]{ ClientRequestType.GET_AVAILABLE_SLOTS, req };
    }

    /**
     * Creates a request to create a new reservation.
     *
     * @param req reservation creation details
     * @return create reservation request object
     */
    public static Object[] createReservation(CreateReservationRequest req) {
        return new Object[]{ ClientRequestType.CREATE_RESERVATION, req };
    }
    
    /**
     * Creates a request to retrieve the waitlist.
     *
     * @return waitlist request object
     */
    public static Object[] getWaitlist() {
        return new Object[] { ClientRequestType.GET_WAITLIST };
    }
    
    /**
     * Creates a request to retrieve the waitlist by month.
     *
     * @param year requested year
     * @param month requested month
     * @return waitlist by month request object
     */
    public static Object[] getWaitlistByMonth(int year, int month) {
        return new Object[] { entities.ClientRequestType.GET_WAITLIST_BY_MONTH, year, month };
    }

    /**
     * Creates a request to retrieve all subscribers.
     *
     * @return subscribers request object
     */
    public static Object[] getSubscribers() {
        return new Object[] { entities.ClientRequestType.GET_SUBSCRIBERS };
    }

    /**
     * Creates a request to retrieve the members report for a given month.
     *
     * @param year report year
     * @param month report month
     * @return members report request object
     */
    public static Object[] getMembersReportByMonth(int year, int month) {
        return new Object[] { ClientRequestType.MANAGER_MEMBERS_REPORT_BY_MONTH, year, month };
    }

    /**
     * Creates a request to retrieve the time report for a given month.
     *
     * @param year report year
     * @param month report month
     * @return time report request object
     */
    public static Object[] getTimeReportByMonth(int year, int month) {
        return new Object[] { ClientRequestType.MANAGER_TIME_REPORT_BY_MONTH, year, month };
    }

    /**
     * Creates a request to retrieve all restaurant tables.
     *
     * @return tables request object
     */
    public static Object[] getTables() {
        return new Object[]{ ClientRequestType.GET_TABLES };
    }

    /**
     * Creates a request to add a new table.
     *
     * @param tableNum table number
     * @param seats number of seats
     * @return add table request object
     */
    public static Object[] addTable(int tableNum, int seats) {
        return new Object[]{ ClientRequestType.ADD_TABLE, tableNum, seats };
    }

    /**
     * Creates a request to update the number of seats in a table.
     *
     * @param tableNum table number
     * @param seats updated seat count
     * @return update table seats request object
     */
    public static Object[] updateTableSeats(int tableNum, int seats) {
        return new Object[]{ ClientRequestType.UPDATE_TABLE_SEATS, tableNum, seats };
    }

    /**
     * Creates a request to deactivate a table.
     *
     * @param tableNum table number
     * @return deactivate table request object
     */
    public static Object[] deactivateTable(int tableNum) {
        return new Object[]{ ClientRequestType.DEACTIVATE_TABLE, tableNum };
    }

    /**
     * Creates a request to activate a table.
     *
     * @param tableNum table number
     * @return activate table request object
     */
    public static Object[] activateTable(int tableNum) {
        return new Object[]{ ClientRequestType.ACTIVATE_TABLE, tableNum };
    }

    /**
     * Creates a request to retrieve weekly opening hours.
     *
     * @return weekly opening hours request object
     */
    public static Object[] getOpeningWeekly() {
        return new Object[]{ ClientRequestType.GET_OPENING_WEEKLY };
    }

    /**
     * Creates a request to update weekly opening hours.
     *
     * @param dayOfWeek day of week (1-7)
     * @param isClosed indicates if the day is closed
     * @param open opening time
     * @param close closing time
     * @return update weekly opening hours request object
     */
    public static Object[] updateOpeningWeekly(int dayOfWeek, boolean isClosed, LocalTime open, LocalTime close) {
        return new Object[]{ ClientRequestType.UPDATE_OPENING_WEEKLY, dayOfWeek, isClosed, open, close };
    }

    /**
     * Creates a request to retrieve special opening hours.
     *
     * @return special opening hours request object
     */
    public static Object[] getOpeningSpecial() {
        return new Object[]{ ClientRequestType.GET_OPENING_SPECIAL };
    }

    /**
     * Creates a request to insert or update special opening hours.
     *
     * @param date specific date
     * @param isClosed indicates if the date is closed
     * @param open opening time
     * @param close closing time
     * @param reason optional reason
     * @return upsert special opening hours request object
     */
    public static Object[] upsertOpeningSpecial(LocalDate date, boolean isClosed, LocalTime open, LocalTime close, String reason) {
        return new Object[]{ ClientRequestType.UPSERT_OPENING_SPECIAL, date, isClosed, open, close, reason };
    }

    /**
     * Creates a request to delete special opening hours.
     *
     * @param date date to delete
     * @return delete special opening hours request object
     */
    public static Object[] deleteOpeningSpecial(LocalDate date) {
        return new Object[]{ ClientRequestType.DELETE_OPENING_SPECIAL, date };
    }
    
    /**
     * Creates a request to pay a bill.
     *
     * @param confCode reservation confirmation code
     * @param amount payment amount
     * @param paidBy payment method or payer
     * @return pay bill request object
     */
    public static Object[] payBill(int confCode, double amount, String paidBy) {
        return new Object[]{ ClientRequestType.PAY_BILL, new entities.PayBillRequest(confCode, amount, paidBy) };
    }
    
    /**
     * Creates a request to retrieve a bill by confirmation code.
     *
     * @param confCode reservation confirmation code
     * @return get bill request object
     */
    public static Object[] getBillByConfCode(int confCode) {
        return new Object[]{ ClientRequestType.GET_BILL_BY_CONF_CODE, confCode };
    }
    
    /**
     * Creates a request for a subscriber to join the waitlist.
     *
     * @param subscriberId subscriber identifier
     * @param diners number of diners
     * @return join waitlist request object
     */
    public static Object[] joinWaitlistSubscriber(int subscriberId, int diners) {
        return new Object[]{ ClientRequestType.JOIN_WAITLIST_SUBSCRIBER, subscriberId, diners };
    }

    /**
     * Creates a request for a non-subscriber to join the waitlist.
     *
     * @param email email address
     * @param phone phone number
     * @param diners number of diners
     * @return join waitlist request object
     */
    public static Object[] joinWaitlistNonSubscriber(String email, String phone, int diners) {
        return new Object[]{ ClientRequestType.JOIN_WAITLIST_NON_SUBSCRIBER, email, phone, diners };
    }

    /**
     * Creates a request for a subscriber to leave the waitlist.
     *
     * @param subscriberId subscriber identifier
     * @return leave waitlist request object
     */
    public static Object[] leaveWaitlistSubscriber(int subscriberId) {
        return new Object[]{ ClientRequestType.LEAVE_WAITLIST_SUBSCRIBER, subscriberId };
    }

    /**
     * Creates a request for a non-subscriber to leave the waitlist.
     *
     * @param email email address
     * @param phone phone number
     * @return leave waitlist request object
     */
    public static Object[] leaveWaitlistNonSubscriber(String email, String phone) {
        return new Object[]{ ClientRequestType.LEAVE_WAITLIST_NON_SUBSCRIBER, email, phone };
        
    }
    
    /**
     * Creates a request to recover a forgotten confirmation code.
     *
     * @param phone phone number
     * @param email email address
     * @return forgot confirmation code request object
     */
    public static Object[] forgotConfirmationCode(String phone, String email) {
        return new Object[]{ ClientRequestType.FORGOT_CONFIRMATION_CODE,
                new entities.ForgotConfirmationCodeRequest(phone, email) };
    }
    
    /**
     * Builds a request to run the monthly reports snapshot.
     *
     * @param year snapshot year
     * @param month snapshot month
     * @return request payload for the server
     */
    public static Object[] runMonthlySnapshot(int year, int month) {
        return new Object[]{ ClientRequestType.RUN_MONTHLY_REPORTS_SNAPSHOT, year, month };
    }
    
    /**
     * Builds a request to fetch waitlist ratio by hour.
     *
     * @param year report year
     * @param month report month
     * @return request payload for the server
     */
    public static Object[] getWaitlistRatioByHour(int year, int month) {
        return new Object[] { ClientRequestType.MANAGER_WAITLIST_RATIO_BY_HOUR, year, month };
    }


}
