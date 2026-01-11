package entities; //////////////////////

import java.io.Serializable;

public class PayBillRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int confCode;
    private double amount; // הסכום לפני הנחה (החשבון)
    private String paidBy; // אופציונלי: "subscriber"/"regular"/"rep" וכו'

    public PayBillRequest(int confCode, double amount, String paidBy) {
        this.confCode = confCode;
        this.amount = amount;
        this.paidBy = paidBy;
    }

    public int getConfCode() { return confCode; }
    public double getAmount() { return amount; }
    public String getPaidBy() { return paidBy; }

    public void setConfCode(int confCode) { this.confCode = confCode; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setPaidBy(String paidBy) { this.paidBy = paidBy; }
}
