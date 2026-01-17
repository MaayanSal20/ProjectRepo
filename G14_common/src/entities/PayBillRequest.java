package entities; //////////////////////

import java.io.Serializable;

/**
 * Represents a request to pay a bill.
 * This object is sent from the client to the server and contains
 * the required information to process a payment for a reservation.
 */
public class PayBillRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Reservation confirmation code.
     */
    private int confCode;

    /**
     * Bill amount before any discounts are applied.
     */
    private double amount; 
    /**
     * Indicates who performed the payment.
     * Examples include subscriber, regular customer, or representative.
     */
    private String paidBy; 

    /**
     * Constructs a new PayBillRequest object.
     *
     * @param confCode reservation confirmation code
     * @param amount bill amount before discounts
     * @param paidBy identifier of the payer
     */
    public PayBillRequest(int confCode, double amount, String paidBy) {
        this.confCode = confCode;
        this.amount = amount;
        this.paidBy = paidBy;
    }

    /**
     * Returns the reservation confirmation code.
     *
     * @return the confirmation code
     */
    public int getConfCode() { return confCode; }

    /**
     * Returns the bill amount before discounts.
     *
     * @return the bill amount
     */
    public double getAmount() { return amount; }

    /**
     * Returns who performed the payment.
     *
     * @return the payer identifier
     */
    public String getPaidBy() { return paidBy; }

    /**
     * Sets the reservation confirmation code.
     *
     * @param confCode the confirmation code
     */
    public void setConfCode(int confCode) { this.confCode = confCode; }

    /**
     * Sets the bill amount before discounts.
     *
     * @param amount the bill amount
     */
    public void setAmount(double amount) { this.amount = amount; }

    /**
     * Sets who performed the payment.
     *
     * @param paidBy the payer identifier
     */
    public void setPaidBy(String paidBy) { this.paidBy = paidBy; }
}
