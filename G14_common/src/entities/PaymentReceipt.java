package entities;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Represents a payment receipt for a reservation.
 * This class contains detailed information about a payment,
 * including amounts, discounts, status, and timestamps.
 */
public class PaymentReceipt implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Unique payment identifier.
     */
    private int paymentId;

    /**
     * Internal reservation identifier.
     */
    private int resId;

    /**
     * Reservation confirmation code.
     */
    private int confCode;

    /**
     * Original bill amount before discounts.
     */
    private double amount;

    /**
     * Discount applied to the payment.
     */
    private double discount;

    /**
     * Final amount paid after discounts.
     */
    private double finalAmount;

    /**
     * Current payment status.
     * Examples include PAID or OPEN.
     */
    private String status; // PAID / OPEN וכו'

    /**
     * Timestamp when the payment record was created.
     */
    private Timestamp createdAt;

    /**
     * Timestamp when the payment was completed.
     * May be null if the payment has not yet been made.
     */
    private Timestamp paidAt;

    /**
     * Constructs a new PaymentReceipt object.
     *
     * @param paymentId unique payment identifier
     * @param resId internal reservation identifier
     * @param confCode reservation confirmation code
     * @param amount original bill amount
     * @param discount discount applied
     * @param finalAmount final amount paid
     * @param status current payment status
     * @param createdAt creation timestamp
     * @param paidAt payment completion timestamp
     */
    public PaymentReceipt(int paymentId, int resId, int confCode,
                          double amount, double discount, double finalAmount,
                          String status, Timestamp createdAt, Timestamp paidAt) {
        this.paymentId = paymentId;
        this.resId = resId;
        this.confCode = confCode;
        this.amount = amount;
        this.discount = discount;
        this.finalAmount = finalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
    }

    /**
     * Returns the payment identifier.
     *
     * @return the payment ID
     */
    public int getPaymentId() { return paymentId; }

    /**
     * Returns the reservation identifier.
     *
     * @return the reservation ID
     */
    public int getResId() { return resId; }

    /**
     * Returns the reservation confirmation code.
     *
     * @return the confirmation code
     */
    public int getConfCode() { return confCode; }

    /**
     * Returns the original bill amount.
     *
     * @return the bill amount
     */
    public double getAmount() { return amount; }

    /**
     * Returns the discount applied.
     *
     * @return the discount amount
     */
    public double getDiscount() { return discount; }

    /**
     * Returns the final amount paid.
     *
     * @return the final amount
     */
    public double getFinalAmount() { return finalAmount; }

    /**
     * Returns the current payment status.
     *
     * @return the payment status
     */
    public String getStatus() { return status; }

    /**
     * Returns the creation timestamp of the payment record.
     *
     * @return the creation timestamp
     */
    public Timestamp getCreatedAt() { return createdAt; }

    /**
     * Returns the payment completion timestamp.
     *
     * @return the payment timestamp, or null if not paid
     */
    public Timestamp getPaidAt() { return paidAt; }
}
