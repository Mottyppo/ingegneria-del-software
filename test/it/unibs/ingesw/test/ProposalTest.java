package it.unibs.ingesw.test;

import it.unibs.ingesw.model.DataType;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProposalTest {

    @Test
    void createAndReadInitialProposalState() {
        Proposal proposal = new Proposal(
                1,
                "Sport",
                Map.of("Titolo", "Partita amichevole"),
                Map.of("Titolo", DataType.STRING)
        );

        assertEquals(1, proposal.getId());
        assertEquals("Sport", proposal.getCategoryName());
        assertEquals("Partita amichevole", proposal.getFieldValues().get("Titolo"));
        assertEquals(DataType.STRING, proposal.getFieldType("Titolo"));
        assertEquals(ProposalStatus.CREATED, proposal.getCurrentStatus());
        assertEquals(1, proposal.getStatusHistory().size());
        assertEquals(ProposalStatus.CREATED, proposal.getStatusHistory().getFirst().getStatus());
        assertNull(proposal.getPublicationDate());
        assertTrue(proposal.getSubscribers().isEmpty());
    }

    @Test
    void transitionFromCreatedToValidToOpen() {
        Proposal proposal = new Proposal(7, "Musica", Map.of("Titolo", "Concerto"));

        assertTrue(proposal.markAsValid());
        assertEquals(ProposalStatus.VALID, proposal.getCurrentStatus());
        assertEquals(2, proposal.getStatusHistory().size());

        assertTrue(proposal.markAsOpen());
        assertEquals(ProposalStatus.OPEN, proposal.getCurrentStatus());
        LocalDateTime actualDate = LocalDateTime.parse(proposal.getPublicationDate());
        assertEquals(LocalDateTime.now().getYear(), actualDate.getYear());
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

    @Test
    void manageSubscribersOnlyWhenOpenAndWithoutDuplicates() {
        Proposal proposal = new Proposal(4, "Sport", Map.of("Titolo", "Partita"));

        assertFalse(proposal.addSubscriber("mario", 2));
        assertTrue(proposal.markAsValid());
        assertTrue(proposal.markAsOpen());

        assertTrue(proposal.addSubscriber("mario", 2));
        assertFalse(proposal.addSubscriber("mario", 2));
        assertFalse(proposal.addSubscriber("MARIO", 2));
        assertTrue(proposal.addSubscriber("luca", 2));
        assertFalse(proposal.addSubscriber("anna", 2));

        assertEquals(2, proposal.getSubscribers().size());
    }

    @Test
    void removeSubscribersOnlyWhenOpenAndAllowResubscription() {
        Proposal proposal = new Proposal(8, "Sport", Map.of("Titolo", "Torneo"));

        assertFalse(proposal.removeSubscriber("mario"));
        assertTrue(proposal.markAsValid());
        assertTrue(proposal.markAsOpen());
        assertTrue(proposal.addSubscriber("mario", 2));

        assertTrue(proposal.removeSubscriber("MARIO"));
        assertTrue(proposal.getSubscribers().isEmpty());
        assertTrue(proposal.addSubscriber("mario", 2));
        assertEquals(1, proposal.getSubscribers().size());
    }

    @Test
    void transitionFromOpenToConfirmedToClose() {
        Proposal proposal = new Proposal(5, "Gite", Map.of("Titolo", "Montisola"));
        assertTrue(proposal.markAsValid());
        assertTrue(proposal.markAsOpen());
        assertTrue(proposal.markAsConfirmed());
        assertTrue(proposal.markAsClose());

        assertEquals(ProposalStatus.CLOSE, proposal.getCurrentStatus());
        assertEquals(5, proposal.getStatusHistory().size());
        assertEquals(ProposalStatus.CLOSE, proposal.getStatusHistory().getLast().getStatus());
    }

    @Test
    void transitionFromOpenToCanceled() {
        Proposal proposal = new Proposal(6, "Cinema", Map.of("Titolo", "Film"));
        assertTrue(proposal.markAsValid());
        assertTrue(proposal.markAsOpen());
        assertTrue(proposal.markAsCanceled());
        assertFalse(proposal.markAsClose());

        assertEquals(ProposalStatus.CANCELED, proposal.getCurrentStatus());
    }

    @Test
    void transitionFromOpenOrConfirmedToWithdrawed() {
        Proposal openProposal = new Proposal(9, "Gite", Map.of("Titolo", "Weekend"));
        assertTrue(openProposal.markAsValid());
        assertTrue(openProposal.markAsOpen());
        assertTrue(openProposal.markAsWithdrawed());
        assertEquals(ProposalStatus.WITHDRAWED, openProposal.getCurrentStatus());
        assertEquals(ProposalStatus.WITHDRAWED, openProposal.getStatusHistory().getLast().getStatus());

        Proposal confirmedProposal = new Proposal(10, "Arte", Map.of("Titolo", "Mostra"));
        assertTrue(confirmedProposal.markAsValid());
        assertTrue(confirmedProposal.markAsOpen());
        assertTrue(confirmedProposal.markAsConfirmed());
        assertTrue(confirmedProposal.markAsWithdrawed());
        assertEquals(ProposalStatus.WITHDRAWED, confirmedProposal.getCurrentStatus());
        assertEquals(ProposalStatus.WITHDRAWED, confirmedProposal.getStatusHistory().getLast().getStatus());
    }

    @Test
    void rejectWithdrawedTransitionFromUnsupportedStates() {
        Proposal createdProposal = new Proposal(11, "Cinema", Map.of("Titolo", "Rassegna"));
        assertFalse(createdProposal.markAsWithdrawed());

        Proposal validProposal = new Proposal(12, "Cinema", Map.of("Titolo", "Rassegna 2"));
        assertTrue(validProposal.markAsValid());
        assertFalse(validProposal.markAsWithdrawed());

        Proposal closedProposal = new Proposal(13, "Cinema", Map.of("Titolo", "Rassegna 3"));
        assertTrue(closedProposal.markAsValid());
        assertTrue(closedProposal.markAsOpen());
        assertTrue(closedProposal.markAsConfirmed());
        assertTrue(closedProposal.markAsClose());
        assertFalse(closedProposal.markAsWithdrawed());
    }
}
