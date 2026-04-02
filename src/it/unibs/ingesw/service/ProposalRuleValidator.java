package it.unibs.ingesw.service;

import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Evaluates proposal business rules and parses canonical proposal values.
 *
 * <p>The validator operates on the canonical representation used by the
 * persistence layer, where dates are stored as ISO strings and numbers are
 * already normalized.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Validates proposal date and numeric constraints.</li>
 *   <li>Parses canonical ISO dates and numeric values safely.</li>
 *   <li>Evaluates lifecycle-related date conditions.</li>
 * </ul>
 */
public class ProposalRuleValidator {
    private static final String DEADLINE_FIELD_NAME = "Termine ultimo di iscrizione";
    private static final String START_DATE_FIELD_NAME = "Data";
    private static final String END_DATE_FIELD_NAME = "Data conclusiva";
    private static final String PARTICIPANTS_FIELD_NAME = "Numero di partecipanti";
    private static final String FEE_FIELD_NAME = "Quota individuale";

    /**
     * Checks whether a normalized proposal satisfies the required business rules.
     *
     * @param values The canonical proposal values.
     * @return {@code true} if all rules are respected, {@code false} otherwise.
     */
    public boolean checkDomainRules(Map<String, String> values) {
        LocalDate deadline = parseIsoDate(values.get(DEADLINE_FIELD_NAME));
        LocalDate startDate = parseIsoDate(values.get(START_DATE_FIELD_NAME));
        LocalDate endDate = parseIsoDate(values.get(END_DATE_FIELD_NAME));
        Integer participants = parseInteger(values.get(PARTICIPANTS_FIELD_NAME));
        Double fee = parseDouble(values.get(FEE_FIELD_NAME));

        if (deadline == null || !deadline.isAfter(LocalDate.now())) {
            return false;
        }
        if (startDate == null || startDate.isBefore(deadline.plusDays(2))) {
            return false;
        }
        if (endDate == null || endDate.isBefore(startDate)) {
            return false;
        }
        if (participants == null || participants <= 0) {
            return false;
        }
        return fee != null && fee >= 0.0f;
    }

    /**
     * Parses an ISO date string into a {@link LocalDate}.
     *
     * @param value The value to parse.
     * @return The parsed date, or {@code null} if the input is invalid.
     */
    public LocalDate parseIsoDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    /**
     * Parses an integer value safely.
     *
     * @param value The value to parse.
     * @return The parsed integer, or {@code null} if invalid.
     */
    public Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Parses a decimal value safely.
     *
     * @param value The value to parse.
     * @return The parsed decimal, or {@code null} if invalid.
     */
    public Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.replace(',', '.');
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Checks whether the subscription deadline of the given proposal has expired.
     *
     * @param proposal The proposal to evaluate.
     * @return {@code true} if the deadline is in the past, {@code false} otherwise.
     */
    public boolean isDeadlineExpired(Proposal proposal) {
        LocalDate deadline = parseIsoDate(proposal.getFieldValues().get(DEADLINE_FIELD_NAME));
        return deadline != null && LocalDate.now().isAfter(deadline);
    }

    /**
     * Checks whether subscriptions and cancellations are still allowed for the given proposal.
     *
     * @param proposal The proposal to evaluate.
     * @return {@code true} if the subscription deadline has not passed yet, {@code false} otherwise.
     */
    public boolean isSubscriptionWindowOpen(Proposal proposal) {
        if (proposal == null) {
            return false;
        }
        LocalDate deadline = parseIsoDate(proposal.getFieldValues().get(DEADLINE_FIELD_NAME));
        return deadline != null && !LocalDate.now().isAfter(deadline);
    }

    /**
     * Checks whether the proposal can still be withdrawn.
     *
     * @param proposal The proposal to evaluate.
     * @return {@code true} if the proposal is open or confirmed and its start date is still in the future.
     */
    public boolean canWithdrawProposal(Proposal proposal) {
        if (proposal == null) {
            return false;
        }
        ProposalStatus status = proposal.getCurrentStatus();
        if (status != ProposalStatus.OPEN && status != ProposalStatus.CONFIRMED) {
            return false;
        }
        LocalDate startDate = parseIsoDate(proposal.getFieldValues().get(START_DATE_FIELD_NAME));
        return startDate != null && LocalDate.now().isBefore(startDate);
    }

    /**
     * Checks whether the current date is after the proposal end date.
     *
     * @param proposal The proposal to evaluate.
     * @return {@code true} if the proposal end date has passed, {@code false} otherwise.
     */
    public boolean isAfterEndDate(Proposal proposal) {
        LocalDate endDate = parseIsoDate(proposal.getFieldValues().get(END_DATE_FIELD_NAME));
        return endDate != null && LocalDate.now().isAfter(endDate);
    }
}
