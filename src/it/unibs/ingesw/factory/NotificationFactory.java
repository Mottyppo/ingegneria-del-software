package it.unibs.ingesw.factory;

import it.unibs.ingesw.model.Notification;
import it.unibs.ingesw.model.Proposal;

/**
 * Factory Method creator for proposal-related notifications.
 */
public interface NotificationFactory {
    /**
     * Creates a notification for the given proposal.
     *
     * @param proposal The proposal that triggered the notification.
     * @return The created notification.
     */
    Notification createNotification(Proposal proposal);
}
