package client;

import entities.AvailableSlotsRequest;
import entities.ClientRequestType;
import entities.CreateReservationRequest;
import entities.Subscriber;
import java.time.LocalDate;
import java.time.LocalTime;

public class ClientRequestBuilder {

    public static Object[] getAllReservations() {
        return new Object[]{ ClientRequestType.GET_RESERVATIONS };
    }
/*
    public static Object updateOrder(int orderNumber, String newDate, Integer guests) {
        return new Object[]{ ClientRequestType.UPDATE_ORDER, orderNumber, newDate, guests };
    }*/

    public static Object[] getReservationInfo(int confCode) {
        return new Object[]{ ClientRequestType.GET_RESERVATION_INFO, confCode };
    }

    public static Object[] cancelReservation(int confCode) {
        return new Object[]{ ClientRequestType.DELETE_RESERVATION, confCode };
    }

    public static Object[] repLogin(String username, String password) {
        return new Object[]{ ClientRequestType.REP_LOGIN, username, password };
    }

    public static Object[] registerSubscriber(Subscriber s) {
        return new Object[]{ ClientRequestType.REGISTER_SUBSCRIBER, s.getName(), s.getPhone(), s.getEmail() };
    }

    public static Object[] subscriberLogin(int subscriberId) {
        return new Object[]{ ClientRequestType.SUBSCRIBER_LOGIN, subscriberId };
    }

    // ✅ זה מה ש-RepReservationsController צריך
    public static Object[] getActiveOrders() {
        return new Object[]{ ClientRequestType.GET_ACTIVE_RESERVATIONS };
    }
    
    public static Object[] getCurrentDiners() {
        return new Object[]{ ClientRequestType.GET_CURRENT_DINERS };
    }

    public static Object[] getAvailableSlots(AvailableSlotsRequest req) {
        return new Object[]{ ClientRequestType.GET_AVAILABLE_SLOTS, req };
    }

    public static Object[] createReservation(CreateReservationRequest req) {
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
    
    public static Object[] getTables() {
        return new Object[]{ ClientRequestType.GET_TABLES };
    }

    public static Object[] addTable(int tableNum, int seats) {
        return new Object[]{ ClientRequestType.ADD_TABLE, tableNum, seats };
    }

    public static Object[] updateTableSeats(int tableNum, int seats) {
        return new Object[]{ ClientRequestType.UPDATE_TABLE_SEATS, tableNum, seats };
    }

    public static Object[] deactivateTable(int tableNum) {
        return new Object[]{ ClientRequestType.DEACTIVATE_TABLE, tableNum };
    }
    
    public static Object[] activateTable(int tableNum) {
        return new Object[]{ ClientRequestType.ACTIVATE_TABLE, tableNum };
    }

    public static Object[] getOpeningWeekly() {
        return new Object[]{ ClientRequestType.GET_OPENING_WEEKLY };
    }

    public static Object[] updateOpeningWeekly(int dayOfWeek, boolean isClosed, LocalTime open, LocalTime close) {
        return new Object[]{ ClientRequestType.UPDATE_OPENING_WEEKLY, dayOfWeek, isClosed, open, close };
    }

    public static Object[] getOpeningSpecial() {
        return new Object[]{ ClientRequestType.GET_OPENING_SPECIAL };
    }

    public static Object[] upsertOpeningSpecial(LocalDate date, boolean isClosed, LocalTime open, LocalTime close, String reason) {
        return new Object[]{ ClientRequestType.UPSERT_OPENING_SPECIAL, date, isClosed, open, close, reason };
    }

    public static Object[] deleteOpeningSpecial(LocalDate date) {
        return new Object[]{ ClientRequestType.DELETE_OPENING_SPECIAL, date };
    }
    
    public static Object[] payBill(int confCode, double amount, String paidBy) {
        return new Object[]{ ClientRequestType.PAY_BILL, new entities.PayBillRequest(confCode, amount, paidBy) };
    }
    public static Object[] getBillByConfCode(int confCode) {
        return new Object[]{ ClientRequestType.GET_BILL_BY_CONF_CODE, confCode };
    }
    
 // WAITLIST - Join
    public static Object[] joinWaitlistSubscriber(int subscriberId, int diners) {
        return new Object[]{ ClientRequestType.JOIN_WAITLIST_SUBSCRIBER, subscriberId, diners };
    }

    public static Object[] joinWaitlistNonSubscriber(String email, String phone, int diners) {
        return new Object[]{ ClientRequestType.JOIN_WAITLIST_NON_SUBSCRIBER, email, phone, diners };
    }

    // WAITLIST - Leave
    public static Object[] leaveWaitlistSubscriber(int subscriberId) {
        return new Object[]{ ClientRequestType.LEAVE_WAITLIST_SUBSCRIBER, subscriberId };
    }

    public static Object[] leaveWaitlistNonSubscriber(String email, String phone) {
        return new Object[]{ ClientRequestType.LEAVE_WAITLIST_NON_SUBSCRIBER, email, phone };
    }

}
