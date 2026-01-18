package entities;

import java.io.Serializable;

/**
 * Enumerates all possible response types that the server can return
 * to the client. Each value represents the result of a specific
 * operation or request handled by the server.
 */
public enum ServerResponseType implements Serializable {

    // ================= General / Errors =================

    /** Generic error response */
    ERROR,

    /** Generic success response */
    SUCCESS,

    // ================= Reservations =================

    /** Returns a list of all reservations */
    RESERVATIONS_LIST_ALL,

    /** Returns a reservations list for specific actions */
    RESERVATIONS_LIST,

    /** Reservation was found successfully */
    RESERVATION_FOUND,

    /** Reservation was not found */
    RESERVATION_NOT_FOUND,

    /** Reservation cancellation is not allowed */
    CANCELATION_NOT_ALLOWED,

    /** Update operation completed successfully */
    UPDATE_SUCCESS,

    /** Update operation failed */
    UPDATE_FAILED,

    /** Deletion operation completed successfully */
    DELETE_SUCCESS,

    // ================= Subscriber =================

    /** Returns a list of reservations for a subscriber */
    SUBSCRIBER_RESERVATIONS_LIST,

    /** Returns personal details of a subscriber */
    SUBSCRIBER_PERSONAL_DETAILS,

    /** Indicates subscriber personal details were updated successfully */
    SUBSCRIBER_PERSONAL_DETAILS_UPDATED,

    // ================= Waiting List – Customer =================

    /** A waitlist offer was successfully created */
    WAITLIST_OFFER_CREATED,

    /** Informational response */
    INFO,

    /** Successfully joined the waiting list */
    WAITINGLIST_SUCCESS,

    /** Error occurred while handling the waiting list */
    WAITINGLIST_ERROR,

    /** Confirmation code was found */
    CONFIRMATION_CODE_FOUND,

    /** Confirmation code was not found */
    CONFIRMATION_CODE_NOT_FOUND,

    /** Confirmation code challenge was generated */
    CONF_CODE_CHALLENGE,

    /** Confirmation code challenge is empty */
    CONF_CODE_CHALLENGE_EMPTY,

    // ================= Representative / Manager Login =================

    /** Login was successful */
    LOGIN_SUCCESS,

    /** Login failed */
    LOGIN_FAILED,

    // ================= Subscriber Authentication =================

    /** Subscriber registration succeeded */
    REGISTER_SUCCESS,

    /** Subscriber registration failed */
    REGISTER_FAILED,

    /** Subscriber login succeeded */
    SUBSCRIBER_LOGIN_SUCCESS,

    /** Subscriber login failed */
    SUBSCRIBER_LOGIN_FAILED,

    // ================= Representative / Manager Views =================

    /** Returns the waitlist */
    WAITLIST_LIST,

    /** Returns the list of current diners */
    CURRENT_DINERS_LIST,

    /** Returns the list of subscribers */
    SUBSCRIBERS_LIST,

    /** Returns time-based report data */
    TIME_REPORT_DATA,

    /** Returns members activity report data */
    MEMBERS_REPORT_DATA,

    /** Returns the list of restaurant tables */
    TABLES_LIST,

    /** Returns weekly opening hours */
    WEEKLY_HOURS_LIST,

    /** Returns special opening hours */
    SPECIAL_HOURS_LIST,

    /** Table update operation succeeded */
    TABLE_UPDATE_SUCCESS,

    /** Opening hours update operation succeeded */
    HOURS_UPDATE_SUCCESS,

    /** Monthly reports snapshot completed successfully */
    MONTHLY_SNAPSHOT_OK,

    /** Monthly reports snapshot failed */
    MONTHLY_SNAPSHOT_FAILED,

    /** Returns waitlist ratio grouped by hour */
    WAITLIST_RATIO_BY_HOUR_DATA,


    // ================= Customer Actions =================


    /** Returns available reservation slots */
    SLOTS_LIST,

    /** Reservation creation succeeded */
    CREATE_SUCCESS,

    /** Reservation creation failed */
    CREATE_FAILED,

    /** Waitlist operation succeeded */
    WAITLIST_SUCCESS,

    /** Waitlist operation failed */
    WAITLIST_FAILED,

    /** Bill payment succeeded */
    PAY_SUCCESS,

    /** Bill payment failed */
    PAY_FAILED,

    /** Returns a payment receipt */
    PAYMENT_RECEIPT,

    /** Bill was found */
    BILL_FOUND,

    /** Bill was not found */
    BILL_NOT_FOUND,

    /** No table is currently available */
    NO_TABLE_AVAILABLE,

    /** Subscriber was successfully identified at the terminal */
    TERMINAL_SUBSCRIBER_IDENTIFIED,

    /** Subscriber was not found at the terminal */
    TERMINAL_SUBSCRIBER_NOT_FOUND
}
