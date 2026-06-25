package it.unibs.ingesw.service;

import it.unibs.ingesw.model.Category;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;
import it.unibs.ingesw.persistence.JsonBatchImportReader;
import it.unibs.ingesw.service.proposal.ProposalService;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes batch imports for configurator-side back-end data.
 *
 * <p>The service validates file contents and then applies them through the
 * existing application services so that persistence, proposal normalization,
 * and business rules remain centralized in one place.</p>
 *
 * <p>The accepted input formats should be the same as the ones in
 * interactive mode, since this input methodology is merely meant to
 * streamline entries' addition.</p>
 */
public class BatchImportService {
    private static final String FIELDS_IMPORT_NAME = "Campi";
    private static final String CATEGORIES_IMPORT_NAME = "Categorie";
    private static final String PROPOSALS_IMPORT_NAME = "Proposte";

    private static final String NO_ELEMENTS_MESSAGE = "Nessun elemento da importare.";
    private static final String BASE_FIELDS_IMPORTED_MESSAGE = "Sezione campi base importata correttamente.";
    private static final String BASE_FIELDS_ALREADY_SET_MESSAGE = "Sezione campi base scartata: campi base gia' configurati.";
    private static final String BASE_FIELDS_INVALID_TEMPLATE = "Sezione campi base scartata: %s";
    private static final String COMMON_FIELD_DISCARDED_TEMPLATE = "Campo comune #%d scartato: %s";
    private static final String CATEGORY_DISCARDED_TEMPLATE = "Categoria #%d scartata: %s";
    private static final String PROPOSAL_DISCARDED_TEMPLATE = "Proposta #%d scartata: %s";
    private static final String CREATED_PROPOSAL_NOTE_TEMPLATE =
            "Proposta #%d importata come %s: vincoli di dominio non soddisfatti.";

    private static final String FIELD_NAME_IN_USE_TEMPLATE = "nome campo \"%s\" gia' in uso.";
    private static final String CATEGORY_NAME_IN_USE_TEMPLATE = "nome categoria \"%s\" gia' in uso.";
    private static final String PROPOSAL_INVALID_VALUES_MESSAGE =
            "campi mancanti oppure valori in formato non valido.";

    private final ConfigurationService configurationService;
    private final ProposalService proposalService;
    private final JsonBatchImportReader reader;
    private final BatchImportValidator validator;

    /**
     * Creates the batch-import service.
     *
     * @param configurationService The configuration service used to apply field and category changes.
     * @param proposalService      The proposal service used to create imported proposals.
     * @param reader               The JSON reader used to load batch files.
     */
    public BatchImportService(
            ConfigurationService configurationService,
            ProposalService proposalService,
            JsonBatchImportReader reader
    ) {
        this.configurationService = configurationService;
        this.proposalService = proposalService;
        this.reader = reader;
        this.validator = new BatchImportValidator(configurationService);
    }

    /**
     * Imports base/common fields from a JSON file.
     *
     * @param path The source file path.
     * @return The import outcome report.
     */
    public BatchImportReport importFields(String path) {
        JsonBatchImportReader.ReadResult<JsonBatchImportReader.FieldsFile> readResult = reader.readFieldsFile(path);
        BatchImportReport report = new BatchImportReport(FIELDS_IMPORT_NAME, readResult.sourcePath());
        if (!readResult.isSuccess()) {
            report.markFileError(readResult.errorMessage());
            return report;
        }

        List<Field> baseFields = copyList(readResult.value() == null ? null : readResult.value().baseFields());
        List<Field> commonFields = copyList(readResult.value() == null ? null : readResult.value().commonFields());
        report.setTotalEntries(baseFields.size() + commonFields.size());

        if (report.getTotalEntries() == 0) {
            report.addNote(NO_ELEMENTS_MESSAGE);
            return report;
        }

        if (!baseFields.isEmpty()) {
            importBaseFields(baseFields, report);
        }
        for (int i = 0; i < commonFields.size(); i++) {
            importCommonField(commonFields.get(i), i, report);
        }
        return report;
    }

    /**
     * Imports categories from a JSON file.
     *
     * @param path The source file path.
     * @return The import outcome report.
     */
    public BatchImportReport importCategories(String path) {
        JsonBatchImportReader.ReadResult<List<Category>> readResult = reader.readCategoriesFile(path);
        BatchImportReport report = new BatchImportReport(CATEGORIES_IMPORT_NAME, readResult.sourcePath());
        if (!readResult.isSuccess()) {
            report.markFileError(readResult.errorMessage());
            return report;
        }

        List<Category> categories = copyList(readResult.value());
        report.setTotalEntries(categories.size());
        if (categories.isEmpty()) {
            report.addNote(NO_ELEMENTS_MESSAGE);
            return report;
        }

        for (int i = 0; i < categories.size(); i++) {
            importCategory(categories.get(i), i, report);
        }
        return report;
    }

