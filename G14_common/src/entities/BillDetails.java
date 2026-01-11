package entities;

import java.io.Serializable;

public class BillDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    private int confCode;
    private int resId;
    private double subtotal;
    private boolean subscriber;
    private double discount;
    private double finalAmount;
    private String status; // ✅ חדש

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

    public int getConfCode() { return confCode; }
    public int getResId() { return resId; }
    public double getSubtotal() { return subtotal; }
    public boolean isSubscriber() { return subscriber; }
    public double getDiscount() { return discount; }
    public double getFinalAmount() { return finalAmount; }
    public String getStatus() { return status; } // ✅ חדש
}
