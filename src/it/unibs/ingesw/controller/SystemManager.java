package it.unibs.ingesw.controller;

import it.unibs.ingesw.io.IOManager;
import it.unibs.ingesw.model.Category;
import it.unibs.ingesw.model.Configurator;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.SystemConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Main controller of the application, coordinates persistence and validation for the application domain.
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Authenticates configurators and updates their credentials.</li>
 *   <li>Manages base, common, and category-specific fields.</li>
 *   <li>Ensures category and field names are unique in a case-insensitive way.</li>
 * </ul>
 */
public class SystemManager {
    private static final String DEFAULT_CONFIGURATOR_ONE_USERNAME = "crocerossaitaliana";
    private static final String DEFAULT_CONFIGURATOR_ONE_PASSWORD = "ginevra1864";
    private static final String DEFAULT_CONFIGURATOR_TWO_USERNAME = "alpinibrescia";
    private static final String DEFAULT_CONFIGURATOR_TWO_PASSWORD = "nikolajewka1943";

    private final IOManager ioManager;
    private final List<Configurator> configurators;
    private final List<Category> categories;
    private final SystemConfig config;

    public SystemManager() {
        this.ioManager = new IOManager();
        this.config = ioManager.readConfig();
        this.categories = ioManager.readCategories();
        this.configurators = ioManager.readConfigurators();

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
