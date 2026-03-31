package it.unibs.ingesw.test;

import it.unibs.ingesw.controller.SystemManager;
import it.unibs.ingesw.io.IOManager;
import it.unibs.ingesw.model.Archive;
import it.unibs.ingesw.model.Configurator;
import it.unibs.ingesw.model.DataType;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.FieldType;
import it.unibs.ingesw.model.Participant;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;
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

public class SystemManagerVersion3FlowTest {

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
        SystemManager manager = new SystemManager();

        assertNull(manager.signUpParticipant("Mario", "Rossi", "crocerossaitaliana", "pwd"));

        Participant created = manager.signUpParticipant("Mario", "Rossi", "mrossi", "pwd");
        assertNotNull(created);
        assertNull(manager.signUpParticipant("Marco", "Rossi", "MROSSI", "pwd2"));

        Configurator configurator = manager.authenticateConfigurator("crocerossaitaliana", "ginevra1864");
        assertNotNull(configurator);
        assertFalse(manager.updateCredentials(configurator, "mrossi", "newpass"));
    }

    @Test
    void subscribeRespectsUniquenessAndCapacity() {
        SystemManager manager = prepareManagerWithSportCategory();
        Participant first = manager.signUpParticipant("Mario", "Rossi", "mrossi", "pwd");
        Participant second = manager.signUpParticipant("Luca", "Bianchi", "lbianchi", "pwd");
        assertNotNull(first);
        assertNotNull(second);

        Proposal proposal = manager.createProposal(0, validRawValuesWithParticipants(1));
        assertNotNull(proposal);
        assertTrue(manager.publishProposal(proposal));

        int openProposalId = manager.getOpenProposals().getFirst().getId();
        assertTrue(manager.subscribeParticipantToProposal(first, openProposalId));
        assertFalse(manager.subscribeParticipantToProposal(first, openProposalId));
        assertFalse(manager.subscribeParticipantToProposal(second, openProposalId));
    }

    @Test
    void transitionsFromOpenToConfirmedOrCanceledGenerateNotifications() {
        IOManager ioManager = new IOManager();
        Participant mario = new Participant("Mario", "Rossi", "mrossi", "pwd");
        Participant luca = new Participant("Luca", "Bianchi", "lbianchi", "pwd");
        ioManager.writeParticipants(List.of(mario, luca));

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
        ioManager.writeArchive(archive);

        SystemManager manager = new SystemManager();
        Proposal confirmedAfter = manager.getArchivedProposals().stream()
                .filter(p -> p.getId() == 1)
                .findFirst()
                .orElseThrow();
        Proposal canceledAfter = manager.getArchivedProposals().stream()
                .filter(p -> p.getId() == 2)
                .findFirst()
                .orElseThrow();

        assertEquals(ProposalStatus.CONFIRMED, confirmedAfter.getCurrentStatus());
        assertEquals(ProposalStatus.CANCELED, canceledAfter.getCurrentStatus());

        Participant marioLoaded = manager.authenticateParticipant("mrossi", "pwd");
        Participant lucaLoaded = manager.authenticateParticipant("lbianchi", "pwd");
        assertNotNull(marioLoaded);
        assertNotNull(lucaLoaded);

        assertEquals(2, manager.getParticipantNotifications(marioLoaded).size());
        assertEquals(1, manager.getParticipantNotifications(lucaLoaded).size());
    }

    @Test
    void confirmedProposalBecomesCloseAfterEndDate() {
        IOManager ioManager = new IOManager();
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
        ioManager.writeArchive(archive);

        SystemManager manager = new SystemManager();
        Proposal closed = manager.getArchivedProposals().stream()
                .filter(p -> p.getId() == 10)
                .findFirst()
                .orElseThrow();
        assertEquals(ProposalStatus.CLOSE, closed.getCurrentStatus());
    }

    @Test
    void personalSpacePersistsAfterNotificationRemoval() {
        IOManager ioManager = new IOManager();
        Participant participant = new Participant("Mario", "Rossi", "mrossi", "pwd");
        participant.getPersonalSpace().addNotification("Notifica iniziale");
        ioManager.writeParticipants(List.of(participant));

        SystemManager firstManager = new SystemManager();
        Participant loaded = firstManager.authenticateParticipant("mrossi", "pwd");
        assertNotNull(loaded);
        assertEquals(1, firstManager.getParticipantNotifications(loaded).size());
        assertTrue(firstManager.removeParticipantNotification(loaded, 0));

        SystemManager secondManager = new SystemManager();
        Participant loadedAgain = secondManager.authenticateParticipant("mrossi", "pwd");
        assertNotNull(loadedAgain);
        assertTrue(secondManager.getParticipantNotifications(loadedAgain).isEmpty());
    }

    @Test
    void configuratorCanReadArchiveContent() {
        SystemManager manager = prepareManagerWithSportCategory();
        Proposal proposal = manager.createProposal(0, validRawValuesWithParticipants(3));
        assertNotNull(proposal);
        assertTrue(manager.publishProposal(proposal));
        assertFalse(manager.getArchivedProposals().isEmpty());
    }

    private SystemManager prepareManagerWithSportCategory() {
        SystemManager manager = new SystemManager();
        manager.setBaseFields(List.of(
                new Field("Titolo", "", true, FieldType.BASE, DataType.STRING),
                new Field("Numero di partecipanti", "", true, FieldType.BASE, DataType.INTEGER),
                new Field("Termine ultimo di iscrizione", "", true, FieldType.BASE, DataType.DATE),
                new Field("Luogo", "", true, FieldType.BASE, DataType.STRING),
                new Field("Data", "", true, FieldType.BASE, DataType.DATE),
                new Field("Ora", "", true, FieldType.BASE, DataType.TIME),
                new Field("Quota individuale", "", true, FieldType.BASE, DataType.DECIMAL),
                new Field("Data conclusiva", "", true, FieldType.BASE, DataType.DATE)
        ));
        manager.addCategory("Sport", List.of(
                new Field("Certificato medico", "", true, FieldType.SPECIFIC, DataType.BOOLEAN)
        ));
        return manager;
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
