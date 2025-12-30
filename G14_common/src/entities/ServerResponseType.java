package entities;

import java.io.Serializable;

public enum ServerResponseType implements Serializable {
    ORDERS_LIST,
    UPDATE_SUCCESS,
    UPDATE_FAILED,
    //REGISTER_SUCCESS,
    //REGISTER_FAILED,
    ERROR,
    
    LOGIN_FAILED,
    LOGIN_SUCCESS,
    REGISTER_SUCCESS,
    REGISTER_FAILED
}
