package it.unibs.ingesw.factory;

import it.unibs.ingesw.model.Proposal;

import java.util.Map;

/**
 * Utility factory for proposal-related notification messages.
 *
 * <p>The class is not instantiable and provides static creators for the
 * automatic messages generated during proposal lifecycle transitions.</p>
 */
public final class NotificationFactory {
    private static final String NOT_INSTANTIABLE_MESSAGE = "Questa classe non e' istanziabile.";

    private static final String TITLE_FIELD_NAME = "Titolo";
    private static final String START_DATE_FIELD_NAME = "Data";
    private static final String TIME_FIELD_NAME = "Ora";
    private static final String PLACE_FIELD_NAME = "Luogo";
    private static final String FEE_FIELD_NAME = "Quota individuale";

    private static final String DEFAULT_TITLE = "(senza titolo)";
    private static final String DEFAULT_VALUE = "-";

    private static final String CONFIRMED_PREFIX_TEMPLATE = "Proposta #%d confermata: \"%s\". ";
    private static final String CONFIRMED_REMINDER_TEMPLATE =
            "Promemoria evento -> Data: %s, Ora: %s, Luogo: %s, Quota individuale: %s.";
    private static final String CANCELED_TEMPLATE =
            "Proposta #%d annullata: \"%s\". L'iniziativa non ha raggiunto il numero richiesto di partecipanti entro la chiusura iscrizioni.";
    private static final String WITHDRAWED_TEMPLATE =
            "Proposta #%d ritirata: \"%s\". L'iniziativa e' stata ritirata dal configuratore per cause di forza maggiore.";

    private NotificationFactory() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE_MESSAGE);
    }

    /**
     * Builds a confirmation notification message for a proposal.
     *
     * @param proposal The confirmed proposal.
     * @return A human-readable notification message.
     */
    public static String buildProposalConfirmedNotification(Proposal proposal) {
        Map<String, String> values = proposal.getFieldValues();
        String title = values.getOrDefault(TITLE_FIELD_NAME, DEFAULT_TITLE);
        String date = values.getOrDefault(START_DATE_FIELD_NAME, DEFAULT_VALUE);
        String time = values.getOrDefault(TIME_FIELD_NAME, DEFAULT_VALUE);
        String place = values.getOrDefault(PLACE_FIELD_NAME, DEFAULT_VALUE);
        String fee = values.getOrDefault(FEE_FIELD_NAME, DEFAULT_VALUE);

        return CONFIRMED_PREFIX_TEMPLATE.formatted(proposal.getId(), title)
                + CONFIRMED_REMINDER_TEMPLATE.formatted(date, time, place, fee);
    }

    /**
     * Builds a cancellation notification message for a proposal.
     *
     * @param proposal The canceled proposal.
     * @return A human-readable notification message.
     */
    public static String buildProposalCanceledNotification(Proposal proposal) {
        Map<String, String> values = proposal.getFieldValues();
        String title = values.getOrDefault(TITLE_FIELD_NAME, DEFAULT_TITLE);
        return CANCELED_TEMPLATE.formatted(proposal.getId(), title);
    }

    /**
     * Builds a withdrawal notification message for a proposal.
     *
     * @param proposal The withdrawn proposal.
     * @return A human-readable notification message.
     */
    public static String buildProposalWithdrawedNotification(Proposal proposal) {
        Map<String, String> values = proposal.getFieldValues();
        String title = values.getOrDefault(TITLE_FIELD_NAME, DEFAULT_TITLE);
        return WITHDRAWED_TEMPLATE.formatted(proposal.getId(), title);
    }
}
