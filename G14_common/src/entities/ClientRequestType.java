package entities;

import java.io.Serializable;

public enum ClientRequestType implements Serializable {
    GET_ORDERS,
    UPDATE_ORDER,              // אם עדיין רלוונטי אצלך
    GET_RESERVATION_INFO,      // לפי id של reservation (ResId)
    DELETE_RESERVATION,        // מחיקה לפי ResId
    REP_LOGIN,
    REGISTER_SUBSCRIBER,
    SUBSCRIBER_LOGIN,

    // future:
    GET_ACTIVE_ORDERS,
    GET_WAITLIST,
    GET_CURRENT_DINERS,
    GET_SUBSCRIBERS
}
