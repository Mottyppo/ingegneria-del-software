package it.unibs.ingesw.test;

import it.unibs.ingesw.service.proposal.ProposalRuleValidator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Black-box tests for the proposal validity rules required before publication.
 */
public class ProposalRuleValidatorBlackBoxTest {
    private static final String DEADLINE = "Termine ultimo di iscrizione";
    private static final String START_DATE = "Data";
    private static final String END_DATE = "Data conclusiva";
    private static final String PARTICIPANTS = "Numero di partecipanti";
    private static final String FEE = "Quota individuale";
    private static final String OVERSIZED_INPUT = "9".repeat(10_000);

    private final ProposalRuleValidator validator = new ProposalRuleValidator();

    @Test
    void acceptsFutureDeadlineEquivalenceClass() {
        LocalDate deadline = LocalDate.now().plusDays(10);

        assertTrue(validator.checkDomainRules(validValues(deadline)));
    }

    @Test
    void acceptsStartDateAfterMinimumEquivalenceClass() {
        LocalDate deadline = LocalDate.now().plusDays(10);

        assertTrue(validator.checkDomainRules(with(deadline, START_DATE, deadline.plusDays(3).toString())));
    }

    @Test
    void acceptsEndDateAfterStartEquivalenceClass() {
        LocalDate deadline = LocalDate.now().plusDays(10);
        LocalDate endDate = deadline.plusDays(4);

        assertTrue(validator.checkDomainRules(with(deadline, END_DATE, endDate.toString())));
    }

    @Test
    void acceptsPositiveParticipantsEquivalenceClass() {
        assertTrue(validator.checkDomainRules(with(PARTICIPANTS, "20")));
    }

    @Test
    void acceptsPositiveFeeEquivalenceClass() {
        assertTrue(validator.checkDomainRules(with(FEE, "12.5")));
    }

    @Test
    void rejectsDeadlineNotAfterTodayEquivalenceClass() {
        assertFalse(validator.checkDomainRules(with(DEADLINE, LocalDate.now().minusDays(1).toString())));
    }

    @Test
    void rejectsStartDateBeforeMinimumEquivalenceClass() {
        LocalDate deadline = LocalDate.now().plusDays(10);

        assertFalse(validator.checkDomainRules(with(deadline, START_DATE, deadline.plusDays(1).toString())));
    }

    @Test
    void rejectsEndDateBeforeStartEquivalenceClass() {
        LocalDate deadline = LocalDate.now().plusDays(10);
        LocalDate startDate = deadline.plusDays(3);

        assertFalse(validator.checkDomainRules(with(deadline, END_DATE, startDate.minusDays(1).toString())));
    }

    @Test
    void rejectsNonPositiveParticipantsEquivalenceClass() {
        assertFalse(validator.checkDomainRules(with(PARTICIPANTS, "-5")));
    }

    @Test
    void rejectsNegativeFeeEquivalenceClass() {
        assertFalse(validator.checkDomainRules(with(FEE, "-12.5")));
    }

    @Test
    void rejectsNonCanonicalDateEquivalenceClass() {
        assertFalse(validator.checkDomainRules(with(DEADLINE, "10/12/2030")));
    }

    @Test
    void rejectsNonCanonicalStartDateEquivalenceClass() {
        assertFalse(validator.checkDomainRules(with(START_DATE, "13/12/2030")));
    }

    @Test
    void rejectsNonCanonicalEndDateEquivalenceClass() {
        assertFalse(validator.checkDomainRules(with(END_DATE, "13/12/2030")));
    }

    @Test
    void rejectsNonNumericParticipantsEquivalenceClass() {
        assertFalse(validator.checkDomainRules(with(PARTICIPANTS, "dieci")));
    }

    @Test
    void rejectsNonNumericFeeEquivalenceClass() {
        assertFalse(validator.checkDomainRules(with(FEE, "gratis")));
    }

    @Test
    void acceptsDeadlineBoundaryTomorrow() {
        LocalDate deadline = LocalDate.now().plusDays(1);

        assertTrue(validator.checkDomainRules(validValues(deadline)));
    }

    @Test
    void rejectsDeadlineBoundaryToday() {
        assertFalse(validator.checkDomainRules(with(DEADLINE, LocalDate.now().toString())));
    }

