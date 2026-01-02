package entities;

import java.io.Serializable;

public enum ServerResponseType implements Serializable {
    ORDERS_LIST,
    UPDATE_SUCCESS,
    UPDATE_FAILED,
    //REGISTER_SUCCESS,
    //REGISTER_FAILED,
    ERROR,
    RESERVATION_FOUND,
    RESERVATION_NOT_FOUND,
    LOGIN_FAILED,
    LOGIN_SUCCESS,
    REGISTER_SUCCESS,
    REGISTER_FAILED,
    DELETE_SUCCESS,
    DELETE_FAILED
}