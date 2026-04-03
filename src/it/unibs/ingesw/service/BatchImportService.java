package it.unibs.ingesw.service;

import it.unibs.ingesw.model.Category;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.FieldType;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;
import it.unibs.ingesw.persistence.JsonBatchImportReader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

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

    private static final String FIELD_NULL_MESSAGE = "campo assente.";
    private static final String FIELD_NAME_REQUIRED_MESSAGE = "nome campo mancante.";
    private static final String FIELD_DESCRIPTION_REQUIRED_MESSAGE = "descrizione campo mancante.";
    private static final String FIELD_TYPE_REQUIRED_MESSAGE = "tipo campo mancante.";
    private static final String FIELD_DATA_TYPE_REQUIRED_MESSAGE = "tipo di dato mancante.";
    private static final String FIELD_TYPE_MISMATCH_TEMPLATE = "tipo campo atteso %s.";
    private static final String FIELD_MANDATORY_REQUIRED_MESSAGE = "i campi base devono essere obbligatori.";
    private static final String FIELD_NAME_IN_USE_TEMPLATE = "nome campo \"%s\" gia' in uso.";
    private static final String FIELD_NAME_DUPLICATE_TEMPLATE = "nome campo duplicato \"%s\".";
    private static final String CATEGORY_NULL_MESSAGE = "categoria assente.";
    private static final String CATEGORY_NAME_REQUIRED_MESSAGE = "nome categoria mancante.";
    private static final String CATEGORY_NAME_IN_USE_TEMPLATE = "nome categoria \"%s\" gia' in uso.";
    private static final String PROPOSAL_CATEGORY_REQUIRED_MESSAGE = "nome categoria mancante.";
    private static final String PROPOSAL_CATEGORY_UNKNOWN_TEMPLATE = "categoria \"%s\" inesistente.";
    private static final String PROPOSAL_FIELD_VALUES_REQUIRED_MESSAGE = "mappa dei campi assente.";
    private static final String PROPOSAL_FIELD_NAME_REQUIRED_MESSAGE = "nome campo proposta mancante.";
    private static final String PROPOSAL_FIELD_VALUE_REQUIRED_TEMPLATE = "valore nullo per il campo \"%s\".";
    private static final String PROPOSAL_UNKNOWN_FIELD_TEMPLATE = "campo sconosciuto \"%s\".";
    private static final String PROPOSAL_DUPLICATE_FIELD_TEMPLATE = "campo duplicato \"%s\".";
    private static final String PROPOSAL_INVALID_VALUES_MESSAGE =
            "campi mancanti oppure valori in formato non valido.";

    private final ConfigurationService configurationService;
    private final ProposalService proposalService;
    private final JsonBatchImportReader reader;

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

        List<Field> normalizedBaseFields = new ArrayList<>();
        Set<String> importedNames = new LinkedHashSet<>();
        for (Field rawField : rawBaseFields) {
            FieldValidation fieldValidation = normalizeField(rawField, FieldType.BASE, true);
            if (!fieldValidation.isValid()) {
                report.addIssue(BASE_FIELDS_INVALID_TEMPLATE.formatted(fieldValidation.errorMessage()));
                return;
            }

            Field normalizedField = fieldValidation.field();
            String canonicalName = canonicalize(normalizedField.getName());
            if (!importedNames.add(canonicalName)) {
                report.addIssue(BASE_FIELDS_INVALID_TEMPLATE.formatted(
                        FIELD_NAME_DUPLICATE_TEMPLATE.formatted(normalizedField.getName())
                ));
                return;
            }
            if (!configurationService.isFieldNameAvailableGlobally(normalizedField.getName())) {
                report.addIssue(BASE_FIELDS_INVALID_TEMPLATE.formatted(
                        FIELD_NAME_IN_USE_TEMPLATE.formatted(normalizedField.getName())
                ));
                return;
            }
            normalizedBaseFields.add(normalizedField);
        }

        boolean stored = configurationService.setBaseFields(normalizedBaseFields);
        if (stored) {
            report.addImportedEntries(normalizedBaseFields.size());
            report.addNote(BASE_FIELDS_IMPORTED_MESSAGE);
            return;
        }
        report.addIssue(BASE_FIELDS_ALREADY_SET_MESSAGE);
    }

    private void importCommonField(Field rawField, int index, BatchImportReport report) {
        FieldValidation validation = normalizeField(rawField, FieldType.COMMON, false);
        if (!validation.isValid()) {
            report.addIssue(COMMON_FIELD_DISCARDED_TEMPLATE.formatted(index + 1, validation.errorMessage()));
            return;
        }

        Field normalizedField = validation.field();
        if (!configurationService.isFieldNameAvailableGlobally(normalizedField.getName())) {
            report.addIssue(COMMON_FIELD_DISCARDED_TEMPLATE.formatted(
                    index + 1,
                    FIELD_NAME_IN_USE_TEMPLATE.formatted(normalizedField.getName())
            ));
            return;
        }

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
        if (rawCategory == null) {
            report.addIssue(CATEGORY_DISCARDED_TEMPLATE.formatted(index + 1, CATEGORY_NULL_MESSAGE));
            return;
        }

        String categoryName = normalizeText(rawCategory.getName());
        if (categoryName == null) {
            report.addIssue(CATEGORY_DISCARDED_TEMPLATE.formatted(index + 1, CATEGORY_NAME_REQUIRED_MESSAGE));
            return;
        }
        if (!configurationService.isCategoryNameAvailable(categoryName)) {
            report.addIssue(CATEGORY_DISCARDED_TEMPLATE.formatted(
                    index + 1,
                    CATEGORY_NAME_IN_USE_TEMPLATE.formatted(categoryName)
            ));
            return;
        }

        List<Field> normalizedSpecificFields = new ArrayList<>();
        Set<String> importedNames = new LinkedHashSet<>();
        List<Field> specificFields = copyList(rawCategory.getSpecificFields());
        for (Field rawField : specificFields) {
            FieldValidation fieldValidation = normalizeField(rawField, FieldType.SPECIFIC, false);
            if (!fieldValidation.isValid()) {
                report.addIssue(CATEGORY_DISCARDED_TEMPLATE.formatted(index + 1, fieldValidation.errorMessage()));
                return;
            }

            Field normalizedField = fieldValidation.field();
            String canonicalName = canonicalize(normalizedField.getName());
            if (!importedNames.add(canonicalName)) {
                report.addIssue(CATEGORY_DISCARDED_TEMPLATE.formatted(
                        index + 1,
                        FIELD_NAME_DUPLICATE_TEMPLATE.formatted(normalizedField.getName())
                ));
                return;
            }
            if (!configurationService.isFieldNameAvailableForCategory(normalizedField.getName(), null)) {
                report.addIssue(CATEGORY_DISCARDED_TEMPLATE.formatted(
                        index + 1,
                        FIELD_NAME_IN_USE_TEMPLATE.formatted(normalizedField.getName())
                ));
                return;
            }
            normalizedSpecificFields.add(normalizedField);
        }

        boolean added = configurationService.addCategory(categoryName, normalizedSpecificFields);
        if (added) {
            report.addImportedEntry();
            return;
        }
        report.addIssue(CATEGORY_DISCARDED_TEMPLATE.formatted(
                index + 1,
                CATEGORY_NAME_IN_USE_TEMPLATE.formatted(categoryName)
        ));
    }

    private void importProposal(JsonBatchImportReader.ProposalSeed proposalSeed, int index, BatchImportReport report) {
        if (proposalSeed == null) {
            report.addIssue(PROPOSAL_DISCARDED_TEMPLATE.formatted(index + 1, PROPOSAL_INVALID_VALUES_MESSAGE));
            return;
        }

        String categoryName = normalizeText(proposalSeed.categoryName());
        if (categoryName == null) {
            report.addIssue(PROPOSAL_DISCARDED_TEMPLATE.formatted(index + 1, PROPOSAL_CATEGORY_REQUIRED_MESSAGE));
            return;
        }

        Category category = configurationService.findCategoryByName(categoryName);
        if (category == null) {
            report.addIssue(PROPOSAL_DISCARDED_TEMPLATE.formatted(
                    index + 1,
                    PROPOSAL_CATEGORY_UNKNOWN_TEMPLATE.formatted(categoryName)
            ));
            return;
        }

        ProposalFieldValuesValidation valuesValidation = canonicalizeProposalValues(category, proposalSeed.fieldValues());
        if (!valuesValidation.isValid()) {
            report.addIssue(PROPOSAL_DISCARDED_TEMPLATE.formatted(index + 1, valuesValidation.errorMessage()));
            return;
        }

        Proposal proposal = proposalService.createProposal(category.getName(), valuesValidation.fieldValues());
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

    private FieldValidation normalizeField(Field rawField, FieldType expectedType, boolean mandatoryRequired) {
        if (rawField == null) {
            return FieldValidation.failure(FIELD_NULL_MESSAGE);
        }

        String name = normalizeText(rawField.getName());
        if (name == null) {
            return FieldValidation.failure(FIELD_NAME_REQUIRED_MESSAGE);
        }

        String description = normalizeText(rawField.getDescription());
        if (description == null) {
            return FieldValidation.failure(FIELD_DESCRIPTION_REQUIRED_MESSAGE);
        }

        if (rawField.getType() == null) {
            return FieldValidation.failure(FIELD_TYPE_REQUIRED_MESSAGE);
        }
        if (rawField.getType() != expectedType) {
            return FieldValidation.failure(FIELD_TYPE_MISMATCH_TEMPLATE.formatted(expectedType));
        }

        if (rawField.getDataType() == null) {
            return FieldValidation.failure(FIELD_DATA_TYPE_REQUIRED_MESSAGE);
        }
        if (mandatoryRequired && !rawField.isMandatory()) {
            return FieldValidation.failure(FIELD_MANDATORY_REQUIRED_MESSAGE);
        }

        boolean mandatory = mandatoryRequired || rawField.isMandatory();
        Field normalizedField = new Field(name, description, mandatory, expectedType, rawField.getDataType());
        return FieldValidation.success(normalizedField);
    }

    private ProposalFieldValuesValidation canonicalizeProposalValues(Category category, Map<String, String> rawValues) {
        if (rawValues == null) {
            return ProposalFieldValuesValidation.failure(PROPOSAL_FIELD_VALUES_REQUIRED_MESSAGE);
        }

        Map<String, String> expectedFieldNames = new LinkedHashMap<>();
        for (Field field : configurationService.getSharedFieldsForCategory(category)) {
            expectedFieldNames.put(canonicalize(field.getName()), field.getName());
        }

        Map<String, String> canonicalFieldValues = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : rawValues.entrySet()) {
            String rawFieldName = normalizeText(entry.getKey());
            if (rawFieldName == null) {
                return ProposalFieldValuesValidation.failure(PROPOSAL_FIELD_NAME_REQUIRED_MESSAGE);
            }

            String expectedFieldName = expectedFieldNames.get(canonicalize(rawFieldName));
            if (expectedFieldName == null) {
                return ProposalFieldValuesValidation.failure(PROPOSAL_UNKNOWN_FIELD_TEMPLATE.formatted(rawFieldName));
            }
            if (canonicalFieldValues.containsKey(expectedFieldName)) {
                return ProposalFieldValuesValidation.failure(PROPOSAL_DUPLICATE_FIELD_TEMPLATE.formatted(rawFieldName));
            }

            if (entry.getValue() == null) {
                return ProposalFieldValuesValidation.failure(PROPOSAL_FIELD_VALUE_REQUIRED_TEMPLATE.formatted(rawFieldName));
            }
            canonicalFieldValues.put(expectedFieldName, entry.getValue());
        }

        return ProposalFieldValuesValidation.success(canonicalFieldValues);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private String canonicalize(String value) {
        return value.trim().toLowerCase();
    }

    private <T> List<T> copyList(List<T> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }

    private record FieldValidation(Field field, String errorMessage) {
        private boolean isValid() {
            return errorMessage == null;
        }

        private static FieldValidation success(Field field) {
            return new FieldValidation(field, null);
        }

        private static FieldValidation failure(String errorMessage) {
            return new FieldValidation(null, errorMessage);
        }
    }

    private record ProposalFieldValuesValidation(Map<String, String> fieldValues, String errorMessage) {
        private boolean isValid() {
            return errorMessage == null;
        }

        private static ProposalFieldValuesValidation success(Map<String, String> fieldValues) {
            return new ProposalFieldValuesValidation(fieldValues, null);
        }

        private static ProposalFieldValuesValidation failure(String errorMessage) {
            return new ProposalFieldValuesValidation(null, errorMessage);
        }
    }
}
