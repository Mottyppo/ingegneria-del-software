package it.unibs.ingesw.test;

import it.unibs.ingesw.model.Archive;
import it.unibs.ingesw.model.Proposal;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArchiveTest {

    @Test
    void nextIdStartsFromOneAndIncrements() {
        Archive archive = new Archive();
        assertEquals(1, archive.nextId());

        Proposal proposal = new Proposal(archive.nextId(), "Sport", Map.of("Titolo", "Torneo"));
        proposal.markAsValid();
        proposal.markAsOpen();
        assertTrue(archive.addOpenProposal(proposal));

        assertEquals(2, archive.nextId());
    }

    @Test
    void addOpenProposalRejectsNonOpenProposals() {
        Archive archive = new Archive();
        Proposal proposal = new Proposal(1, "Gite", Map.of("Titolo", "Montisola"));

        assertFalse(archive.addOpenProposal(proposal));
        assertTrue(archive.getProposals().isEmpty());
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

        assertTrue(archive.addOpenProposal(sport));
        assertTrue(archive.addOpenProposal(cinema));

        Map<String, List<Proposal>> board = archive.getOpenByCategory();
        assertEquals(2, board.size());
        assertEquals(1, board.get("Sport").size());
        assertEquals(1, board.get("Cinema").size());
    }
}
