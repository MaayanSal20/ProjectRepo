package client;

import entities.ClientRequestType;
import entities.Subscriber;

public class ClientRequestBuilder {

    public static Object getOrders() {
        return new Object[]{ ClientRequestType.GET_ORDERS };
    }

    public static Object updateOrder(int orderNumber, String newDate, Integer guests) {
        return new Object[]{ ClientRequestType.UPDATE_ORDER, orderNumber, newDate, guests };
    }

   /* public static Object registerSubscriber(Subscriber s) {
        return new Object[]{ ClientRequestType.REGISTER_SUBSCRIBER, s };
    }*/
    
    public static Object repLogin(String username, String password) {
        return new Object[] { ClientRequestType.REP_LOGIN, username, password };
    }

    public static Object registerSubscriber(String name, String phone, String email) {
        return new Object[] { ClientRequestType.REGISTER_SUBSCRIBER, name, phone, email };
    }
    
    public static Object getReservationInfo(int confirmationCode) {
        return new Object[] { ClientRequestType.GET_RESERVATION_INFO,confirmationCode  };
    }
    
    public static Object cancelReservation(int confirmationCode) {
        return new Object[] { ClientRequestType.DELETE_RESERVATION,confirmationCode  };
    }

}