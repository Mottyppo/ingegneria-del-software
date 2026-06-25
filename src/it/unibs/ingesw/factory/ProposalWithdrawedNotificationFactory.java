package it.unibs.ingesw.factory;

import it.unibs.ingesw.model.Notification;
import it.unibs.ingesw.model.Proposal;

/**
 * Creates notifications for withdrawn proposals.
 */
public class ProposalWithdrawedNotificationFactory extends AbstractProposalNotificationFactory {
    private static final String WITHDRAWED_TEMPLATE =
            "Proposta #%d ritirata: \"%s\". L'iniziativa e' stata ritirata dal configuratore per cause di forza maggiore.";

    private ProposalWithdrawedNotificationFactory() {
    }

    /**
     * Returns the unique withdrawn-notification factory instance.
     *
     * @return The shared factory instance.
     */
    public static ProposalWithdrawedNotificationFactory getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public Notification createNotification(Proposal proposal) {
        return new Notification(WITHDRAWED_TEMPLATE.formatted(proposal.getId(), titleOf(proposal)));
    }

    private static class Holder {
        private static final ProposalWithdrawedNotificationFactory INSTANCE = new ProposalWithdrawedNotificationFactory();
    }
}
