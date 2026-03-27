package it.unibs.ingesw.controller;

import it.unibs.ingesw.io.IOManager;
import it.unibs.ingesw.model.Archive;
import it.unibs.ingesw.model.Category;
import it.unibs.ingesw.model.Configurator;
import it.unibs.ingesw.model.DataType;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;
import it.unibs.ingesw.model.SystemConfig;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Main controller of the application, coordinates persistence and validation for the application domain.
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Authenticates configurators and updates their credentials.</li>
 *   <li>Manages base, common, and category-specific fields.</li>
 *   <li>Ensures category and field names are unique in a case-insensitive way.</li>
 *   <li>Validates and creates proposals according to business rules.</li>
 *   <li>Publishes valid proposals and exposes the board grouped by category.</li>
 * </ul>
 */
public class SystemManager {
    private static final String DEFAULT_CONFIGURATOR_ONE_USERNAME = "crocerossaitaliana";
    private static final String DEFAULT_CONFIGURATOR_ONE_PASSWORD = "ginevra1864";
    private static final String DEFAULT_CONFIGURATOR_TWO_USERNAME = "alpinibrescia";
    private static final String DEFAULT_CONFIGURATOR_TWO_PASSWORD = "nikolajewka1943";
    private static final String DEADLINE_FIELD_NAME = "Termine ultimo di iscrizione";
    private static final String START_DATE_FIELD_NAME = "Data";
    private static final String END_DATE_FIELD_NAME = "Data conclusiva";
    private static final String PARTICIPANTS_FIELD_NAME = "Numero di partecipanti";
    private static final String FEE_FIELD_NAME = "Quota individuale";
    private static final DateTimeFormatter USER_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter USER_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    private final IOManager ioManager;
    private final List<Configurator> configurators;
    private final List<Category> categories;
    private final SystemConfig config;
    private final Archive archive;

    public SystemManager() {
        this.ioManager = new IOManager();
        this.config = ioManager.readConfig();
        this.categories = ioManager.readCategories();
        this.configurators = ioManager.readConfigurators();
        this.archive = ioManager.readArchive();

        if (this.configurators.isEmpty()) {
            initializeDefaultConfigurators();
        }
    }

    /**
     * Authenticates a configurator using the provided credentials.
     *
     * @param username The username to verify.
     * @param password The password to verify.
     *
     * @return The matching configurator, or {@code null} if the credentials are invalid.
     */
    public Configurator authenticateConfigurator(String username, String password) {
        for (Configurator configurator : configurators) {
            if (configurator.getUsername().equalsIgnoreCase(username)
                    && configurator.getPassword().equals(password)) {
                return configurator;
            }
        }
        return null;
    }

