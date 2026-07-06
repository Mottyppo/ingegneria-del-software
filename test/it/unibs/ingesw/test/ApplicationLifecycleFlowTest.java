package it.unibs.ingesw.test;

import it.unibs.ingesw.model.Configurator;
import it.unibs.ingesw.model.Notification;
import it.unibs.ingesw.model.Participant;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;
import it.unibs.ingesw.service.AuthenticationService;
import it.unibs.ingesw.service.proposal.ProposalLifecycleService;
import it.unibs.ingesw.service.proposal.ProposalService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Black-box tests for participant, lifecycle, notification, and withdrawal use cases.
 */
public class ApplicationLifecycleFlowTest {
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
    void participantRegistersViewsBoardAndSubscribesRespectingUniquenessAndCapacity() {
        TestApplicationContext context = BlackBoxTestSupport.configuredSportContext();
        AuthenticationService authenticationService = context.getAuthenticationService();
        ProposalService proposalService = context.getProposalService();

        assertNull(authenticationService.signUpParticipant("Mario", "Rossi", "crocerossaitaliana", "pwd"));
        Participant mario = authenticationService.signUpParticipant("Mario", "Rossi", "mrossi", "pwd");
        Participant luca = authenticationService.signUpParticipant("Luca", "Bianchi", "lbianchi", "pwd");
        assertNotNull(mario);
        assertNotNull(luca);
        assertNull(authenticationService.signUpParticipant("Marco", "Rossi", "MROSSI", "pwd2"));

        Configurator configurator = authenticationService.authenticateConfigurator("crocerossaitaliana", "ginevra1864");
        assertNotNull(configurator);
        assertFalse(authenticationService.updateCredentials(configurator, "mrossi", "newpass"));

        Proposal proposal = proposalService.createProposal(0, BlackBoxTestSupport.validSportValues(1));
        assertNotNull(proposal);
        assertTrue(proposalService.publishProposal(proposal));
        int proposalId = proposalService.getBoardByCategory().get("Sport").getFirst().getId();

        assertTrue(proposalService.subscribeParticipantToProposal(mario, proposalId));
        assertFalse(proposalService.subscribeParticipantToProposal(mario, proposalId));
        assertFalse(proposalService.subscribeParticipantToProposal(luca, proposalId));
        assertEquals(1, proposalService.getSubscribedOpenProposals(mario).size());

        Proposal persisted = BlackBoxTestSupport.newContext().getProposalService().getOpenProposals().getFirst();
        assertEquals(List.of("mrossi"), persisted.getSubscribers());
    }

    @Test
    void participantCanCancelSubscriptionBeforeDeadlineAndSubscribeAgain() {
        TestApplicationContext context = BlackBoxTestSupport.configuredSportContext();
        AuthenticationService authenticationService = context.getAuthenticationService();
        ProposalService proposalService = context.getProposalService();
        Participant participant = authenticationService.signUpParticipant("Mario", "Rossi", "mrossi", "pwd");
        Proposal proposal = proposalService.createProposal(0, BlackBoxTestSupport.validSportValues(2));
        assertNotNull(participant);
        assertNotNull(proposal);
        assertTrue(proposalService.publishProposal(proposal));
        int proposalId = proposalService.getOpenProposals().getFirst().getId();

        assertTrue(proposalService.subscribeParticipantToProposal(participant, proposalId));
        assertEquals(1, proposalService.getSubscribedOpenProposals(participant).size());
        assertTrue(proposalService.unsubscribeParticipantFromProposal(participant, proposalId));
        assertTrue(proposalService.getSubscribedOpenProposals(participant).isEmpty());
        assertTrue(proposalService.subscribeParticipantToProposal(participant, proposalId));
        assertEquals(1, proposalService.getOpenProposals().getFirst().getSubscribers().size());
    }

    @Test
    void participantCannotCancelSubscriptionAfterDeadlineBoundary() {
        Participant mario = BlackBoxTestSupport.participant("Mario", "Rossi", "mrossi");
        BlackBoxTestSupport.persistParticipants(mario);
        BlackBoxTestSupport.persistArchive(BlackBoxTestSupport.openProposal(
                20,
                2,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(3),
                LocalDate.now().plusDays(3),
                "mrossi"
        ));

        TestApplicationContext context = BlackBoxTestSupport.newContext();
        Participant loaded = context.getAuthenticationService().authenticateParticipant("mrossi", "pwd");
        assertNotNull(loaded);

        assertFalse(context.getProposalService().unsubscribeParticipantFromProposal(loaded, 20));
        Proposal persisted = BlackBoxTestSupport.newContext().getProposalService().getArchivedProposals().getFirst();
        assertEquals(List.of("mrossi"), persisted.getSubscribers());
    }

