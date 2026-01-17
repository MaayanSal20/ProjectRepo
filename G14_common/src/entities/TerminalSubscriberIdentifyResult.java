package entities;

import java.io.Serializable;

public class TerminalSubscriberIdentifyResult implements Serializable {
    private final boolean success;
    private final Subscriber subscriber;
    private final String message;

    public TerminalSubscriberIdentifyResult(boolean success, Subscriber subscriber, String message) {
        this.success = success;
        this.subscriber = subscriber;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public Subscriber getSubscriber() { return subscriber; }
    public String getMessage() { return message; }
}
