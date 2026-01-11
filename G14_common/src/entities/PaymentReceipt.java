package entities;

import java.io.Serializable;
import java.sql.Timestamp;

public class PaymentReceipt implements Serializable {
    private static final long serialVersionUID = 1L;

    private int paymentId;
    private int resId;
    private int confCode;

    private double amount;
    private double discount;
    private double finalAmount;

    private String status; // PAID / OPEN וכו'
    private Timestamp createdAt;
    private Timestamp paidAt;

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

    public int getPaymentId() { return paymentId; }
    public int getResId() { return resId; }
    public int getConfCode() { return confCode; }
    public double getAmount() { return amount; }
    public double getDiscount() { return discount; }
    public double getFinalAmount() { return finalAmount; }
    public String getStatus() { return status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getPaidAt() { return paidAt; }
}
