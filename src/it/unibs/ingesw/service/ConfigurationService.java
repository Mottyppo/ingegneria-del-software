package it.unibs.ingesw.service;

import it.unibs.ingesw.model.Category;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.SystemConfig;
import it.unibs.ingesw.persistence.CategoryRepository;
import it.unibs.ingesw.persistence.ConfigRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles configuration use cases related to fields and categories.
 *
 * <p>The service encapsulates validation and persistence rules for base fields,
 * common fields, categories, and category-specific fields.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Manages base and common fields.</li>
 *   <li>Manages categories and specific fields.</li>
 *   <li>Checks field and category name availability.</li>
 *   <li>Builds the shared field set visible for a category.</li>
 * </ul>
 */
public class ConfigurationService {
    private final SystemConfig config;
    private final List<Category> categories;
    private final ConfigRepository configRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Creates a configuration service over the shared in-memory state.
     *
     * @param config                The loaded system configuration.
     * @param categories            The loaded categories.
     * @param configRepository      The configuration repository used to store mutations.
     * @param categoryRepository    The category repository used to store mutations.
     */
    public ConfigurationService(
            SystemConfig config,
            List<Category> categories,
            ConfigRepository configRepository,
            CategoryRepository categoryRepository
    ) {
        this.config = config;
        this.categories = categories;
        this.configRepository = configRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Checks whether the base fields have already been configured.
     *
     * @return {@code true} if base fields are available, {@code false} otherwise.
     */
    public boolean areBaseFieldsSet() {
        return config.areBaseFieldsSet();
    }

    /**
     * Stores the base fields when they have not been configured yet.
     *
     * @param baseFields The base fields to persist.
     * @return {@code true} if the fields were stored, {@code false} otherwise.
     */
    public boolean setBaseFields(List<Field> baseFields) {
        boolean success = config.setBaseFields(baseFields);
        if (success) {
            configRepository.write(config);
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
     * Adds a new common field when its name is available.
     *
     * @param field The field to add.
     * @return {@code true} if the field was added, {@code false} otherwise.
     */
    public boolean addCommonField(Field field) {
        if (field == null || !isFieldNameAvailableGlobally(field.getName())) {
            return false;
        }
        config.addCommonField(field);
        configRepository.write(config);
        return true;
    }

    /**
     * Removes a common field by index.
     *
     * @param index The index to remove.
     * @return {@code true} if the field was removed, {@code false} otherwise.
     */
    public boolean removeCommonField(int index) {
        if (isInvalidIndex(index, config.getCommonFields())) {
            return false;
        }
        config.removeCommonField(index);
        configRepository.write(config);
        return true;
    }

    /**
     * Toggles the mandatory flag of a common field.
     *
     * @param index The field index.
     * @return {@code true} if the field was updated, {@code false} otherwise.
     */
    public boolean toggleMandatorinessCommonField(int index) {
        if (isInvalidIndex(index, config.getCommonFields())) {
            return false;
        }
        config.toggleMandatorinessCommonField(index);
        configRepository.write(config);
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
     * Adds a category with its specific fields when validation succeeds.
     *
     * @param name           The category name.
     * @param specificFields The specific fields to attach.
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

        categories.add(new Category(normalized, validSpecifics));
        categoryRepository.writeAll(categories);
        return true;
    }

    /**
     * Removes a category by index.
     *
     * @param index The category index.
     * @return {@code true} if the category was removed, {@code false} otherwise.
     */
    public boolean removeCategory(int index) {
        if (isInvalidIndex(index, categories)) {
            return false;
        }
        categories.remove(index);
        categoryRepository.writeAll(categories);
        return true;
    }

    /**
     * Adds a specific field to a category.
     *
     * @param categoryIndex The category index.
     * @param field         The field to add.
     * @return {@code true} if the field was added, {@code false} otherwise.
     */
    public boolean addSpecificField(int categoryIndex, Field field) {
        if (field == null || isInvalidIndex(categoryIndex, categories)) {
            return false;
        }

        Category category = categories.get(categoryIndex);
        if (!isFieldNameAvailable(field.getName(), category)) {
            return false;
        }

        category.addSpecificField(field);
        categoryRepository.writeAll(categories);
        return true;
    }

    /**
     * Removes a specific field from a category.
     *
     * @param categoryIndex The category index.
     * @param fieldIndex    The field index.
     * @return {@code true} if the field was removed, {@code false} otherwise.
     */
    public boolean removeSpecificField(int categoryIndex, int fieldIndex) {
        if (isInvalidIndex(categoryIndex, categories)) {
            return false;
        }

        Category category = categories.get(categoryIndex);
        if (isInvalidIndex(fieldIndex, category.getSpecificFields())) {
            return false;
        }

        category.removeSpecificField(fieldIndex);
        categoryRepository.writeAll(categories);
        return true;
    }

    /**
     * Toggles the mandatory flag of a specific field.
     *
     * @param categoryIndex The category index.
     * @param fieldIndex    The field index.
     * @return {@code true} if the field was updated, {@code false} otherwise.
     */
    public boolean toggleMandatorinessSpecificField(int categoryIndex, int fieldIndex) {
        if (isInvalidIndex(categoryIndex, categories)) {
            return false;
        }

        Category category = categories.get(categoryIndex);
        if (isInvalidIndex(fieldIndex, category.getSpecificFields())) {
            return false;
        }

        category.toggleMandatoriness(fieldIndex);
        categoryRepository.writeAll(categories);
        return true;
    }

    /**
     * Checks whether the given category name is available.
     *
     * @param name The category name to validate.
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
     * Checks whether a field name is available in the current configuration.
     *
     * @param fieldName The field name to validate.
     * @param category  The category context, or {@code null}.
     * @return {@code true} if the name is available, {@code false} otherwise.
     */
    public boolean isFieldNameAvailableForCategory(String fieldName, Category category) {
        return isFieldNameAvailable(fieldName, category);
    }

    /**
     * Checks whether a field name is globally available across shared fields and
     * all category-specific fields.
     *
     * @param fieldName The field name to validate.
     * @return {@code true} if the name is globally available, {@code false} otherwise.
     */
    public boolean isFieldNameAvailableGlobally(String fieldName) {
        if (!isFieldNameAvailable(fieldName, null)) {
            return false;
        }

        String normalized = fieldName.trim();
        for (Category category : categories) {
            if (fieldNameExists(category.getSpecificFields(), normalized)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds a category by name, ignoring letter case.
     *
     * @param name The category name to search for.
     * @return The matching category, or {@code null} if unavailable.
     */
    public Category findCategoryByName(String name) {
        if (name == null || name.trim().isBlank()) {
            return null;
        }

        String normalized = name.trim();
        for (Category category : categories) {
            if (category.getName().equalsIgnoreCase(normalized)) {
                return category;
            }
        }
        return null;
    }

    /**
     * Returns the fields shared by all proposals of the given category.
     *
     * @param category The category context, or {@code null}.
     * @return A new list containing base, common, and category-specific fields.
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
     * Checks whether a field name is available across base, common, and specific fields.
     *
     * @param fieldName The field name to validate.
     * @param category  The category context, or {@code null}.
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
        return category == null || !fieldNameExists(category.getSpecificFields(), normalized);
    }

    /**
     * Checks whether a field name already exists in the given list.
     *
     * @param fields    The fields to scan.
     * @param fieldName The name to search for.
     * @return {@code true} if a field with the same name exists, {@code false} otherwise.
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
     * Validates a batch of specific fields during category creation.
     *
     * @param specificFields The fields to validate.
     * @return {@code true} if all field names are valid and unique, {@code false} otherwise.
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
     * Checks whether an index is outside the bounds of the given list.
     *
     * @param index The index to validate.
     * @param list  The list to check.
     * @param <T>   The list item type.
     * @return {@code true} if the index is invalid, {@code false} otherwise.
     */
    private <T> boolean isInvalidIndex(int index, List<T> list) {
        return index < 0 || index >= list.size();
    }
}
