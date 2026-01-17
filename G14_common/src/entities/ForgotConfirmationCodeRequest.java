package entities;

import java.io.Serializable;

/**
 * Represents a request to retrieve a forgotten reservation confirmation code.
 * This request is sent from the client to the server using the customer's
 * contact information.
 */
public class ForgotConfirmationCodeRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Contact phone number used to identify the reservation.
     */
    private final String phone;

    /**
     * Contact email address used to identify the reservation.
     */
    private final String email;

    /**
     * Constructs a new ForgotConfirmationCodeRequest.
     *
     * @param phone contact phone number
     * @param email contact email address
     */
    public ForgotConfirmationCodeRequest(String phone, String email) {
        this.phone = phone;
        this.email = email;
    }

    /**
     * Returns the contact phone number.
     *
     * @return the phone number
     */
    public String getPhone() { return phone; }

    /**
     * Returns the contact email address.
     *
     * @return the email address
     */
    public String getEmail() { return email; }
}
