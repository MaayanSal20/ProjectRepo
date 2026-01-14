package entities;

import java.io.Serializable;

public class ForgotConfirmationCodeRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String phone;
    private final String email;

    public ForgotConfirmationCodeRequest(String phone, String email) {
        this.phone = phone;
        this.email = email;
    }

    public String getPhone() { return phone; }
    public String getEmail() { return email; }
}
