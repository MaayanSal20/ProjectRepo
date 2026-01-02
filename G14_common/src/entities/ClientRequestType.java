package entities;

import java.io.Serializable;

public enum ClientRequestType implements Serializable {
    GET_ORDERS,
    UPDATE_ORDER,
    GET_RESERVATION_INFO,
    REP_LOGIN,
    REGISTER_SUBSCRIBER,
    DELETE_RESERVATION,
    GET_SUBSCRIBER_BY_ID/////
}