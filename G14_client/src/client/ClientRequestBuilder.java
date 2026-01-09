package client;

import entities.AvailableSlotsRequest;
import entities.ClientRequestType;
import entities.CreateReservationRequest;
import entities.Subscriber;

public class ClientRequestBuilder {

    public static Object getOrders() {
        return new Object[]{ ClientRequestType.GET_ORDERS };
    }

    public static Object updateOrder(int orderNumber, String newDate, Integer guests) {
        return new Object[]{ ClientRequestType.UPDATE_ORDER, orderNumber, newDate, guests };
    }

    public static Object getReservationInfo(int confCode) {
        return new Object[]{ ClientRequestType.GET_RESERVATION_INFO, confCode };
    }

    public static Object cancelReservation(int confCode) {
        return new Object[]{ ClientRequestType.DELETE_RESERVATION, confCode };
    }

    public static Object repLogin(String username, String password) {
        return new Object[]{ ClientRequestType.REP_LOGIN, username, password };
    }

    public static Object registerSubscriber(Subscriber s) {
        return new Object[]{ ClientRequestType.REGISTER_SUBSCRIBER, s.getName(), s.getPhone(), s.getEmail() };
    }

    public static Object subscriberLogin(int subscriberId) {
        return new Object[]{ ClientRequestType.SUBSCRIBER_LOGIN, subscriberId };
    }

    // ✅ זה מה ש-RepReservationsController צריך
    public static Object getActiveOrders() {
        return new Object[]{ ClientRequestType.GET_ACTIVE_RESERVATIONS };
    }
    
    public static Object[] getCurrentDiners() {
        return new Object[]{ ClientRequestType.GET_CURRENT_DINERS };
    }

    public static Object getAvailableSlots(AvailableSlotsRequest req) {
        return new Object[]{ ClientRequestType.GET_AVAILABLE_SLOTS, req };
    }

    public static Object createReservation(CreateReservationRequest req) {
        return new Object[]{ ClientRequestType.CREATE_RESERVATION, req };
    }
    
    public static Object[] getWaitlist() {
        return new Object[] { ClientRequestType.GET_WAITLIST };
    }
    
    public static Object[] getWaitlistByMonth(int year, int month) {
        return new Object[] { entities.ClientRequestType.GET_WAITLIST_BY_MONTH, year, month };
    }
    
    public static Object[] getSubscribers() {
        return new Object[] { entities.ClientRequestType.GET_SUBSCRIBERS };
    }
    
    public static Object[] getMembersReportByMonth(int year, int month) {
        return new Object[] { ClientRequestType.MANAGER_MEMBERS_REPORT_BY_MONTH, year, month };
    }

    public static Object[] getTimeReportByMonth(int year, int month) {
        return new Object[] { ClientRequestType.MANAGER_TIME_REPORT_BY_MONTH, year, month };
    }

}
