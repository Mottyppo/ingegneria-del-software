package it.unibs.ingesw.factory;

import it.unibs.ingesw.model.Notification;
import it.unibs.ingesw.model.Proposal;

/**
 * Creates notifications for confirmed proposals.
 */
public class ProposalConfirmedNotificationFactory extends AbstractProposalNotificationFactory {
    private static final String CONFIRMED_PREFIX_TEMPLATE = "Proposta #%d confermata: \"%s\". ";
    private static final String CONFIRMED_REMINDER_TEMPLATE =
            "Promemoria evento -> Data: %s, Ora: %s, Luogo: %s, Quota individuale: %s.";

    private ProposalConfirmedNotificationFactory() {
    }

    /**
     * Returns the unique confirmed-notification factory instance.
     *
     * @return The shared factory instance.
     */
    public static ProposalConfirmedNotificationFactory getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public Notification createNotification(Proposal proposal) {
        String title = titleOf(proposal);
        String date = valueOrDefault(proposal, START_DATE_FIELD_NAME, DEFAULT_VALUE);
        String time = valueOrDefault(proposal, TIME_FIELD_NAME, DEFAULT_VALUE);
        String place = valueOrDefault(proposal, PLACE_FIELD_NAME, DEFAULT_VALUE);
        String fee = valueOrDefault(proposal, FEE_FIELD_NAME, DEFAULT_VALUE);

        String message = CONFIRMED_PREFIX_TEMPLATE.formatted(proposal.getId(), title)
                + CONFIRMED_REMINDER_TEMPLATE.formatted(date, time, place, fee);
        return new Notification(message);
    }

    private static class Holder {
        private static final ProposalConfirmedNotificationFactory INSTANCE = new ProposalConfirmedNotificationFactory();
    }
}