    @Test
    void acceptsStartDateBoundaryDeadlinePlusTwoDays() {
        LocalDate deadline = LocalDate.now().plusDays(1);
        Map<String, String> values = validValues(deadline);

        values.put(START_DATE, deadline.plusDays(2).toString());

        assertTrue(validator.checkDomainRules(values));
    }

    @Test
    void rejectsStartDateBoundaryDeadlinePlusOneDay() {
        LocalDate deadline = LocalDate.now().plusDays(1);

        assertFalse(validator.checkDomainRules(with(deadline, START_DATE, deadline.plusDays(1).toString())));
    }

    @Test
    void acceptsEndDateBoundarySameAsStartDate() {
        LocalDate deadline = LocalDate.now().plusDays(1);
        LocalDate startDate = deadline.plusDays(2);
        Map<String, String> values = validValues(deadline);

        values.put(START_DATE, startDate.toString());
        values.put(END_DATE, startDate.toString());

        assertTrue(validator.checkDomainRules(values));
    }

    @Test
    void rejectsEndDateBoundaryDayBeforeStartDate() {
        LocalDate deadline = LocalDate.now().plusDays(1);
        LocalDate startDate = deadline.plusDays(2);
        Map<String, String> values = validValues(deadline);

        values.put(START_DATE, startDate.toString());
        values.put(END_DATE, startDate.minusDays(1).toString());

        assertFalse(validator.checkDomainRules(values));
    }

    @Test
    void acceptsParticipantsBoundaryOne() {
        assertTrue(validator.checkDomainRules(with(PARTICIPANTS, "1")));
    }

    @Test
    void rejectsParticipantsBoundaryZero() {
        assertFalse(validator.checkDomainRules(with(PARTICIPANTS, "0")));
    }

    @Test
    void acceptsFeeBoundaryZero() {
        assertTrue(validator.checkDomainRules(with(FEE, "0")));
    }

    @Test
    void rejectsFeeBoundaryBelowZero() {
        assertFalse(validator.checkDomainRules(with(FEE, "-0.01")));
    }

    @Test
    void whittakerRejectsMissingValueMapWithoutCrashing() {
        assertFalse(assertDoesNotThrow(() -> validator.checkDomainRules(null)));
    }

    @Test
    void whittakerRejectsBlankRequiredValues() {
        assertFalse(validator.checkDomainRules(with(DEADLINE, "")));
        assertFalse(validator.checkDomainRules(with(START_DATE, " ")));
        assertFalse(validator.checkDomainRules(with(END_DATE, "\t")));
        assertFalse(validator.checkDomainRules(with(PARTICIPANTS, "")));
        assertFalse(validator.checkDomainRules(with(FEE, " ")));
    }

    @Test
    void whittakerRejectsOversizedDeadlineInputWithoutCrashing() {
        assertFalse(assertDoesNotThrow(() -> validator.checkDomainRules(with(DEADLINE, OVERSIZED_INPUT))));
    }

    @Test
    void whittakerRejectsOversizedStartDateInputWithoutCrashing() {
        assertFalse(assertDoesNotThrow(() -> validator.checkDomainRules(with(START_DATE, OVERSIZED_INPUT))));
    }

    @Test
    void whittakerRejectsOversizedEndDateInputWithoutCrashing() {
        assertFalse(assertDoesNotThrow(() -> validator.checkDomainRules(with(END_DATE, OVERSIZED_INPUT))));
    }

    @Test
    void whittakerRejectsOversizedParticipantsInputWithoutCrashing() {
        assertFalse(assertDoesNotThrow(() -> validator.checkDomainRules(with(PARTICIPANTS, OVERSIZED_INPUT))));
    }

    @Test
    void whittakerRejectsOversizedFeeInputWithoutCrashing() {
        assertFalse(assertDoesNotThrow(() -> validator.checkDomainRules(with(FEE, OVERSIZED_INPUT))));
    }

    private Map<String, String> validValues(LocalDate deadline) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(DEADLINE, deadline.toString());
        values.put(START_DATE, deadline.plusDays(3).toString());
        values.put(END_DATE, deadline.plusDays(3).toString());
        values.put(PARTICIPANTS, "20");
        values.put(FEE, "12.5");
        return values;
    }

    private Map<String, String> with(String field, String value) {
        return with(LocalDate.now().plusDays(1), field, value);
    }

    private Map<String, String> with(LocalDate deadline, String field, String value) {
        Map<String, String> values = validValues(deadline);
        values.put(field, value);
        return values;
    }
}
