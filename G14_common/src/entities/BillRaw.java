package entities;

import java.io.Serializable;

public class BillRaw implements Serializable {
    private static final long serialVersionUID = 1L;

    private int confCode;
    private int resId;
    private double amount;
    private boolean subscriber;
    private String status;

    public BillRaw(int confCode, int resId, double amount, boolean subscriber, String status) {
        this.confCode = confCode;
        this.resId = resId;
        this.amount = amount;
        this.subscriber = subscriber;
        this.status = status;
    }

    public int getConfCode() { return confCode; }
    public int getResId() { return resId; }
    public double getAmount() { return amount; }
    public boolean isSubscriber() { return subscriber; }
    public String getStatus() { return status; }
}
