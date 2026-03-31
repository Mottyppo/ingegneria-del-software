package it.unibs.ingesw.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores personal notifications for a fruitore.
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Provides read-only access to persisted notifications.</li>
 *   <li>Supports notification insertion and selective removal.</li>
 * </ul>
 */
public class PersonalSpace {
    private static final String TO_STRING_PREFIX =  "SpazioPersonale{";
    private static final String NOTIFICATIONS_LABEL = "notifiche=";
    private static final String TO_STRING_SUFFIX =  "}";

    private List<Notification> notifications;

    public PersonalSpace() {
        this.notifications = new ArrayList<>();
    }

    public List<Notification> getNotifications() {
        ensureNotifications();
        return Collections.unmodifiableList(notifications);
    }

    /**
     * Adds a new {@link Notification} to the personal space.
     * <p>
     * The notification will be rejected if the object itself is {@code null},
     * or if its internal message is {@code null} or blank.
     * </p>
     *
     * @param notification the {@link Notification} object to be added
     * @return {@code true} if the notification was successfully added;
     *         {@code false} if the notification or its message is invalid
     */
    public boolean addNotification(Notification notification) {
        if (notification == null || notification.getMessage() == null || notification.getMessage().isBlank()) {
            return false;
        }
        ensureNotifications();
        notifications.add(notification);
        return true;
    }

    /**
     * Creates and adds a new {@link Notification} using the provided message string.
     * <p>
     * The provided message is automatically trimmed. If the resulting string
     * is {@code null} or entirely blank, the notification is not created.
     * </p>
     *
     * @param message the text content of the notification to be created
     * @return {@code true} if the notification was successfully created and added;
     *         {@code false} if the message is {@code null} or blank
     */
    public boolean addNotification(String message) {
        String normalized = message == null ? null : message.trim();
        if (normalized == null || normalized.isBlank()) {
            return false;
        }
        return addNotification(new Notification(normalized));
    }

    /**
     * Removes a notification from the personal space based on its index.
     * <p>
     * This method safely handles out-of-bounds indices. If the provided index
     * is negative or greater than or equal to the current size of the notifications list,
     * no action is taken.
     * </p>
     *
     * @param index the zero-based position of the notification to remove
     * @return {@code true} if the notification was successfully removed;
     *         {@code false} if the index is out of bounds
     */
    public boolean removeNotification(int index) {
        ensureNotifications();
        if (index < 0 || index >= notifications.size()) {
            return false;
        }
        notifications.remove(index);
        return true;
    }

    private void ensureNotifications() {
        if (notifications == null) {
            notifications = new ArrayList<>();
        }
    }

    @Override
    public String toString() {
        return TO_STRING_PREFIX +
                NOTIFICATIONS_LABEL + notifications +
                TO_STRING_SUFFIX;
    }
}
