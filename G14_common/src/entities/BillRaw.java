package entities;

import java.io.Serializable;


/**
 * Represents raw billing information for a reservation.
 * This class contains basic billing data before any detailed
 * calculations or breakdowns are applied.
 */
public class BillRaw implements Serializable {
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
     * Base billing amount before discounts or adjustments.
     */
    private double amount;

    /**
     * Indicates whether the customer is a subscriber.
     */
    private boolean subscriber;

    /**
     * Current billing or payment status.
     */
    private String status;

    /**
     * Constructs a new BillRaw object.
     *
     * @param confCode reservation confirmation code
     * @param resId internal reservation identifier
     * @param amount base billing amount
     * @param subscriber indicates whether the customer is a subscriber
     * @param status current billing or payment status
     */
    public BillRaw(int confCode, int resId, double amount, boolean subscriber, String status) {
        this.confCode = confCode;
        this.resId = resId;
        this.amount = amount;
        this.subscriber = subscriber;
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
     * Returns the base billing amount.
     *
     * @return the billing amount
     */
    public double getAmount() { return amount; }

    /**
     * Indicates whether the customer is a subscriber.
     *
     * @return true if the customer is a subscriber, false otherwise
     */
    public boolean isSubscriber() { return subscriber; }

    /**
     * Returns the current billing or payment status.
     *
     * @return the status
     */
    public String getStatus() { return status; }
}
