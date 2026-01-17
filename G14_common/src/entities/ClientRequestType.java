package entities;

import java.io.Serializable;

/**
 * Enumerates all request types that can be sent from the client to the server.
 * Each value represents a specific operation, action, or query supported
 * by the system.
 */
public enum ClientRequestType implements Serializable {

    /** Retrieve all reservations */
    GET_RESERVATIONS,

    /** Update an existing order (if still relevant) */
    UPDATE_ORDER,

    /** Retrieve reservation information by confirmation code */
    GET_RESERVATION_INFO,

    /** Delete an existing reservation */
    DELETE_RESERVATION,

    /** Representative login request */
    REP_LOGIN,

    /** Register a new subscriber */
    REGISTER_SUBSCRIBER,

    /** Subscriber login request */
    SUBSCRIBER_LOGIN,

    /** Retrieve all active reservations */
    GET_ACTIVE_RESERVATIONS,

    /** Retrieve the current waitlist */
    GET_WAITLIST,

    /** Retrieve current diners in the restaurant */
    GET_CURRENT_DINERS,

    /** Retrieve all subscribers */
    GET_SUBSCRIBERS,

    /** Retrieve available reservation time slots */
    GET_AVAILABLE_SLOTS,

    /** Create a new reservation */
    CREATE_RESERVATION,

    /** Join the waitlist */
    JOIN_WAITLIST,

    /** Leave the waitlist */
    LEAVE_WAITLIST,

    /** Pay a bill */
    PAY_BILL,

    /** Retrieve bill details by confirmation code */
    GET_BILL_BY_CONF_CODE,

    /** Run and store a monthly reports snapshot */
    RUN_MONTHLY_REPORTS_SNAPSHOT,

    /** Retrieve waitlist ratio grouped by hour (manager view) */
    MANAGER_WAITLIST_RATIO_BY_HOUR,

    /** Retrieve waitlist data for a specific month */
    GET_WAITLIST_BY_MONTH,

    /** Retrieve time-based report for a specific month */
    MANAGER_TIME_REPORT_BY_MONTH,

    /** Retrieve members activity report for a specific month */
    MANAGER_MEMBERS_REPORT_BY_MONTH,

    /** Retrieve all restaurant tables */
    GET_TABLES,

    /** Add a new table */
    ADD_TABLE,

    /** Update the number of seats for a table */
    UPDATE_TABLE_SEATS,

    /** Deactivate a table */
    DEACTIVATE_TABLE,

    /** Activate a table */
    ACTIVATE_TABLE,

    /** Retrieve weekly opening hours */
    GET_OPENING_WEEKLY,

    /** Update weekly opening hours */
    UPDATE_OPENING_WEEKLY,

    /** Retrieve all reservations for a specific subscriber */
    GET_ALL_RESERVATIONS_FOR_SUBSCRIBER,

    /** Retrieve completed reservations for a specific subscriber */
    GET_DONE_RESERVATIONS_FOR_SUBSCRIBER,

    /** Retrieve personal details of a subscriber */
    GET_SUBSCRIBER_PERSONAL_DETAILS,

    /** Update personal details of a subscriber */
    UPDATE_SUBSCRIBER_PERSONAL_DETAILS,

    /** Join waitlist as a subscriber */
    JOIN_WAITLIST_SUBSCRIBER,

    /** Join waitlist as a non-subscriber */
    JOIN_WAITLIST_NON_SUBSCRIBER,

    /** Leave waitlist as a subscriber */
    LEAVE_WAITLIST_SUBSCRIBER,

    /** Leave waitlist as a non-subscriber */
    LEAVE_WAITLIST_NON_SUBSCRIBER,

    /** Try to offer a table to the next customer in the waitlist */
    TRY_OFFER_TABLE_TO_WAITLIST,

    /** Confirm receiving an offered table */
    CONFIRM_RECEIVE_TABLE,

    /** Request recovery of a forgotten confirmation code */
    FORGOT_CONFIRMATION_CODE,

    /** Identify a subscriber at the terminal using a scan code */
    TERMINAL_IDENTIFY_SUBSCRIBER_BY_SCANCODE,

    /** Retrieve special opening hours */
    GET_OPENING_SPECIAL,

    /** Insert or update special opening hours */
    UPSERT_OPENING_SPECIAL,

    /** Delete special opening hours */
    DELETE_OPENING_SPECIAL,

    /** Retrieve confirmation code challenge for a subscriber */
    GET_CONF_CODE_CHALLENGE_FOR_SUBSCRIBER,

    /** Identify a subscriber at the terminal */
    TERMINAL_IDENTIFY_SUBSCRIBER
}
