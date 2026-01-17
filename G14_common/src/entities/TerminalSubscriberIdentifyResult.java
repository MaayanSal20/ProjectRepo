package entities;

import java.io.Serializable;

/**
 * Represents the result of attempting to identify a subscriber
 * using a terminal (for example, by scan code or other identifier).
 * The result indicates whether the identification was successful
 * and may include the subscriber details and an explanatory message.
 */
public class TerminalSubscriberIdentifyResult implements Serializable {

    /** Indicates whether the identification was successful */
    private final boolean success;

    /** The identified subscriber, or null if identification failed */
    private final Subscriber subscriber;

    /** Additional message describing the result */
    private final String message;

    /**
     * Constructs a TerminalSubscriberIdentifyResult.
     *
     * @param success true if the subscriber was successfully identified
     * @param subscriber the identified subscriber, or null if not found
     * @param message descriptive message about the identification result
     */
    public TerminalSubscriberIdentifyResult(boolean success, Subscriber subscriber, String message) {
        this.success = success;
        this.subscriber = subscriber;
        this.message = message;
    }

    /**
     * Indicates whether the identification was successful.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the identified subscriber.
     *
     * @return the subscriber, or null if identification failed
     */
    public Subscriber getSubscriber() {
        return subscriber;
    }

    /**
     * Returns a message describing the identification result.
     *
     * @return result message
     */
    public String getMessage() {
        return message;
    }
}