    @Test
    void expiredOpenProposalsBecomeConfirmedOrCanceledAndNotifySubscribers() {
        Participant mario = BlackBoxTestSupport.participant("Mario", "Rossi", "mrossi");
        Participant luca = BlackBoxTestSupport.participant("Luca", "Bianchi", "lbianchi");
        BlackBoxTestSupport.persistParticipants(mario, luca);
        BlackBoxTestSupport.persistArchive(
                BlackBoxTestSupport.openProposal(
                        1,
                        2,
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(3),
                        LocalDate.now().plusDays(3),
                        "mrossi",
                        "lbianchi"
                ),
                BlackBoxTestSupport.openProposal(
                        2,
                        3,
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(3),
                        LocalDate.now().plusDays(3),
                        "mrossi"
                )
        );

        TestApplicationContext context = BlackBoxTestSupport.newContext();
        ProposalService proposalService = context.getProposalService();
        ProposalLifecycleService lifecycleService = context.getProposalLifecycleService();
        lifecycleService.refreshProposalLifecycle();

        Proposal confirmed = proposalService.getArchivedProposals().stream()
                .filter(proposal -> proposal.getId() == 1)
                .findFirst()
                .orElseThrow();
        Proposal canceled = proposalService.getArchivedProposals().stream()
                .filter(proposal -> proposal.getId() == 2)
                .findFirst()
                .orElseThrow();

        assertEquals(ProposalStatus.CONFIRMED, confirmed.getCurrentStatus());
        assertEquals(ProposalStatus.CANCELED, canceled.getCurrentStatus());

        Participant marioLoaded = context.getAuthenticationService().authenticateParticipant("mrossi", "pwd");
        Participant lucaLoaded = context.getAuthenticationService().authenticateParticipant("lbianchi", "pwd");
        assertNotNull(marioLoaded);
        assertNotNull(lucaLoaded);

        List<Notification> marioNotifications = proposalService.getParticipantNotifications(marioLoaded);
        List<Notification> lucaNotifications = proposalService.getParticipantNotifications(lucaLoaded);
        assertEquals(2, marioNotifications.size());
        assertEquals(1, lucaNotifications.size());
        assertTrue(marioNotifications.stream().anyMatch(notification -> notification.getMessage().contains("confermata")));
        assertTrue(marioNotifications.stream().anyMatch(notification -> notification.getMessage().contains("annullata")));
    }

    @Test
    void confirmedProposalBecomesClosedAfterEndDate() {
        BlackBoxTestSupport.persistArchive(BlackBoxTestSupport.confirmedProposal(
                30,
                1,
                LocalDate.now().minusDays(5),
                LocalDate.now().minusDays(3),
                LocalDate.now().minusDays(1),
                "mrossi"
        ));

        TestApplicationContext context = BlackBoxTestSupport.newContext();
        context.getProposalLifecycleService().refreshProposalLifecycle();

        Proposal closed = context.getProposalService().getArchivedProposals().getFirst();
        assertEquals(ProposalStatus.CLOSE, closed.getCurrentStatus());
    }

    @Test
    void configuratorWithdrawsOpenProposalBeforeStartButNotOnStartDateBoundary() {
        Participant mario = BlackBoxTestSupport.participant("Mario", "Rossi", "mrossi");
        BlackBoxTestSupport.persistParticipants(mario);
        BlackBoxTestSupport.persistArchive(
                BlackBoxTestSupport.openProposal(
                        40,
                        2,
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(3),
                        LocalDate.now().plusDays(3),
                        "mrossi"
                ),
                BlackBoxTestSupport.openProposal(
                        41,
                        2,
                        LocalDate.now().minusDays(1),
                        LocalDate.now(),
                        LocalDate.now(),
                        "mrossi"
                )
        );

        TestApplicationContext context = BlackBoxTestSupport.newContext();
        ProposalService proposalService = context.getProposalService();
        Proposal withdrawable = proposalService.getArchivedProposals().stream()
                .filter(proposal -> proposal.getId() == 40)
                .findFirst()
                .orElseThrow();
        Proposal onStartDate = proposalService.getArchivedProposals().stream()
                .filter(proposal -> proposal.getId() == 41)
                .findFirst()
                .orElseThrow();

        assertTrue(proposalService.withdrawProposal(withdrawable));
        assertFalse(proposalService.withdrawProposal(onStartDate));

        TestApplicationContext reloaded = BlackBoxTestSupport.newContext();
        Proposal withdrawn = reloaded.getProposalService().getArchivedProposals().stream()
                .filter(proposal -> proposal.getId() == 40)
                .findFirst()
                .orElseThrow();
        Proposal stillOpen = reloaded.getProposalService().getArchivedProposals().stream()
                .filter(proposal -> proposal.getId() == 41)
                .findFirst()
                .orElseThrow();
        Participant notified = reloaded.getAuthenticationService().authenticateParticipant("mrossi", "pwd");

        assertEquals(ProposalStatus.WITHDRAWED, withdrawn.getCurrentStatus());
        assertEquals(ProposalStatus.OPEN, stillOpen.getCurrentStatus());
        assertTrue(reloaded.getProposalService().getBoardByCategory().get("Sport").stream()
                .noneMatch(proposal -> proposal.getId() == 40));
        assertNotNull(notified);
        assertTrue(reloaded.getProposalService().getParticipantNotifications(notified).stream()
                .anyMatch(notification -> notification.getMessage().contains("ritirata")));
    }

    @Test
    void personalSpacePersistsAfterSelectiveNotificationRemoval() {
        Participant participant = BlackBoxTestSupport.participant("Mario", "Rossi", "mrossi");
        participant.getPersonalSpace().addNotification("Notifica da conservare");
        participant.getPersonalSpace().addNotification("Notifica da cancellare");
        BlackBoxTestSupport.persistParticipants(participant);

        TestApplicationContext context = BlackBoxTestSupport.newContext();
        Participant loaded = context.getAuthenticationService().authenticateParticipant("mrossi", "pwd");
        assertNotNull(loaded);
        assertEquals(2, context.getProposalService().getParticipantNotifications(loaded).size());

        assertTrue(context.getProposalService().removeParticipantNotification(loaded, 1));

        TestApplicationContext reloaded = BlackBoxTestSupport.newContext();
        Participant loadedAgain = reloaded.getAuthenticationService().authenticateParticipant("mrossi", "pwd");
        assertNotNull(loadedAgain);
        List<Notification> notifications = reloaded.getProposalService().getParticipantNotifications(loadedAgain);
        assertEquals(1, notifications.size());
        assertEquals("Notifica da conservare", notifications.getFirst().getMessage());
    }
}
