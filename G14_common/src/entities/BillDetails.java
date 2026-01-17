package entities;

import java.io.Serializable;

/**
 * Represents billing details for a reservation.
 * This class is used to transfer billing and payment-related
 * information between the server and the client.
 */
public class BillDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Reservation confirmation code.
     */
    private int confCode;

    /**
     * Internal reservation identifier.
     */
    private int resId;

    /**
     * Subtotal amount before discounts are applied.
     */
    private double subtotal;

    /**
     * Indicates whether the customer is a subscriber.
     */
    private boolean subscriber;

    /**
     * Discount amount applied to the bill.
     */
    private double discount;

    /**
     * Final amount to be paid after discounts.
     */
    private double finalAmount;

    /**
     * Current billing or payment status.
     */
    private String status; 

    /**
     * Constructs a new BillDetails object.
     *
     * @param confCode reservation confirmation code
     * @param resId internal reservation identifier
     * @param subtotal subtotal amount before discounts
     * @param subscriber indicates whether the customer is a subscriber
     * @param discount discount amount applied
     * @param finalAmount final amount to be paid
     * @param status current billing or payment status
     */
    public BillDetails(int confCode, int resId, double subtotal, boolean subscriber,
                       double discount, double finalAmount, String status) {
        this.confCode = confCode;
        this.resId = resId;
        this.subtotal = subtotal;
        this.subscriber = subscriber;
        this.discount = discount;
        this.finalAmount = finalAmount;
        this.status = status;
    }

    /**
     * Returns the reservation confirmation code.
     *
     * @return the confirmation code
     */
    public int getConfCode() { return confCode; }

    /**
     * Returns the internal reservation identifier.
     *
     * @return the reservation ID
     */
    public int getResId() { return resId; }

    /**
     * Returns the subtotal amount before discounts.
     *
     * @return the subtotal amount
     */
    public double getSubtotal() { return subtotal; }

    /**
     * Indicates whether the customer is a subscriber.
     *
     * @return true if the customer is a subscriber, false otherwise
     */
    public boolean isSubscriber() { return subscriber; }

    /**
     * Returns the discount amount applied to the bill.
     *
     * @return the discount amount
     */
    public double getDiscount() { return discount; }

    /**
     * Returns the final amount to be paid.
     *
     * @return the final amount
     */
    public double getFinalAmount() { return finalAmount; }

    /**
     * Returns the current billing or payment status.
     *
     * @return the status
     */
    public String getStatus() { return status; }
}