    /**
     * Imports new proposals from a JSON file.
     *
     * @param path The source file path.
     * @return The import outcome report.
     */
    public BatchImportReport importProposals(String path) {
        JsonBatchImportReader.ReadResult<List<JsonBatchImportReader.ProposalSeed>> readResult =
                reader.readProposalsFile(path);
        BatchImportReport report = new BatchImportReport(PROPOSALS_IMPORT_NAME, readResult.sourcePath());
        if (!readResult.isSuccess()) {
            report.markFileError(readResult.errorMessage());
            return report;
        }

        List<JsonBatchImportReader.ProposalSeed> proposals = copyList(readResult.value());
        report.setTotalEntries(proposals.size());
        if (proposals.isEmpty()) {
            report.addNote(NO_ELEMENTS_MESSAGE);
            return report;
        }

        for (int i = 0; i < proposals.size(); i++) {
            importProposal(proposals.get(i), i, report);
        }
        return report;
    }

    private void importBaseFields(List<Field> rawBaseFields, BatchImportReport report) {
        if (configurationService.areBaseFieldsSet()) {
            report.addIssue(BASE_FIELDS_ALREADY_SET_MESSAGE);
            return;
        }

        BatchImportValidator.ValidatedFields validation = validator.validateBaseFields(rawBaseFields);
        if (!validation.isValid()) {
            report.addIssue(BASE_FIELDS_INVALID_TEMPLATE.formatted(validation.errorMessage()));
            return;
        }

        List<Field> normalizedBaseFields = validation.fields();
        boolean stored = configurationService.setBaseFields(normalizedBaseFields);
        if (stored) {
            report.addImportedEntries(normalizedBaseFields.size());
            report.addNote(BASE_FIELDS_IMPORTED_MESSAGE);
            return;
        }
        report.addIssue(BASE_FIELDS_ALREADY_SET_MESSAGE);
    }

    private void importCommonField(Field rawField, int index, BatchImportReport report) {
        BatchImportValidator.ValidatedField validation = validator.validateCommonField(rawField);
        if (!validation.isValid()) {
            report.addIssue(COMMON_FIELD_DISCARDED_TEMPLATE.formatted(index + 1, validation.errorMessage()));
            return;
        }

        Field normalizedField = validation.field();
        boolean added = configurationService.addCommonField(normalizedField);
        if (added) {
            report.addImportedEntry();
            return;
        }
        report.addIssue(COMMON_FIELD_DISCARDED_TEMPLATE.formatted(
                index + 1,
                FIELD_NAME_IN_USE_TEMPLATE.formatted(normalizedField.getName())
        ));
    }

    private void importCategory(Category rawCategory, int index, BatchImportReport report) {
        BatchImportValidator.ValidatedCategory validation = validator.validateCategory(rawCategory);
        if (!validation.isValid()) {
            report.addIssue(CATEGORY_DISCARDED_TEMPLATE.formatted(index + 1, validation.errorMessage()));
            return;
        }

        boolean added = configurationService.addCategory(validation.name(), validation.specificFields());
        if (added) {
            report.addImportedEntry();
            return;
        }
        report.addIssue(CATEGORY_DISCARDED_TEMPLATE.formatted(
                index + 1,
                CATEGORY_NAME_IN_USE_TEMPLATE.formatted(validation.name())
        ));
    }

    private void importProposal(JsonBatchImportReader.ProposalSeed proposalSeed, int index, BatchImportReport report) {
        BatchImportValidator.ValidatedProposalSeed validation = validator.validateProposalSeed(proposalSeed);
        if (!validation.isValid()) {
            report.addIssue(PROPOSAL_DISCARDED_TEMPLATE.formatted(index + 1, validation.errorMessage()));
            return;
        }

        Proposal proposal = proposalService.createProposal(validation.category().getName(), validation.fieldValues());
        if (proposal == null) {
            report.addIssue(PROPOSAL_DISCARDED_TEMPLATE.formatted(index + 1, PROPOSAL_INVALID_VALUES_MESSAGE));
            return;
        }

        report.addImportedEntry();
        if (proposal.getCurrentStatus() == ProposalStatus.CREATED) {
            report.addNote(CREATED_PROPOSAL_NOTE_TEMPLATE.formatted(
                    proposal.getId(),
                    proposal.getCurrentStatus()
            ));
        }
    }

    private <T> List<T> copyList(List<T> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }
}