    /**
     * Checks whether the provided username is available.
     *
     * @param username  The username to check.
     * @param exclude   A configurator to ignore during the check, or {@code null}.
     *
     * @return {@code true} if the username is available, {@code false} otherwise.
     */
    public boolean isUsernameAvailable(String username, Configurator exclude) {
        for (Configurator configurator : configurators) {
            if (exclude != null && configurator == exclude) {
                continue;
            }
            if (configurator.getUsername().equalsIgnoreCase(username)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Updates the credentials of the given configurator.
     *
     * @param configurator  The configurator to update.
     * @param newUsername   The new username.
     * @param newPassword   The new password.
     *
     * @return {@code true} if the credentials were updated, {@code false} otherwise.
     */
    public boolean updateCredentials(Configurator configurator, String newUsername, String newPassword) {
        if (newUsername == null || newUsername.isBlank()) {
            return false;
        }
        String normalized = newUsername.trim();
        if (!isUsernameAvailable(normalized, configurator)) {
            return false;
        }
        configurator.setCredentials(normalized, newPassword);
        ioManager.writeConfigurators(this.configurators);
        return true;
    }

    /**
     * Checks whether the base fields have already been configured.
     *
     * @return {@code true} if the base fields are already set, {@code false} otherwise.
     */
    public boolean areBaseFieldsSet() {
        return config.areBaseFieldsSet();
    }

    /**
     * Stores the base fields if they have not been configured yet.
     *
     * @param baseFields The fields to store as base fields.
     *
     * @return {@code true} if the fields were stored, {@code false} otherwise.
     */
    public boolean setBaseFields(List<Field> baseFields) {
        boolean success = config.setBaseFields(baseFields);
        if (success) {
            ioManager.writeConfig(config);
        }
        return success;
    }

    /**
     * Returns the configured base fields.
     *
     * @return An immutable view of the base fields.
     */
    public List<Field> getBaseFields() {
        return config.getBaseFields();
    }

    /**
     * Returns the configured common fields.
     *
     * @return An immutable view of the common fields.
     */
    public List<Field> getCommonFields() {
        return config.getCommonFields();
    }

    /**
     * Adds a common field if its name is available.
     *
     * @param field The field to add.
     *
     * @return {@code true} if the field was added, {@code false} otherwise.
     */
    public boolean addCommonField(Field field) {
        if (!isFieldNameAvailable(field.getName(), null)) {
            return false;
        }
        config.addCommonField(field);
        ioManager.writeConfig(config);
        return true;
    }

    /**
     * Removes a common field by index.
     *
     * @param index The index of the field to remove.
     *
     * @return {@code true} if the field was removed, {@code false} otherwise.
     */
    public boolean removeCommonField(int index) {
        if (isInvalidIndex(index, config.getCommonFields())) return false;
        config.removeCommonField(index);
        ioManager.writeConfig(config);
        return true;
    }

    /**
     * Toggles the mandatory flag of a common field.
     *
     * @param index The index of the field to update.
     *
     * @return {@code true} if the field was updated, {@code false} otherwise.
     */
    public boolean toggleMandatorinessCommonField(int index) {
        if (isInvalidIndex(index, config.getCommonFields())) return false;
        config.toggleMandatorinessCommonField(index);
        ioManager.writeConfig(config);
        return true;
    }

    /**
     * Returns the configured categories.
     *
     * @return An immutable view of the categories.
     */
    public List<Category> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    /**
     * Adds a new category with the provided specific fields.
     *
     * @param name              The category name.
     * @param specificFields    The specific fields to attach to the category.
     *
     * @return {@code true} if the category was added, {@code false} otherwise.
     */
    public boolean addCategory(String name, List<Field> specificFields) {
        if (!isCategoryNameAvailable(name)) {
            return false;
        }
        String normalized = name.trim();
        List<Field> validSpecifics = specificFields == null ? new ArrayList<>() : new ArrayList<>(specificFields);
        if (!checkSpecificFieldsNames(validSpecifics)) {
            return false;
        }
        Category category = new Category(normalized, validSpecifics);
        categories.add(category);
        ioManager.writeCategories(categories);
        return true;
    }

    /**
     * Removes a category by index.
     *
     * @param index The index of the category to remove.
     *
     * @return {@code true} if the category was removed, {@code false} otherwise.
     */
    public boolean removeCategory(int index) {
        if (isInvalidIndex(index, categories)) return false;
        categories.remove(index);
        ioManager.writeCategories(categories);
        return true;
    }

    /**
     * Adds a specific field to the selected category.
     *
     * @param categoryIndex     The index of the category to update.
     * @param field             The field to add.
     *
     * @return {@code true} if the field was added, {@code false} otherwise.
     */
    public boolean addSpecificField(int categoryIndex, Field field) {
        if (isInvalidIndex(categoryIndex, categories)) return false;
        Category category = categories.get(categoryIndex);
        if (!isFieldNameAvailable(field.getName(), category)) {
            return false;
        }
        category.addSpecificField(field);
        ioManager.writeCategories(categories);
        return true;
    }

    /**
     * Removes a specific field from the selected category.
     *
     * @param categoryIndex     The index of the category to update.
     * @param fieldIndex        The index of the field to remove.
     *
     * @return {@code true} if the field was removed, {@code false} otherwise.
     */
    public boolean removeSpecificField(int categoryIndex, int fieldIndex) {
        if (isInvalidIndex(categoryIndex, categories)) return false;
        Category category = categories.get(categoryIndex);
        if (isInvalidIndex(fieldIndex, category.getSpecificFields())) return false;
        category.removeSpecificField(fieldIndex);
        ioManager.writeCategories(categories);
        return true;
    }

    /**
     * Toggles the mandatory flag of a specific field in the selected category.
     *
     * @param categoryIndex     The index of the category to update.
     * @param fieldIndex        The index of the field to update.
     *
     * @return {@code true} if the field was updated, {@code false} otherwise.
     */
    public boolean toggleMandatorinessSpecificField(int categoryIndex, int fieldIndex) {
        if (isInvalidIndex(categoryIndex, categories)) return false;
        Category category = categories.get(categoryIndex);
        if (isInvalidIndex(fieldIndex, category.getSpecificFields())) return false;
        category.toggleMandatoriness(fieldIndex);
        ioManager.writeCategories(categories);
        return true;
    }

    /**
     * Checks whether the provided category name is available.
     *
     * @param name The category name to validate.
     *
     * @return {@code true} if the name is available, {@code false} otherwise.
     */
    public boolean isCategoryNameAvailable(String name) {
        if (name == null || name.trim().isBlank()) {
            return false;
        }
        String normalized = name.trim();
        for (Category category : categories) {
            if (category.getName().equalsIgnoreCase(normalized)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the given field name is available for a category.
     *
     * @param fieldName     The field name to validate.
     * @param category      The category context, or {@code null}.
     *
     * @return {@code true} if the field name is available, {@code false} otherwise.
     */
    public boolean isFieldNameAvailableForCategory(String fieldName, Category category) {
        return isFieldNameAvailable(fieldName, category);
    }

    /**
     * Returns the shared fields visible for a category.
     *
     * @param category The category to resolve, or {@code null}.
     *
     * @return The combined list of base, common, and category-specific fields.
     */
    public List<Field> getSharedFieldsForCategory(Category category) {
        List<Field> fields = new ArrayList<>();
        fields.addAll(config.getBaseFields());
        fields.addAll(config.getCommonFields());
        if (category != null) {
            fields.addAll(category.getSpecificFields());
        }
        return fields;
    }

    /**
     * Creates a valid proposal for the selected category using raw UI field values.
     *
     * @param categoryIndex The selected category index.
     * @param rawValues     The raw values inserted by the configurator.
     *
     * @return A valid proposal in {@link ProposalStatus#VALID}, or {@code null} if validation fails.
     */
    public Proposal createProposal(int categoryIndex, Map<String, String> rawValues) {
        if (isInvalidIndex(categoryIndex, categories) || rawValues == null) {
            return null;
        }
        Category category = categories.get(categoryIndex);
        List<Field> fields = getSharedFieldsForCategory(category);
        Map<String, String> normalized = normalizeAndValidateValues(fields, rawValues);
        if (normalized == null || !checkDomainRules(normalized)) {
            return null;
        }

        Proposal proposal = new Proposal(archive.nextId(), category.getName(), normalized);
        if (!proposal.markAsValid()) {
            return null;
        }
        return proposal;
    }

    /**
     * Publishes a valid proposal to the board and persists it in the archive.
     *
     * @param proposal The proposal to publish.
     *
     * @return {@code true} if publishing succeeds, {@code false} otherwise.
     */
    public boolean publishProposal(Proposal proposal) {
        if (proposal == null) {
            return false;
        }
        if (!proposal.markAsOpen()) {
            return false;
        }
        if (!archive.addOpenProposal(proposal)) {
            return false;
        }
        ioManager.writeArchive(archive);
        return true;
    }

    /**
     * Returns the current board grouped by category.
     *
     * @return Category to open proposal mapping.
     */
    public Map<String, List<Proposal>> getBoardByCategory() {
        return archive.getOpenByCategory();
    }

    /**
     * Initializes the default configurators used when no users are stored yet.
     */
    private void initializeDefaultConfigurators() {
        Configurator c1 = new Configurator(DEFAULT_CONFIGURATOR_ONE_USERNAME, DEFAULT_CONFIGURATOR_ONE_PASSWORD);
        Configurator c2 = new Configurator(DEFAULT_CONFIGURATOR_TWO_USERNAME, DEFAULT_CONFIGURATOR_TWO_PASSWORD);
        this.configurators.add(c1);
        this.configurators.add(c2);
        ioManager.writeConfigurators(this.configurators);
    }

    /**
     * Checks whether a field name is available across the current configuration.
     *
     * @param fieldName     The field name to validate.
     * @param category      The category context, or {@code null}.
     *
     * @return {@code true} if the name is available, {@code false} otherwise.
     */
    private boolean isFieldNameAvailable(String fieldName, Category category) {
        if (fieldName == null) {
            return false;
        }
        String normalized = fieldName.trim();
        if (normalized.isBlank()) {
            return false;
        }

        if (fieldNameExists(config.getBaseFields(), normalized)) {
            return false;
        }
        if (fieldNameExists(config.getCommonFields(), normalized)) {
            return false;
        }
        if (category != null && fieldNameExists(category.getSpecificFields(), normalized)) {
            return false;
        }
        return true;
    }

    /**
     * Checks whether the provided field name already exists in the given list.
     *
     * @param fields    The fields to scan.
     * @param fieldName The name to search for.
     *
     * @return {@code true} if a matching field is found, {@code false} otherwise.
     */
    private boolean fieldNameExists(List<Field> fields, String fieldName) {
        for (Field field : fields) {
            if (field.getName() != null && field.getName().equalsIgnoreCase(fieldName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates that all the given specific field names are available.
     *
     * @param specificFields The fields to validate.
     *
     * @return {@code true} if all names are valid and unique, {@code false} otherwise.
     */
    private boolean checkSpecificFieldsNames(List<Field> specificFields) {
        List<String> checkedNames = new ArrayList<>();
        for (Field field : specificFields) {
            if (field == null || field.getName() == null) {
                return false;
            }
            String name = field.getName().trim();
            if (name.isBlank()) {
                return false;
            }
            if (!isFieldNameAvailable(name, null)) {
                return false;
            }
            for (String checked : checkedNames) {
                if (checked.equalsIgnoreCase(name)) {
                    return false;
                }
            }
            checkedNames.add(name);
        }
        return true;
    }

    /**
     * Normalizes raw values based on field type and validates mandatory compilations.
     *
     * @param fields    The fields that compose the selected category template.
     * @param rawValues The raw values coming from the UI.
     *
     * @return A normalized value map or {@code null} if validation fails.
     */
    private Map<String, String> normalizeAndValidateValues(List<Field> fields, Map<String, String> rawValues) {
        Map<String, String> normalized = new LinkedHashMap<>();
        for (Field field : fields) {
            String fieldName = field.getName();
            String value = rawValues.get(fieldName);
            if (value != null) {
                value = value.trim();
            }

            if (value == null || value.isBlank()) {
                if (field.isMandatory()) {
                    return null;
                }
                continue;
            }

            String canonical = normalizeValue(value, field.getDataType());
            if (canonical == null) {
                return null;
            }
            normalized.put(fieldName, canonical);
        }
        return normalized;
    }

    /**
     * Converts a user value to its canonical storage form.
     *
     * @param rawValue The raw user value.
     * @param dataType The expected field data type.
     *
     * @return Canonical value, or {@code null} if conversion fails.
     */
    private String normalizeValue(String rawValue, DataType dataType) {
        try {
            return switch (dataType) {
                case STRING -> rawValue;
                case INTEGER -> Integer.toString(Integer.parseInt(rawValue));
                case DECIMAL -> Double.toString(Double.parseDouble(rawValue));
                case DATE -> LocalDate.parse(rawValue, USER_DATE_FORMATTER).format(DateTimeFormatter.ISO_LOCAL_DATE);
                case TIME -> LocalTime.parse(rawValue, USER_TIME_FORMATTER).format(USER_TIME_FORMATTER);
                case BOOLEAN -> parseBoolean(rawValue);
            };
        } catch (NumberFormatException | DateTimeParseException exception) {
            return null;
        }
    }

    /**
     * Checks all required domain rules for a proposal.
     *
     * @param values Canonical value map.
     *
     * @return {@code true} if all rules are respected, {@code false} otherwise.
     */
    private boolean checkDomainRules(Map<String, String> values) {
        LocalDate deadline = parseIsoDate(values.get(DEADLINE_FIELD_NAME));
        LocalDate startDate = parseIsoDate(values.get(START_DATE_FIELD_NAME));
        LocalDate endDate = parseIsoDate(values.get(END_DATE_FIELD_NAME));
        Integer participants = parseInteger(values.get(PARTICIPANTS_FIELD_NAME));
        Double fee = parseDouble(values.get(FEE_FIELD_NAME));

        if (deadline == null || !deadline.isAfter(LocalDate.now())) {
            return false;
        }
        if (startDate == null || startDate.isBefore(deadline.plusDays(2))) {
            return false;
        }
        if (endDate == null || endDate.isBefore(startDate)) {
            return false;
        }
        if (participants == null || participants <= 0) {
            return false;
        }
        return fee != null && fee >= 0.0f;
    }

    private LocalDate parseIsoDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.replace(',', '.');
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String parseBoolean(String value) {
        String normalized = value.trim().toLowerCase();
        if (normalized.equals("true") || normalized.equals("si") || normalized.equals("s")
                || normalized.equals("yes") || normalized.equals("y")) {
            return Boolean.TRUE.toString();
        }
        if (normalized.equals("false") || normalized.equals("no") || normalized.equals("n")) {
            return Boolean.FALSE.toString();
        }
        return null;
    }

    /**
     * Checks whether an index is invalid for the given list.
     *
     * @param index The index to validate.
     * @param list The list that must contain the index.
     *
     * @return {@code true} if the index is outside the list bounds, {@code false} otherwise.
     */
    private <T> boolean isInvalidIndex(int index, List<T> list) {
        return index < 0 || index >= list.size();
    }
}
