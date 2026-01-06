package entities;

import java.io.Serializable;

public class Subscriber implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int subscriberId;
    private final String name;
    private final String phone;
   // private final int CostumerId; //changed by maayan 6.1.26
    private final String email;

    public Subscriber(int subscriberId, String name, String phone, String email) {
        this.subscriberId = subscriberId;
        this.name = name;
        this.phone = phone;
        this.email = email;//changed by maayan 6.1.26
        //this.CostumerId=CostumerId;
    }

    public int getSubscriberId() { return subscriberId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
   // public int getCostumerId() { return CostumerId; }//changed by maayan 6.1.26
    
}
