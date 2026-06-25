package it.unibs.ingesw.factory;

import it.unibs.ingesw.model.Proposal;

import java.util.Map;

/**
 * Shared helpers for concrete proposal notification factories.
 */
abstract class AbstractProposalNotificationFactory implements NotificationFactory {
    protected static final String TITLE_FIELD_NAME = "Titolo";
    protected static final String START_DATE_FIELD_NAME = "Data";
    protected static final String TIME_FIELD_NAME = "Ora";
    protected static final String PLACE_FIELD_NAME = "Luogo";
    protected static final String FEE_FIELD_NAME = "Quota individuale";

    protected static final String DEFAULT_TITLE = "(senza titolo)";
    protected static final String DEFAULT_VALUE = "-";

    protected String valueOrDefault(Proposal proposal, String fieldName, String defaultValue) {
        Map<String, String> values = proposal.getFieldValues();
        return values.getOrDefault(fieldName, defaultValue);
    }

    protected String titleOf(Proposal proposal) {
        return valueOrDefault(proposal, TITLE_FIELD_NAME, DEFAULT_TITLE);
    }
}
