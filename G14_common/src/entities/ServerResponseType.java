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
    
    //Subscriber
    SUBSCRIBER_RESERVATIONS_LIST,//Added by maayan 10.1.26 to show list 
    SUBSCRIBER_PERSONAL_DETAILS,          // //Added by maayan 10.1.26 retuen subscriber Details
    SUBSCRIBER_PERSONAL_DETAILS_UPDATED,  // Added by maayan 10.1.26 - is update successd or not
    
    
    //Waiting List - Customer
    WAITLIST_OFFER_CREATED,//Added by maayan 12.1.26
    //WAITLIST_NO_MATCH,//Added by maayan 12.1.26
    INFO,
    WAITINGLIST_SUCCESS,
    WAITINGLIST_ERROR,
    
    CONFIRMATION_CODE_FOUND,//added by maayan 14.1.26
    CONFIRMATION_CODE_NOT_FOUND,
    CONF_CODE_CHALLENGE,
    CONF_CODE_CHALLENGE_EMPTY,


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
    MONTHLY_SNAPSHOT_OK,
    MONTHLY_SNAPSHOT_FAILED,
    WAITLIST_RATIO_BY_HOUR_DATA,

    // Customer actions (we'll implement later)
    SLOTS_LIST,
    CREATE_SUCCESS,
    CREATE_FAILED,
    WAITLIST_SUCCESS,
    WAITLIST_FAILED,
    PAY_SUCCESS,
    PAY_FAILED,
    BILL_FOUND,
    BILL_NOT_FOUND,
    
    NO_TABLE_AVAILABLE,
    TERMINAL_SUBSCRIBER_IDENTIFIED,
    TERMINAL_SUBSCRIBER_NOT_FOUND,

}
