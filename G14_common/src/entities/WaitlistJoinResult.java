package entities;

import java.io.Serializable;


/**
 * Represents the result of a request to join the waitlist.
 * This object is sent from the server to the client and contains
 * the status of the request along with optional assignment details.
 */
public class WaitlistJoinResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Status of the waitlist join attempt.
     */
    private final WaitlistStatus status;

    /**
     * Confirmation code assigned to the waitlist entry.
     */
    private final int confirmationCode;

    /**
     * Table number assigned to the customer.
     * May be null if no table was assigned.
     */
    private final Integer tableNum;

    /**
     * Informational message describing the result.
     */
    private final String message;

    /**
     * Constructs a new WaitlistJoinResult.
     *
     * @param status result status of the waitlist request
     * @param confirmationCode confirmation code for the waitlist entry
     * @param tableNum assigned table number, or null if not assigned
     * @param message descriptive message for the client
     */
    public WaitlistJoinResult(WaitlistStatus status,
                              int confirmationCode,
                              Integer tableNum,
                              String message) {
        this.status = status;
        this.confirmationCode = confirmationCode;
        this.tableNum = tableNum;
        this.message = message;
    }

    /**
     * Returns the waitlist status.
     *
     * @return waitlist status
     */
    public WaitlistStatus getStatus() { return status; }

    /**
     * Returns the confirmation code.
     *
     * @return confirmation code
     */
    public int getConfirmationCode() { return confirmationCode; }

    /**
     * Returns the assigned table number.
     *
     * @return table number or null if not assigned
     */
    public Integer getTableNum() { return tableNum; }

    /**
     * Returns the result message.
     *
     * @return descriptive message
     */
    public String getMessage() { return message; }

    /**
     * Returns a string representation of the waitlist join result.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "WaitlistJoinResult{" +
                "status=" + status +
                ", confirmationCode=" + confirmationCode +
                ", tableNum=" + tableNum +
                ", message='" + message + '\'' +
                '}';
    }
}
