package it.unibs.ingesw.test;

import it.unibs.ingesw.model.Archive;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArchiveTest {

    @Test
    void nextIdStartsFromOneAndIncrements() {
        Archive archive = new Archive();
        assertEquals(1, archive.nextId());

        Proposal proposal = new Proposal(archive.nextId(), "Sport", Map.of("Titolo", "Torneo"));
        assertTrue(archive.saveProposal(proposal));

        assertEquals(2, archive.nextId());
    }

    @Test
    void groupBoardByCategory() {
        Archive archive = new Archive();

        Proposal sport = new Proposal(1, "Sport", Map.of("Titolo", "Partita"));
        sport.markAsValid();
        sport.markAsOpen();

        Proposal cinema = new Proposal(2, "Cinema", Map.of("Titolo", "Film"));
        cinema.markAsValid();
        cinema.markAsOpen();

        assertTrue(archive.saveProposal(sport));
        assertTrue(archive.saveProposal(cinema));

        Map<String, List<Proposal>> board = archive.getOpenByCategory();
        assertEquals(2, board.size());
        assertEquals(1, board.get("Sport").size());
        assertEquals(1, board.get("Cinema").size());
    }

    @Test
    void filterByStatusReturnsOnlyMatchingProposals() {
        Archive archive = new Archive();

        Proposal created = new Proposal(1, "Sport", Map.of("Titolo", "Camminata"));
        Proposal valid = new Proposal(2, "Sport", Map.of("Titolo", "Torneo"));
        valid.markAsValid();

        assertTrue(archive.saveProposal(created));
        assertTrue(archive.saveProposal(valid));

        List<Proposal> createdOnly = archive.getByStatus(ProposalStatus.CREATED);
        List<Proposal> validOnly = archive.getByStatus(ProposalStatus.VALID);

        assertEquals(1, createdOnly.size());
        assertEquals(1, validOnly.size());
        assertEquals(1, createdOnly.getFirst().getId());
        assertEquals(2, validOnly.getFirst().getId());
    }

    @Test
    void findByIdReturnsMatchingProposal() {
        Archive archive = new Archive();
        Proposal proposal = new Proposal(9, "Sport", Map.of("Titolo", "Gara"));
        proposal.markAsValid();
        proposal.markAsOpen();

        assertTrue(archive.saveProposal(proposal));
        assertNotNull(archive.findById(9));
        assertNull(archive.findById(99));
    }

    @Test
    void boardContainsOnlyOpenProposals() {
        Archive archive = new Archive();

        Proposal open = new Proposal(1, "Sport", Map.of("Titolo", "Open"));
        open.markAsValid();
        open.markAsOpen();

        Proposal confirmed = new Proposal(2, "Sport", Map.of("Titolo", "Confirmed"));
        confirmed.markAsValid();
        confirmed.markAsOpen();
        confirmed.markAsConfirmed();

        assertTrue(archive.saveProposal(open));
        assertTrue(archive.saveProposal(confirmed));

        Map<String, List<Proposal>> board = archive.getOpenByCategory();
        assertEquals(1, board.get("Sport").size());
        assertEquals(1, board.get("Sport").getFirst().getId());
    }
}
