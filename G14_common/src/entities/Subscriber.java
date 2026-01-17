package entities;

import java.io.Serializable;

/**
 * Represents a subscriber in the system.
 * A subscriber is a registered customer with additional identification
 * details such as a scan code.
 */
public class Subscriber implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Unique identifier of the subscriber */
    private final int subscriberId;

    /** Full name of the subscriber */
    private final String name;

    /** Optional personal information about the subscriber */
    private final String personalInfo;

    /** Related customer ID (can be 0 if not linked) */
    private final int customerId;

    /** Phone number of the subscriber */
    private final String phone;

    /** Email address of the subscriber */
    private final String email;

    /** Scan code used for identification (e.g. terminal or QR scan) */
    private final String scanCode;

    /**
     * Constructs a fully initialized Subscriber object.
     *
     * @param subscriberId unique subscriber identifier
     * @param name subscriber full name
     * @param personalInfo optional personal information
     * @param customerId related customer ID
     * @param phone subscriber phone number
     * @param email subscriber email address
     * @param scanCode scan code used for identification
     */
    public Subscriber(int subscriberId, String name, String personalInfo,
                      int customerId, String phone, String email, String scanCode) {
        this.subscriberId = subscriberId;
        this.name = name;
        this.personalInfo = personalInfo;
        this.customerId = customerId;
        this.phone = phone;
        this.email = email;
        this.scanCode = scanCode;
    }

    /**
     * Constructs a Subscriber with minimal information.
     * Personal info and customer ID are set to default values.
     *
     * @param subscriberId unique subscriber identifier
     * @param name subscriber full name
     * @param phone subscriber phone number
     * @param email subscriber email address
     * @param scanCode scan code used for identification
     */
    public Subscriber(int subscriberId, String name, String phone, String email, String scanCode) {
        this(subscriberId, name, null, 0, phone, email, scanCode);
    }

    /**
     * Returns the subscriber ID.
     *
     * @return subscriber ID
     */
    public int getSubscriberId() {
        return subscriberId;
    }

    /**
     * Returns the subscriber name.
     *
     * @return subscriber name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns personal information about the subscriber.
     *
     * @return personal information, or null if not provided
     */
    public String getPersonalInfo() {
        return personalInfo;
    }

    /**
     * Returns the related customer ID.
     *
     * @return customer ID
     */
    public int getCustomerId() {
        return customerId;
    }

    /**
     * Returns the subscriber phone number.
     *
     * @return phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Returns the subscriber email address.
     *
     * @return email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the scan code used to identify the subscriber.
     *
     * @return scan code
     */
    public String getScanCode() {
        return scanCode;
    }
}
