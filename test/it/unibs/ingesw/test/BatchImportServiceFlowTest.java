package it.unibs.ingesw.test;

import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;
import it.unibs.ingesw.service.BatchImportReport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Black-box tests for the version-5 batch import use cases.
 */
public class BatchImportServiceFlowTest {
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
    void importFieldsAcceptsCompleteConfigurationAndPersistsIt() throws IOException {
        TestApplicationContext context = BlackBoxTestSupport.newContext();
        Path batchFile = writeFile(
                "fields-batch.json",
                """
                {
                  "baseFields": [
                    { "name": "Titolo", "description": "nome di fantasia", "mandatory": true, "type": "BASE", "dataType": "STRING" },
                    { "name": "Numero di partecipanti", "description": "numero partecipanti", "mandatory": true, "type": "BASE", "dataType": "INTEGER" },
                    { "name": "Termine ultimo di iscrizione", "description": "deadline", "mandatory": true, "type": "BASE", "dataType": "DATE" },
                    { "name": "Luogo", "description": "luogo evento", "mandatory": true, "type": "BASE", "dataType": "STRING" },
                    { "name": "Data", "description": "data inizio", "mandatory": true, "type": "BASE", "dataType": "DATE" },
                    { "name": "Ora", "description": "ora ritrovo", "mandatory": true, "type": "BASE", "dataType": "TIME" },
                    { "name": "Quota individuale", "description": "quota", "mandatory": true, "type": "BASE", "dataType": "DECIMAL" },
                    { "name": "Data conclusiva", "description": "data fine", "mandatory": true, "type": "BASE", "dataType": "DATE" }
                  ],
                  "commonFields": [
                    { "name": "Note", "description": "note aggiuntive", "mandatory": false, "type": "COMMON", "dataType": "STRING" }
                  ]
                }
                """
        );

        BatchImportReport report = context.getBatchImportService().importFields(batchFile.toString());

        assertEquals(9, report.getTotalEntries());
        assertEquals(9, report.getImportedEntries());
        assertEquals(0, report.getDiscardedEntries());

        TestApplicationContext reloaded = BlackBoxTestSupport.newContext();
        assertTrue(reloaded.getConfigurationService().areBaseFieldsSet());
        assertEquals(8, reloaded.getConfigurationService().getBaseFields().size());
        assertEquals("Note", reloaded.getConfigurationService().getCommonFields().getFirst().getName());
    }

    @Test
    void importCategoriesKeepsValidEntriesAndDiscardsInvalidEntriesIndependently() throws IOException {
        TestApplicationContext context = BlackBoxTestSupport.newContext();
        context.getConfigurationService().setBaseFields(BlackBoxTestSupport.baseFields());
        Path batchFile = writeFile(
                "categories-batch.json",
                """
                [
                  {
                    "name": "Sport",
                    "specificFields": [
                      { "name": "Certificato medico", "description": "requisito medico", "mandatory": true, "type": "SPECIFIC", "dataType": "BOOLEAN" }
                    ]
                  },
                  {
                    "name": "Musica",
                    "specificFields": [
                      { "name": "Titolo", "description": "campo in conflitto", "mandatory": true, "type": "SPECIFIC", "dataType": "STRING" }
                    ]
                  }
                ]
                """
        );

        BatchImportReport report = context.getBatchImportService().importCategories(batchFile.toString());

        assertEquals(2, report.getTotalEntries());
        assertEquals(1, report.getImportedEntries());
        assertEquals(1, report.getDiscardedEntries());
        assertTrue(report.getIssues().stream().anyMatch(issue -> issue.contains("Titolo")));

        TestApplicationContext reloaded = BlackBoxTestSupport.newContext();
        assertEquals(1, reloaded.getConfigurationService().getCategories().size());
        assertEquals("Sport", reloaded.getConfigurationService().getCategories().getFirst().getName());
    }

    @Test
    void importProposalsKeepsValidCreatedAndDiscardedEntriesSeparate() throws IOException {
        TestApplicationContext context = BlackBoxTestSupport.configuredSportContext();
        Path batchFile = writeFile(
                "proposals-batch.json",
                """
                {
                  "proposals": [
                    {
                      "categoryName": "Sport",
                      "fieldValues": {
                        "Titolo": "Camminata",
                        "Numero di partecipanti": 20,
                        "Termine ultimo di iscrizione": "10/12/2030",
                        "Luogo": "Brescia",
                        "Data": "13/12/2030",
                        "Ora": "15:00",
                        "Quota individuale": 12.5,
                        "Data conclusiva": "13/12/2030",
                        "Note": "Portare scarpe comode",
                        "Certificato medico": true
                      }
                    },
                    {
                      "categoryName": "Sport",
                      "fieldValues": {
                        "Titolo": "Evento da rivalutare",
                        "Numero di partecipanti": 10,
                        "Termine ultimo di iscrizione": "10/12/2030",
                        "Luogo": "Brescia",
                        "Data": "11/12/2030",
                        "Ora": "18:00",
                        "Quota individuale": 0,
                        "Data conclusiva": "11/12/2030",
                        "Certificato medico": false
                      }
                    },
                    {
                      "categoryName": "Cinema",
                      "fieldValues": {
                        "Titolo": "Film club"
                      }
                    },
                    {
                      "categoryName": "Sport",
                      "fieldValues": {
                        "Titolo": "Campo sconosciuto",
                        "Numero di partecipanti": 5,
                        "Termine ultimo di iscrizione": "10/12/2030",
                        "Luogo": "Brescia",
                        "Data": "13/12/2030",
                        "Ora": "20:00",
                        "Quota individuale": 5,
                        "Data conclusiva": "13/12/2030",
                        "Campo fantasma": "ciao",
                        "Certificato medico": false
                      }
                    }
                  ]
                }
                """
        );

        BatchImportReport report = context.getBatchImportService().importProposals(batchFile.toString());

        assertEquals(4, report.getTotalEntries());
        assertEquals(2, report.getImportedEntries());
        assertEquals(2, report.getDiscardedEntries());
        assertTrue(report.getNotes().stream().anyMatch(note -> note.contains("Creata")));
        assertTrue(report.getIssues().stream().anyMatch(issue -> issue.contains("Cinema")));
        assertTrue(report.getIssues().stream().anyMatch(issue -> issue.contains("Campo fantasma")));

        List<Proposal> proposals = BlackBoxTestSupport.newContext().getProposalService().getArchivedProposals().stream()
                .sorted(Comparator.comparingInt(Proposal::getId))
                .toList();
        assertEquals(2, proposals.size());
        assertEquals(ProposalStatus.VALID, proposals.get(0).getCurrentStatus());
        assertEquals(ProposalStatus.CREATED, proposals.get(1).getCurrentStatus());
    }

    @Test
    void malformedBatchFileDoesNotModifyPersistedState() throws IOException {
        TestApplicationContext context = BlackBoxTestSupport.newContext();
        Path malformedFile = writeFile(
                "malformed-fields.json",
                """
                {
                  "baseFields": [
                    { "name": "Titolo", "description": "nome"
                """
        );

        BatchImportReport report = context.getBatchImportService().importFields(malformedFile.toString());

        assertTrue(report.hasFileError());
        assertEquals(0, report.getTotalEntries());
        assertEquals(0, report.getImportedEntries());
        assertFalse(BlackBoxTestSupport.newContext().getConfigurationService().areBaseFieldsSet());
    }

    private Path writeFile(String fileName, String contents) throws IOException {
        Path path = tempDir.resolve(fileName);
        Files.writeString(path, contents);
        return path;
    }
}
