package entities;

import java.io.Serializable;

public enum ServerResponseType implements Serializable {
    // General / Errors
    ERROR,

    // Orders / Reservations
    RESERVATIONS_LIST_ALL, //get the reservations
    RESERVATIONS_LIST, // for actions
    RESERVATION_FOUND,
    RESERVATION_NOT_FOUND,
    CANCELATION_NOT_ALLOWED,
    UPDATE_SUCCESS,
    UPDATE_FAILED,
    DELETE_SUCCESS,
   

    // Representative / Manager login
    LOGIN_SUCCESS,
    LOGIN_FAILED,

    // Subscriber
    REGISTER_SUCCESS,
    REGISTER_FAILED,
    SUBSCRIBER_LOGIN_SUCCESS,
    SUBSCRIBER_LOGIN_FAILED,

    // Representative views (we'll implement server-side next)
    WAITLIST_LIST,
    CURRENT_DINERS_LIST,
    SUBSCRIBERS_LIST,
    TIME_REPORT_DATA,
    MEMBERS_REPORT_DATA,
    TABLES_LIST,
    WEEKLY_HOURS_LIST,
    SPECIAL_HOURS_LIST,
    TABLE_UPDATE_SUCCESS,
    HOURS_UPDATE_SUCCESS,

    // Customer actions (we'll implement later)
    SLOTS_LIST,
    CREATE_SUCCESS,
    CREATE_FAILED,
    WAITLIST_SUCCESS,
    WAITLIST_FAILED,
    PAY_SUCCESS,
    PAY_FAILED
}
