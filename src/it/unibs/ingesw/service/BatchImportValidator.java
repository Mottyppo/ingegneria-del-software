package it.unibs.ingesw.service;

import it.unibs.ingesw.model.Category;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.FieldType;
import it.unibs.ingesw.persistence.JsonBatchImportReader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validates and normalizes data loaded from batch-import files.
 *
 * <p>This collaborator keeps structural checks outside {@link BatchImportService},
 * whose responsibility remains orchestrating file import and persistence.</p>
 */
public class BatchImportValidator {
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

    /**
     * Creates a validator over the current application configuration.
     *
     * @param configurationService The service used to inspect configured fields and categories.
     */
    public BatchImportValidator(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * Validates a whole base-field batch section.
     *
     * @param rawBaseFields The fields loaded from the import file.
     * @return Normalized fields, or an error message.
     */
    public ValidatedFields validateBaseFields(List<Field> rawBaseFields) {
        List<Field> normalizedFields = new ArrayList<>();
        Set<String> importedNames = new LinkedHashSet<>();
        for (Field rawField : copyList(rawBaseFields)) {
            ValidatedField fieldValidation = validateField(rawField, FieldType.BASE, true);
            if (!fieldValidation.isValid()) {
                return ValidatedFields.failure(fieldValidation.errorMessage());
            }

            Field normalizedField = fieldValidation.field();
            String canonicalName = canonicalize(normalizedField.getName());
            if (!importedNames.add(canonicalName)) {
                return ValidatedFields.failure(FIELD_NAME_DUPLICATE_TEMPLATE.formatted(normalizedField.getName()));
            }
            if (!configurationService.isFieldNameAvailableGlobally(normalizedField.getName())) {
                return ValidatedFields.failure(FIELD_NAME_IN_USE_TEMPLATE.formatted(normalizedField.getName()));
            }
            normalizedFields.add(normalizedField);
        }
        return ValidatedFields.success(normalizedFields);
    }

    /**
     * Validates a common field loaded from a batch section.
     *
     * @param rawField The field loaded from the import file.
     * @return A normalized field, or an error message.
     */
    public ValidatedField validateCommonField(Field rawField) {
        ValidatedField validation = validateField(rawField, FieldType.COMMON, false);
        if (!validation.isValid()) {
            return validation;
        }

        Field normalizedField = validation.field();
        if (!configurationService.isFieldNameAvailableGlobally(normalizedField.getName())) {
            return ValidatedField.failure(FIELD_NAME_IN_USE_TEMPLATE.formatted(normalizedField.getName()));
        }
        return validation;
    }

    /**
     * Validates a category and its specific fields.
     *
     * @param rawCategory The category loaded from the import file.
     * @return Normalized category data, or an error message.
     */
    public ValidatedCategory validateCategory(Category rawCategory) {
        if (rawCategory == null) {
            return ValidatedCategory.failure(CATEGORY_NULL_MESSAGE);
        }

        String categoryName = normalizeText(rawCategory.getName());
        if (categoryName == null) {
            return ValidatedCategory.failure(CATEGORY_NAME_REQUIRED_MESSAGE);
        }
        if (!configurationService.isCategoryNameAvailable(categoryName)) {
            return ValidatedCategory.failure(CATEGORY_NAME_IN_USE_TEMPLATE.formatted(categoryName));
        }

        List<Field> normalizedSpecificFields = new ArrayList<>();
        Set<String> importedNames = new LinkedHashSet<>();
        for (Field rawField : copyList(rawCategory.getSpecificFields())) {
            ValidatedField fieldValidation = validateField(rawField, FieldType.SPECIFIC, false);
            if (!fieldValidation.isValid()) {
                return ValidatedCategory.failure(fieldValidation.errorMessage());
            }

            Field normalizedField = fieldValidation.field();
            String canonicalName = canonicalize(normalizedField.getName());
            if (!importedNames.add(canonicalName)) {
                return ValidatedCategory.failure(FIELD_NAME_DUPLICATE_TEMPLATE.formatted(normalizedField.getName()));
            }
            if (!configurationService.isFieldNameAvailableForCategory(normalizedField.getName(), null)) {
                return ValidatedCategory.failure(FIELD_NAME_IN_USE_TEMPLATE.formatted(normalizedField.getName()));
            }
            normalizedSpecificFields.add(normalizedField);
        }

        return ValidatedCategory.success(categoryName, normalizedSpecificFields);
    }

    /**
     * Validates a proposal seed and canonicalizes its field names.
     *
     * @param proposalSeed The proposal loaded from the import file.
     * @return A configured category and field-value map, or an error message.
     */
    public ValidatedProposalSeed validateProposalSeed(JsonBatchImportReader.ProposalSeed proposalSeed) {
        if (proposalSeed == null) {
            return ValidatedProposalSeed.failure(PROPOSAL_INVALID_VALUES_MESSAGE);
        }

        String categoryName = normalizeText(proposalSeed.categoryName());
        if (categoryName == null) {
            return ValidatedProposalSeed.failure(PROPOSAL_CATEGORY_REQUIRED_MESSAGE);
        }

        Category category = configurationService.findCategoryByName(categoryName);
        if (category == null) {
            return ValidatedProposalSeed.failure(PROPOSAL_CATEGORY_UNKNOWN_TEMPLATE.formatted(categoryName));
        }

        return canonicalizeProposalValues(category, proposalSeed.fieldValues());
    }

    private ValidatedField validateField(Field rawField, FieldType expectedType, boolean mandatoryRequired) {
        if (rawField == null) {
            return ValidatedField.failure(FIELD_NULL_MESSAGE);
        }

        String name = normalizeText(rawField.getName());
        if (name == null) {
            return ValidatedField.failure(FIELD_NAME_REQUIRED_MESSAGE);
        }

        String description = normalizeText(rawField.getDescription());
        if (description == null) {
            return ValidatedField.failure(FIELD_DESCRIPTION_REQUIRED_MESSAGE);
        }

        if (rawField.getType() == null) {
            return ValidatedField.failure(FIELD_TYPE_REQUIRED_MESSAGE);
        }
        if (rawField.getType() != expectedType) {
            return ValidatedField.failure(FIELD_TYPE_MISMATCH_TEMPLATE.formatted(expectedType));
        }

        if (rawField.getDataType() == null) {
            return ValidatedField.failure(FIELD_DATA_TYPE_REQUIRED_MESSAGE);
        }
        if (mandatoryRequired && !rawField.isMandatory()) {
            return ValidatedField.failure(FIELD_MANDATORY_REQUIRED_MESSAGE);
        }

        boolean mandatory = mandatoryRequired || rawField.isMandatory();
        Field normalizedField = new Field(name, description, mandatory, expectedType, rawField.getDataType());
        return ValidatedField.success(normalizedField);
    }

    private ValidatedProposalSeed canonicalizeProposalValues(Category category, Map<String, String> rawValues) {
        if (rawValues == null) {
            return ValidatedProposalSeed.failure(PROPOSAL_FIELD_VALUES_REQUIRED_MESSAGE);
        }

        Map<String, String> expectedFieldNames = new LinkedHashMap<>();
        for (Field field : configurationService.getSharedFieldsForCategory(category)) {
            expectedFieldNames.put(canonicalize(field.getName()), field.getName());
        }

        Map<String, String> canonicalFieldValues = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : rawValues.entrySet()) {
            String rawFieldName = normalizeText(entry.getKey());
            if (rawFieldName == null) {
                return ValidatedProposalSeed.failure(PROPOSAL_FIELD_NAME_REQUIRED_MESSAGE);
            }

            String expectedFieldName = expectedFieldNames.get(canonicalize(rawFieldName));
            if (expectedFieldName == null) {
                return ValidatedProposalSeed.failure(PROPOSAL_UNKNOWN_FIELD_TEMPLATE.formatted(rawFieldName));
            }
            if (canonicalFieldValues.containsKey(expectedFieldName)) {
                return ValidatedProposalSeed.failure(PROPOSAL_DUPLICATE_FIELD_TEMPLATE.formatted(rawFieldName));
            }

            if (entry.getValue() == null) {
                return ValidatedProposalSeed.failure(PROPOSAL_FIELD_VALUE_REQUIRED_TEMPLATE.formatted(rawFieldName));
            }
            canonicalFieldValues.put(expectedFieldName, entry.getValue());
        }

        return ValidatedProposalSeed.success(category, canonicalFieldValues);
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

    /**
     * Validation outcome for a single field.
     *
     * @param field        The normalized field.
     * @param errorMessage The validation error, or {@code null}.
     */
    public record ValidatedField(Field field, String errorMessage) {
        public boolean isValid() {
            return errorMessage == null;
        }

        private static ValidatedField success(Field field) {
            return new ValidatedField(field, null);
        }

        private static ValidatedField failure(String errorMessage) {
            return new ValidatedField(null, errorMessage);
        }
    }

    /**
     * Validation outcome for a field list.
     *
     * @param fields       The normalized fields.
     * @param errorMessage The validation error, or {@code null}.
     */
    public record ValidatedFields(List<Field> fields, String errorMessage) {
        public boolean isValid() {
            return errorMessage == null;
        }

        private static ValidatedFields success(List<Field> fields) {
            return new ValidatedFields(fields, null);
        }

        private static ValidatedFields failure(String errorMessage) {
            return new ValidatedFields(List.of(), errorMessage);
        }
    }

    /**
     * Validation outcome for a category import entry.
     *
     * @param name           The normalized category name.
     * @param specificFields The normalized specific fields.
     * @param errorMessage   The validation error, or {@code null}.
     */
    public record ValidatedCategory(String name, List<Field> specificFields, String errorMessage) {
        public boolean isValid() {
            return errorMessage == null;
        }

        private static ValidatedCategory success(String name, List<Field> specificFields) {
            return new ValidatedCategory(name, specificFields, null);
        }

        private static ValidatedCategory failure(String errorMessage) {
            return new ValidatedCategory(null, List.of(), errorMessage);
        }
    }

    /**
     * Validation outcome for a proposal import entry.
     *
     * @param category     The matched configured category.
     * @param fieldValues  The field values keyed by configured field names.
     * @param errorMessage The validation error, or {@code null}.
     */
    public record ValidatedProposalSeed(Category category, Map<String, String> fieldValues, String errorMessage) {
        public boolean isValid() {
            return errorMessage == null;
        }

        private static ValidatedProposalSeed success(Category category, Map<String, String> fieldValues) {
            return new ValidatedProposalSeed(category, fieldValues, null);
        }

        private static ValidatedProposalSeed failure(String errorMessage) {
            return new ValidatedProposalSeed(null, Map.of(), errorMessage);
        }
    }
}
