package entities;

import java.io.Serializable;

public class Subscriber implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int subscriberId;
    private final String name;
    private final String personalInfo;
    private final int customerId;
    private final String phone;
    private final String email;
    private final String scanCode;

    public Subscriber(int subscriberId, String name, String personalInfo,
                      int customerId, String phone, String email,String scanCode) {
        this.subscriberId = subscriberId;
        this.name = name;
        this.personalInfo = personalInfo;
        this.customerId = customerId;
        this.phone = phone;
        this.email = email;
        this.scanCode=scanCode;
    }
    
    public Subscriber(int subscriberId, String name, String phone, String email,String ScanCode) {
        this(subscriberId, name, null, 0, phone, email,ScanCode);
    }

    public int getSubscriberId() { return subscriberId; }
    public String getName() { return name; }
    public String getPersonalInfo() { return personalInfo; }
    public int getCustomerId() { return customerId; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getScanCode() { return scanCode; }
}
