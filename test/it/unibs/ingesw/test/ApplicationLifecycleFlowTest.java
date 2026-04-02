package it.unibs.ingesw.test;

import it.unibs.ingesw.application.ApplicationContext;
import it.unibs.ingesw.model.Archive;
import it.unibs.ingesw.model.Configurator;
import it.unibs.ingesw.model.DataType;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.FieldType;
import it.unibs.ingesw.model.Participant;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;
import it.unibs.ingesw.persistence.ArchiveRepository;
import it.unibs.ingesw.persistence.JsonArchiveRepository;
import it.unibs.ingesw.persistence.JsonCategoryRepository;
import it.unibs.ingesw.persistence.JsonConfigRepository;
import it.unibs.ingesw.persistence.JsonConfiguratorRepository;
import it.unibs.ingesw.persistence.JsonParticipantRepository;
import it.unibs.ingesw.persistence.ParticipantRepository;
import it.unibs.ingesw.service.AuthenticationService;
import it.unibs.ingesw.service.ConfigurationService;
import it.unibs.ingesw.service.ProposalLifecycleService;
import it.unibs.ingesw.service.ProposalService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for lifecycle, notification, and persistence flows.
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
    void signUpEnforcesGlobalUsernameUniqueness() {
        ApplicationContext context = newContext();
        AuthenticationService authenticationService = context.getAuthenticationService();

        assertNull(authenticationService.signUpParticipant("Mario", "Rossi", "crocerossaitaliana", "pwd"));

        Participant created = authenticationService.signUpParticipant("Mario", "Rossi", "mrossi", "pwd");
        assertNotNull(created);
        assertNull(authenticationService.signUpParticipant("Marco", "Rossi", "MROSSI", "pwd2"));

        Configurator configurator = authenticationService.authenticateConfigurator("crocerossaitaliana", "ginevra1864");
        assertNotNull(configurator);
        assertFalse(authenticationService.updateCredentials(configurator, "mrossi", "newpass"));
    }

    @Test
    void subscribeRespectsUniquenessAndCapacity() {
        ApplicationContext context = prepareContextWithSportCategory();
        AuthenticationService authenticationService = context.getAuthenticationService();
        ProposalService proposalService = context.getProposalService();
        ProposalLifecycleService lifecycleService = context.getProposalLifecycleService();

        Participant first = authenticationService.signUpParticipant("Mario", "Rossi", "mrossi", "pwd");
        Participant second = authenticationService.signUpParticipant("Luca", "Bianchi", "lbianchi", "pwd");
        assertNotNull(first);
        assertNotNull(second);

        Proposal proposal = proposalService.createProposal(0, validRawValuesWithParticipants(1));
        assertNotNull(proposal);
        assertTrue(proposalService.publishProposal(proposal));

        lifecycleService.refreshProposalLifecycle();
        int openProposalId = proposalService.getOpenProposals().getFirst().getId();
        assertTrue(proposalService.subscribeParticipantToProposal(first, openProposalId));
        assertFalse(proposalService.subscribeParticipantToProposal(first, openProposalId));
        assertFalse(proposalService.subscribeParticipantToProposal(second, openProposalId));
    }

    @Test
    void unsubscribeWithinDeadlineRemovesSubscriberAndAllowsResubscription() {
        ApplicationContext context = prepareContextWithSportCategory();
        AuthenticationService authenticationService = context.getAuthenticationService();
        ProposalService proposalService = context.getProposalService();

        Participant participant = authenticationService.signUpParticipant("Mario", "Rossi", "mrossi", "pwd");
        assertNotNull(participant);

        Proposal proposal = proposalService.createProposal(0, validRawValuesWithParticipants(2));
        assertNotNull(proposal);
        assertTrue(proposalService.publishProposal(proposal));

        int proposalId = proposalService.getOpenProposals().getFirst().getId();
        assertTrue(proposalService.subscribeParticipantToProposal(participant, proposalId));
        assertEquals(1, proposalService.getSubscribedOpenProposals(participant).size());

        assertTrue(proposalService.unsubscribeParticipantFromProposal(participant, proposalId));
        assertTrue(proposalService.getSubscribedOpenProposals(participant).isEmpty());
        assertTrue(proposalService.getOpenProposals().getFirst().getSubscribers().isEmpty());

        assertTrue(proposalService.subscribeParticipantToProposal(participant, proposalId));
        assertEquals(1, proposalService.getOpenProposals().getFirst().getSubscribers().size());
    }

    @Test
    void unsubscribeFailsAfterDeadline() {
        ParticipantRepository participantRepository = new JsonParticipantRepository();
        ArchiveRepository archiveRepository = new JsonArchiveRepository();
        Participant participant = new Participant("Mario", "Rossi", "mrossi", "pwd");
        participantRepository.writeAll(List.of(participant));

        Archive archive = new Archive();
        Proposal expiredProposal = buildOpenProposal(
                20,
                1,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(2)
        );
        assertTrue(expiredProposal.addSubscriber("mrossi", 1));
        archive.saveProposal(expiredProposal);
        archiveRepository.write(archive);

        ApplicationContext context = newContext();
        AuthenticationService authenticationService = context.getAuthenticationService();
        ProposalService proposalService = context.getProposalService();

        Participant loaded = authenticationService.authenticateParticipant("mrossi", "pwd");
        assertNotNull(loaded);
        assertFalse(proposalService.unsubscribeParticipantFromProposal(loaded, 20));
        assertEquals(1, proposalService.getArchivedProposals().getFirst().getSubscribers().size());
    }

    @Test
    void transitionsFromOpenToConfirmedOrCanceledGenerateNotifications() {
        ParticipantRepository participantRepository = new JsonParticipantRepository();
        ArchiveRepository archiveRepository = new JsonArchiveRepository();
        Participant mario = new Participant("Mario", "Rossi", "mrossi", "pwd");
        Participant luca = new Participant("Luca", "Bianchi", "lbianchi", "pwd");
        participantRepository.writeAll(List.of(mario, luca));

        Archive archive = new Archive();
        Proposal confirmed = buildOpenProposal(
                1,
                2,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(3)
        );
        confirmed.addSubscriber("mrossi", 2);
        confirmed.addSubscriber("lbianchi", 2);

        Proposal canceled = buildOpenProposal(
                2,
                3,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(3)
        );
        canceled.addSubscriber("mrossi", 3);

        archive.saveProposal(confirmed);
        archive.saveProposal(canceled);
        archiveRepository.write(archive);

        ApplicationContext context = newContext();
        ProposalService proposalService = context.getProposalService();
        AuthenticationService authenticationService = context.getAuthenticationService();
        ProposalLifecycleService lifecycleService = context.getProposalLifecycleService();

        lifecycleService.refreshProposalLifecycle();

        Proposal confirmedAfter = proposalService.getArchivedProposals().stream()
                .filter(p -> p.getId() == 1)
                .findFirst()
                .orElseThrow();
        Proposal canceledAfter = proposalService.getArchivedProposals().stream()
                .filter(p -> p.getId() == 2)
                .findFirst()
                .orElseThrow();

        assertEquals(ProposalStatus.CONFIRMED, confirmedAfter.getCurrentStatus());
        assertEquals(ProposalStatus.CANCELED, canceledAfter.getCurrentStatus());

        Participant marioLoaded = authenticationService.authenticateParticipant("mrossi", "pwd");
        Participant lucaLoaded = authenticationService.authenticateParticipant("lbianchi", "pwd");
        assertNotNull(marioLoaded);
        assertNotNull(lucaLoaded);

        assertEquals(2, proposalService.getParticipantNotifications(marioLoaded).size());
        assertEquals(1, proposalService.getParticipantNotifications(lucaLoaded).size());
    }

    @Test
    void confirmedProposalBecomesCloseAfterEndDate() {
        ArchiveRepository archiveRepository = new JsonArchiveRepository();
        Archive archive = new Archive();
        Proposal proposal = buildOpenProposal(
                10,
                1,
                LocalDate.now().minusDays(1),
                LocalDate.now().minusDays(1)
        );
        proposal.addSubscriber("mrossi", 1);
        assertTrue(proposal.markAsConfirmed());
        archive.saveProposal(proposal);
        archiveRepository.write(archive);

        ApplicationContext context = newContext();
        context.getProposalLifecycleService().refreshProposalLifecycle();
        Proposal closed = context.getProposalService().getArchivedProposals().stream()
                .filter(p -> p.getId() == 10)
                .findFirst()
                .orElseThrow();
        assertEquals(ProposalStatus.CLOSE, closed.getCurrentStatus());
    }

    @Test
    void withdrawingOpenProposalRemovesItFromBoardAndNotifiesSubscribers() {
        ParticipantRepository participantRepository = new JsonParticipantRepository();
        ArchiveRepository archiveRepository = new JsonArchiveRepository();
        Participant participant = new Participant("Mario", "Rossi", "mrossi", "pwd");
        participantRepository.writeAll(List.of(participant));

        Archive archive = new Archive();
        Proposal proposal = buildOpenProposal(
                30,
                1,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3)
        );
        assertTrue(proposal.addSubscriber("mrossi", 1));
        archive.saveProposal(proposal);
        archiveRepository.write(archive);

        ApplicationContext context = newContext();
        ProposalService proposalService = context.getProposalService();
        Proposal openProposal = proposalService.getOpenProposals().getFirst();

        assertTrue(proposalService.withdrawProposal(openProposal));
        assertTrue(proposalService.getOpenProposals().isEmpty());
        assertTrue(proposalService.getBoardByCategory().isEmpty());

        ApplicationContext reloadedContext = newContext();
        Proposal withdrawn = reloadedContext.getProposalService().getArchivedProposals().stream()
                .filter(current -> current.getId() == 30)
                .findFirst()
                .orElseThrow();
        assertEquals(ProposalStatus.WITHDRAWED, withdrawn.getCurrentStatus());
        assertEquals(List.of("mrossi"), withdrawn.getSubscribers());

        Participant notified = reloadedContext.getAuthenticationService().authenticateParticipant("mrossi", "pwd");
        assertNotNull(notified);
        assertEquals(1, reloadedContext.getProposalService().getParticipantNotifications(notified).size());
        assertTrue(
                reloadedContext.getProposalService().getParticipantNotifications(notified).getFirst().getMessage().contains("ritirata")
        );
    }

    @Test
    void withdrawingConfirmedProposalPersistsHistoryAndKeepsSubscribersFrozen() {
        ParticipantRepository participantRepository = new JsonParticipantRepository();
        ArchiveRepository archiveRepository = new JsonArchiveRepository();
        Participant participant = new Participant("Mario", "Rossi", "mrossi", "pwd");
        participantRepository.writeAll(List.of(participant));

        Archive archive = new Archive();
        Proposal proposal = buildOpenProposal(
                31,
                1,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(4)
        );
        assertTrue(proposal.addSubscriber("mrossi", 1));
        assertTrue(proposal.markAsConfirmed());
        archive.saveProposal(proposal);
        archiveRepository.write(archive);

        ApplicationContext context = newContext();
        ProposalService proposalService = context.getProposalService();
        Proposal confirmedProposal = proposalService.getArchivedProposals().stream()
                .filter(current -> current.getId() == 31)
                .findFirst()
                .orElseThrow();
        assertTrue(proposalService.withdrawProposal(confirmedProposal));

        ApplicationContext reloadedContext = newContext();
        ProposalService reloadedProposalService = reloadedContext.getProposalService();
        Proposal withdrawn = reloadedProposalService.getArchivedProposals().stream()
                .filter(current -> current.getId() == 31)
                .findFirst()
                .orElseThrow();
        assertEquals(ProposalStatus.WITHDRAWED, withdrawn.getCurrentStatus());
        assertEquals(ProposalStatus.WITHDRAWED, withdrawn.getStatusHistory().getLast().getStatus());
        assertEquals(List.of("mrossi"), withdrawn.getSubscribers());

        Participant loaded = reloadedContext.getAuthenticationService().authenticateParticipant("mrossi", "pwd");
        assertNotNull(loaded);
        assertFalse(reloadedProposalService.unsubscribeParticipantFromProposal(loaded, 31));
        assertEquals(List.of("mrossi"), withdrawn.getSubscribers());
    }

    @Test
    void cannotWithdrawProposalOnOrAfterStartDate() {
        ArchiveRepository archiveRepository = new JsonArchiveRepository();
        Archive archive = new Archive();
        Proposal proposal = buildOpenProposal(
                32,
                1,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
        );
        archive.saveProposal(proposal);
        archiveRepository.write(archive);

        ApplicationContext context = newContext();
        ProposalService proposalService = context.getProposalService();
        Proposal openProposal = proposalService.getOpenProposals().getFirst();

        assertFalse(proposalService.withdrawProposal(openProposal));
        Proposal stillOpen = proposalService.getArchivedProposals().stream()
                .filter(current -> current.getId() == 32)
                .findFirst()
                .orElseThrow();
        assertEquals(ProposalStatus.OPEN, stillOpen.getCurrentStatus());
    }

    @Test
    void personalSpacePersistsAfterNotificationRemoval() {
        ParticipantRepository participantRepository = new JsonParticipantRepository();
        Participant participant = new Participant("Mario", "Rossi", "mrossi", "pwd");
        participant.getPersonalSpace().addNotification("Notifica iniziale");
        participantRepository.writeAll(List.of(participant));

        ApplicationContext firstContext = newContext();
        AuthenticationService firstAuthenticationService = firstContext.getAuthenticationService();
        ProposalService firstProposalService = firstContext.getProposalService();

        Participant loaded = firstAuthenticationService.authenticateParticipant("mrossi", "pwd");
        assertNotNull(loaded);
        assertEquals(1, firstProposalService.getParticipantNotifications(loaded).size());
        assertTrue(firstProposalService.removeParticipantNotification(loaded, 0));

        ApplicationContext secondContext = newContext();
        AuthenticationService secondAuthenticationService = secondContext.getAuthenticationService();
        ProposalService secondProposalService = secondContext.getProposalService();

        Participant loadedAgain = secondAuthenticationService.authenticateParticipant("mrossi", "pwd");
        assertNotNull(loadedAgain);
        assertTrue(secondProposalService.getParticipantNotifications(loadedAgain).isEmpty());
    }

    @Test
    void configuratorCanReadArchiveContent() {
        ApplicationContext context = prepareContextWithSportCategory();
        ProposalService proposalService = context.getProposalService();
        ProposalLifecycleService lifecycleService = context.getProposalLifecycleService();

        Proposal proposal = proposalService.createProposal(0, validRawValuesWithParticipants(3));
        assertNotNull(proposal);
        assertTrue(proposalService.publishProposal(proposal));
        lifecycleService.refreshProposalLifecycle();
        assertFalse(proposalService.getArchivedProposals().isEmpty());
    }

    private ApplicationContext newContext() {
        return new ApplicationContext(
                new JsonConfigRepository(),
                new JsonCategoryRepository(),
                new JsonConfiguratorRepository(),
                new JsonParticipantRepository(),
                new JsonArchiveRepository()
        );
    }

    private ApplicationContext prepareContextWithSportCategory() {
        ApplicationContext context = newContext();
        ConfigurationService configurationService = context.getConfigurationService();

        configurationService.setBaseFields(List.of(
                new Field("Titolo", "", true, FieldType.BASE, DataType.STRING),
                new Field("Numero di partecipanti", "", true, FieldType.BASE, DataType.INTEGER),
                new Field("Termine ultimo di iscrizione", "", true, FieldType.BASE, DataType.DATE),
                new Field("Luogo", "", true, FieldType.BASE, DataType.STRING),
                new Field("Data", "", true, FieldType.BASE, DataType.DATE),
                new Field("Ora", "", true, FieldType.BASE, DataType.TIME),
                new Field("Quota individuale", "", true, FieldType.BASE, DataType.DECIMAL),
                new Field("Data conclusiva", "", true, FieldType.BASE, DataType.DATE)
        ));
        configurationService.addCategory("Sport", List.of(
                new Field("Certificato medico", "", true, FieldType.SPECIFIC, DataType.BOOLEAN)
        ));
        return context;
    }

    private Map<String, String> validRawValuesWithParticipants(int participants) {
        return Map.of(
                "Titolo", "Camminata",
                "Numero di partecipanti", String.valueOf(participants),
                "Termine ultimo di iscrizione", formatDate(LocalDate.now().plusDays(2)),
                "Luogo", "Brescia",
                "Data", formatDate(LocalDate.now().plusDays(5)),
                "Ora", "15:00",
                "Quota individuale", "12.5",
                "Data conclusiva", formatDate(LocalDate.now().plusDays(5)),
                "Certificato medico", "si"
        );
    }

    private Proposal buildOpenProposal(int id, int participants, LocalDate deadline, LocalDate endDate) {
        Map<String, String> values = Map.of(
                "Titolo", "Evento " + id,
                "Numero di partecipanti", String.valueOf(participants),
                "Termine ultimo di iscrizione", deadline.format(DateTimeFormatter.ISO_LOCAL_DATE),
                "Luogo", "Brescia",
                "Data", endDate.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
                "Ora", "15:00",
                "Quota individuale", "0",
                "Data conclusiva", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        );
        Proposal proposal = new Proposal(id, "Sport", values);
        proposal.markAsValid();
        proposal.markAsOpen();
        return proposal;
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
