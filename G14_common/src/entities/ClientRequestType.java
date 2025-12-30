package entities;

import java.io.Serializable;

public enum ClientRequestType implements Serializable {
    GET_ORDERS,
    UPDATE_ORDER,
    
    REP_LOGIN,
    REGISTER_SUBSCRIBER,
    GET_SUBSCRIBER_BY_ID/////
}
