package it.unibs.ingesw.test;

import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;
import it.unibs.ingesw.service.proposal.ProposalService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Black-box tests for the proposal creation and publication use cases from version 2.
 */
public class ProposalServiceFlowTest {
    @TempDir
    Path tempDir;

    @BeforeEach
    void setDataDir() {
        System.setProperty("ingesw.data.dir", tempDir.toString());
    }

    @AfterEach
    void clearDataDir() {
        System.clearProperty("ingesw.data.dir");
    }

    @Test
    void configuratorCreatesPublishesAndViewsProposalByCategory() {
        TestApplicationContext context = BlackBoxTestSupport.configuredSportContext();
        ProposalService proposalService = context.getProposalService();

        Proposal proposal = proposalService.createProposal(0, BlackBoxTestSupport.validSportValues(20));
        assertNotNull(proposal);
        assertEquals(ProposalStatus.VALID, proposal.getCurrentStatus());
        assertEquals(1, proposalService.getValidProposals().size());

        assertTrue(proposalService.publishProposal(proposal));
        assertTrue(proposalService.getValidProposals().isEmpty());
        assertEquals(1, proposalService.getOpenProposals().size());

        Map<String, List<Proposal>> board = proposalService.getBoardByCategory();
        assertEquals(1, board.size());
        assertEquals(proposal.getId(), board.get("Sport").getFirst().getId());

        TestApplicationContext reloaded = BlackBoxTestSupport.newContext();
        Proposal persisted = reloaded.getProposalService().getArchivedProposals().getFirst();
        assertEquals(ProposalStatus.OPEN, persisted.getCurrentStatus());
        assertNotNull(persisted.getPublicationDate());
    }

    @Test
    void configuratorCannotPublishProposalOutsideValidDateEquivalenceClass() {
        TestApplicationContext context = BlackBoxTestSupport.configuredSportContext();
        ProposalService proposalService = context.getProposalService();
        LocalDate deadline = LocalDate.now().plusDays(2);
        Map<String, String> values = BlackBoxTestSupport.sportValues(
                10,
                deadline,
                deadline.plusDays(1),
                deadline.plusDays(1)
        );

        Proposal proposal = proposalService.createProposal(0, values);

        assertNotNull(proposal);
        assertEquals(ProposalStatus.CREATED, proposal.getCurrentStatus());
        assertTrue(proposalService.getValidProposals().isEmpty());
        assertFalse(proposalService.publishProposal(proposal));
        assertTrue(proposalService.getOpenProposals().isEmpty());

        Proposal persisted = BlackBoxTestSupport.newContext().getProposalService().getArchivedProposals().getFirst();
        assertEquals(ProposalStatus.CREATED, persisted.getCurrentStatus());
    }

    @Test
    void configuratorCannotCreateProposalWithMissingMandatorySpecificField() {
        TestApplicationContext context = BlackBoxTestSupport.configuredSportContext();
        ProposalService proposalService = context.getProposalService();

        Proposal proposal = proposalService.createProposal(
                0,
                BlackBoxTestSupport.withoutField(
                        BlackBoxTestSupport.validSportValues(12),
                        "Certificato medico"
                )
        );

        assertNull(proposal);
        assertTrue(proposalService.getArchivedProposals().isEmpty());
        assertTrue(BlackBoxTestSupport.newContext().getProposalService().getArchivedProposals().isEmpty());
    }
}
