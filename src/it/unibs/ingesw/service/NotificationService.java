package it.unibs.ingesw.service;

import it.unibs.ingesw.factory.NotificationFactory;
import it.unibs.ingesw.model.Participant;
import it.unibs.ingesw.model.Proposal;

import java.util.List;

/**
 * Delivers automatic notifications related to proposal lifecycle changes.
 *
 * <p>The service is responsible only for finding subscribed participants and
 * appending the generated notification messages to their personal spaces.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Builds confirmation, cancellation and withdrawal messages through the factory.</li>
 *   <li>Finds participants by username among the loaded users.</li>
 *   <li>Returns whether any participant state has changed.</li>
 * </ul>
 */
public class NotificationService {
    private final List<Participant> participants;

    /**
     * Creates a notification service over the loaded participants.
     *
     * @param participants The loaded participants.
     */
    public NotificationService(List<Participant> participants) {
        this.participants = participants;
    }

    /**
     * Sends a confirmation notification to all subscribers of a proposal.
     *
     * @param proposal The confirmed proposal.
     * @return {@code true} if at least one participant changed, {@code false} otherwise.
     */
    public boolean notifyProposalConfirmed(Proposal proposal) {
        return notifySubscribers(proposal, NotificationFactory.buildProposalConfirmedNotification(proposal));
    }

    /**
     * Sends a cancellation notification to all subscribers of a proposal.
     *
     * @param proposal The canceled proposal.
     * @return {@code true} if at least one participant changed, {@code false} otherwise.
     */
    public boolean notifyProposalCanceled(Proposal proposal) {
        return notifySubscribers(proposal, NotificationFactory.buildProposalCanceledNotification(proposal));
    }

    /**
     * Sends a withdrawal notification to all subscribers of a proposal.
     *
     * @param proposal The withdrawn proposal.
     * @return {@code true} if at least one participant changed, {@code false} otherwise.
     */
    public boolean notifyProposalWithdrawed(Proposal proposal) {
        return notifySubscribers(proposal, NotificationFactory.buildProposalWithdrawedNotification(proposal));
    }

    /**
     * Returns the participant collection managed by this service.
     *
     * @return The shared participant list.
     */
    public List<Participant> getParticipants() {
        return participants;
    }

    /**
     * Finds a participant by username in a case-insensitive way.
     *
     * @param username The username to search for.
     * @return The matching participant, or {@code null} if not found.
     */
    private Participant findParticipantByUsername(String username) {
        if (username == null) {
            return null;
        }

        for (Participant participant : participants) {
            if (participant.getUsername().equalsIgnoreCase(username.trim())) {
                return participant;
            }
        }
        return null;
    }

    /**
     * Sends the given message to all subscribers of a proposal.
     *
     * @param proposal The proposal whose subscribers must be notified.
     * @param message  The message to deliver.
     * @return {@code true} if at least one notification was added, {@code false} otherwise.
     */
    private boolean notifySubscribers(Proposal proposal, String message) {
        boolean changed = false;
        for (String username : proposal.getSubscribers()) {
            Participant participant = findParticipantByUsername(username);
            if (participant != null) {
                changed = participant.getPersonalSpace().addNotification(message) || changed;
            }
        }
        return changed;
    }
}
