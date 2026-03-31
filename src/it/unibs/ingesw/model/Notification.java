package it.unibs.ingesw.model;

/**
 * Represents a personal-space notification addressed to a fruitore.
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Stores the notification message.</li>
 *   <li>Provides a compact textual representation.</li>
 * </ul>
 */
public class Notification {
    private static final String TO_STRING_PREFIX =  "Notifica{";
    private static final String MESSAGE_LABEL =     "messaggio='";
    private static final String TO_STRING_SUFFIX =  "'}";

    private final String message;

    public Notification(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return TO_STRING_PREFIX +
                MESSAGE_LABEL + message +
                TO_STRING_SUFFIX;
    }
}
