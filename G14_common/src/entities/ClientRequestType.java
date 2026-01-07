package entities;

import java.io.Serializable;

public enum ClientRequestType implements Serializable {
    GET_ORDERS,
    UPDATE_ORDER,              // אם עדיין רלוונטי
    GET_RESERVATION_INFO,
    DELETE_RESERVATION,
    REP_LOGIN,
    REGISTER_SUBSCRIBER,
    SUBSCRIBER_LOGIN,

    GET_ACTIVE_ORDERS,
    GET_WAITLIST,
    GET_CURRENT_DINERS,
    GET_SUBSCRIBERS,
    GET_AVAILABLE_SLOTS,
    CREATE_RESERVATION,
    JOIN_WAITLIST,
    LEAVE_WAITLIST,
    PAY_BILL,
    GET_WAITLIST_BY_MONTH,
    MANAGER_TIME_REPORT_BY_MONTH,
    MANAGER_MEMBERS_REPORT_BY_MONTH
}
