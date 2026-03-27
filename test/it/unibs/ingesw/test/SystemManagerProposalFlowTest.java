package it.unibs.ingesw.test;

import it.unibs.ingesw.controller.SystemManager;
import it.unibs.ingesw.io.IOManager;
import it.unibs.ingesw.model.Archive;
import it.unibs.ingesw.model.DataType;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.FieldType;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemManagerProposalFlowTest {

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
    void createProposalSavesCreatedAndThenValid() {
        SystemManager manager = prepareManagerWithSportCategory();

        Proposal proposal = manager.createProposal(0, validRawValues());
        assertNotNull(proposal);
        assertEquals(ProposalStatus.VALID, proposal.getCurrentStatus());

        Archive archive = new IOManager().readArchive();
        assertEquals(1, archive.getProposals().size());
        assertEquals(ProposalStatus.VALID, archive.getProposals().getFirst().getCurrentStatus());
    }

    @Test
    void publishSelectivelyOnlyChosenValidProposal() {
        SystemManager manager = prepareManagerWithSportCategory();

        Proposal createdOnly = manager.createProposal(0, invalidRawValues());
        Proposal valid = manager.createProposal(0, validRawValues());

        assertNotNull(createdOnly);
        assertNotNull(valid);
        assertEquals(ProposalStatus.CREATED, createdOnly.getCurrentStatus());
        assertEquals(ProposalStatus.VALID, valid.getCurrentStatus());

        List<Proposal> validProposals = manager.getValidProposals();
        assertEquals(1, validProposals.size());
        assertEquals(valid.getId(), validProposals.getFirst().getId());

        assertTrue(manager.publishProposal(validProposals.getFirst()));
        assertEquals(0, manager.getValidProposals().size());

        Archive archive = new IOManager().readArchive();
        assertEquals(2, archive.getProposals().size());
        assertEquals(1, archive.getByStatus(ProposalStatus.CREATED).size());
        assertEquals(1, archive.getByStatus(ProposalStatus.OPEN).size());
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

    private Map<String, String> validRawValues() {
        return Map.of(
                "Titolo", "Camminata",
                "Numero di partecipanti", "20",
                "Termine ultimo di iscrizione", "10/12/2030",
                "Luogo", "Brescia",
                "Data", "13/12/2030",
                "Ora", "15:00",
                "Quota individuale", "12.5",
                "Data conclusiva", "13/12/2030",
                "Certificato medico", "si"
        );
    }

    private Map<String, String> invalidRawValues() {
        return Map.of(
                "Titolo", "Evento non valido",
                "Numero di partecipanti", "10",
                "Termine ultimo di iscrizione", "10/12/2030",
                "Luogo", "Brescia",
                "Data", "11/12/2030",
                "Ora", "18:00",
                "Quota individuale", "5",
                "Data conclusiva", "11/12/2030",
                "Certificato medico", "no"
        );
    }
}
