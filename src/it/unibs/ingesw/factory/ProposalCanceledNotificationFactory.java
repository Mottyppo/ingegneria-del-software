package it.unibs.ingesw.factory;

import it.unibs.ingesw.model.Notification;
import it.unibs.ingesw.model.Proposal;

/**
 * Creates notifications for canceled proposals.
 */
public class ProposalCanceledNotificationFactory extends AbstractProposalNotificationFactory {
    private static final String CANCELED_TEMPLATE =
            "Proposta #%d annullata: \"%s\". L'iniziativa non ha raggiunto il numero richiesto di partecipanti entro la chiusura iscrizioni.";

    private ProposalCanceledNotificationFactory() {
    }

    /**
     * Returns the unique canceled-notification factory instance.
     *
     * @return The shared factory instance.
     */
    public static ProposalCanceledNotificationFactory getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public Notification createNotification(Proposal proposal) {
        return new Notification(CANCELED_TEMPLATE.formatted(proposal.getId(), titleOf(proposal)));
    }

    private static class Holder {
        private static final ProposalCanceledNotificationFactory INSTANCE = new ProposalCanceledNotificationFactory();
    }
}
