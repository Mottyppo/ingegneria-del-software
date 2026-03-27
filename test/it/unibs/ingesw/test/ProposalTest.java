package it.unibs.ingesw.test;

import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProposalTest {

    @Test
    void createAndReadInitialProposalState() {
        Proposal proposal = new Proposal(1, "Sport", Map.of("Titolo", "Partita amichevole"));

        assertEquals(1, proposal.getId());
        assertEquals("Sport", proposal.getCategoryName());
        assertEquals("Partita amichevole", proposal.getFieldValues().get("Titolo"));
        assertEquals(ProposalStatus.CREATED, proposal.getCurrentStatus());
        assertEquals(1, proposal.getStatusHistory().size());
        assertEquals(ProposalStatus.CREATED, proposal.getStatusHistory().getFirst().getStatus());
        assertNull(proposal.getPublicationDate());
    }

    @Test
    void transitionFromCreatedToValidToOpen() {
        Proposal proposal = new Proposal(7, "Musica", Map.of("Titolo", "Concerto"));

        assertTrue(proposal.markAsValid());
        assertEquals(ProposalStatus.VALID, proposal.getCurrentStatus());
        assertEquals(2, proposal.getStatusHistory().size());

        assertTrue(proposal.markAsOpen());
        assertEquals(ProposalStatus.OPEN, proposal.getCurrentStatus());
        LocalDate actualDate = LocalDateTime.parse(proposal.getPublicationDate()).toLocalDate();
        assertEquals(LocalDate.now(), actualDate);
        assertEquals(3, proposal.getStatusHistory().size());
        assertEquals(ProposalStatus.OPEN, proposal.getStatusHistory().getLast().getStatus());
    }

    @Test
    void rejectInvalidStatusTransitions() {
        Proposal proposal = new Proposal(3, "Cinema", Map.of("Titolo", "Proiezione"));

        assertFalse(proposal.markAsOpen());
        assertTrue(proposal.markAsValid());
        assertFalse(proposal.markAsValid());
    }
}
