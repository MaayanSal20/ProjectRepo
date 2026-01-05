package entities;

import java.io.Serializable;

public enum ClientRequestType implements Serializable {
    GET_ORDERS,
    UPDATE_ORDER,
    GET_RESERVATION_INFO,
    REP_LOGIN,
    REGISTER_SUBSCRIBER,
    DELETE_RESERVATION,
    SUBSCRIBER_LOGIN,
    SUBSCRIBER_LOGIN_SUCCESS,
    SUBSCRIBER_LOGIN_FAILED,
    GET_SUBSCRIBER_BY_ID/////
}