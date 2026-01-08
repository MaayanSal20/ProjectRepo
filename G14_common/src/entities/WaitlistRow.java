package entities;

import java.io.Serializable;
import java.sql.Timestamp;

public class WaitlistRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private int confCode;
    private int customerId;
    private String email;
    private Timestamp timeEnterQueue;
    //private Timestamp notifiedAt;
    private int numOfDiners;
    private String phone;

    public WaitlistRow(int confCode,
                       Timestamp timeEnterQueue,
                       int numOfDiners,
                       int customerId,
                       String phone,
                       String email) {
        this.confCode = confCode;
        this.customerId = customerId;
        this.email = email;
        this.timeEnterQueue = timeEnterQueue;
        //this.notifiedAt = notifiedAt;
        this.numOfDiners = numOfDiners;
        this.phone = phone;
    }

    public int getConfCode() { return confCode; }
    public int getCustomerId() { return customerId; }
    public String getEmail() { return email; }
    public Timestamp getTimeEnterQueue() { return timeEnterQueue; }
    //public Timestamp getNotifiedAt() { return notifiedAt; }
    public String getPhone() { return phone; }
    public int getNumOfDiners() { return numOfDiners; }
}
