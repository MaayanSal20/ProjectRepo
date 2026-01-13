package entities;

import java.io.Serializable;

public class WaitlistJoinResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final WaitlistStatus status;
    private final int confirmationCode;
    private final Integer tableNum;
    private final String message;

    public WaitlistJoinResult(WaitlistStatus status, int confirmationCode, Integer tableNum, String message) {
        this.status = status;
        this.confirmationCode = confirmationCode;
        this.tableNum = tableNum;
        this.message = message;
    }

    public WaitlistStatus getStatus() { return status; }
    public int getConfirmationCode() { return confirmationCode; }
    public Integer getTableNum() { return tableNum; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return "WaitlistJoinResult{" +
                "status=" + status +
                ", confirmationCode=" + confirmationCode +
                ", tableNum=" + tableNum +
                ", message='" + message + '\'' +
                '}';
    }
}
