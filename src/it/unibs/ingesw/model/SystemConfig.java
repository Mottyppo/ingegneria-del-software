package it.unibs.ingesw.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the configuration stored by the system.
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *     <li>Stores the base fields configured once at first access.</li>
 *     <li>Stores the common fields shared across categories.</li>
 *     <li>Provides read-only access to the stored field lists.</li>
 * </ul>
 */
public class SystemConfig {
    private static final String TO_STRING_PREFIX =      "SystemConfig{";
    private static final String BASE_FIELDS_LABEL =     "campi base=";
    private static final String COMMON_FIELDS_LABEL =   ", campi comuni=";
    private static final String TO_STRING_SUFFIX =      "}";

    private List<Field> baseFields;
    private List<Field> commonFields;

    public SystemConfig() {
        this.baseFields = new ArrayList<>();
        this.commonFields = new ArrayList<>();
    }

    /**
     * Checks whether the base fields have already been configured.
     *
     * @return {@code true} if at least one base field has been configured,
     *         {@code false} otherwise.
     */
    public boolean areBaseFieldsSet() {
        return baseFields != null && !baseFields.isEmpty();
    }

    /**
     * Sets the base fields only if they have not been configured yet.
     *
     * @param baseFields The fields to store as base fields.
     *
     * @return {@code true} if the base fields were stored, {@code false} if they were already set.
     */
    public boolean setBaseFields(List<Field> baseFields) {
        if (areBaseFieldsSet()) {
            return false;
        }
        this.baseFields = new ArrayList<>(baseFields);
        return true;
    }

    public List<Field> getBaseFields() {
        return baseFields == null ? List.of() : Collections.unmodifiableList(baseFields);
    }

    public List<Field> getCommonFields() {
        return commonFields == null ? List.of() : Collections.unmodifiableList(commonFields);
    }

    /**
     * Adds a common field to the configuration.
     *
     * @param field The field to add.
     */
    public void addCommonField(Field field) {
        if (commonFields == null) {
            commonFields = new ArrayList<>();
        }
        commonFields.add(field);
    }

    /**
     * Removes the common field at the given index.
     *
     * @param index The index of the field to remove.
     */
    public void removeCommonField(int index) {
        if (commonFields == null) {
            return;
        }
        commonFields.remove(index);
    }

    /**
     * Toggles the mandatory flag of the common field at the given index.
     *
     * @param index The index of the field to update.
     */
    public void toggleMandatorinessCommonField(int index) {
        if (commonFields == null) {
            return;
        }
        commonFields.get(index).toggleMandatoriness();
    }

    @Override
    public String toString() {
        return TO_STRING_PREFIX +
                BASE_FIELDS_LABEL + baseFields +
                COMMON_FIELDS_LABEL + commonFields +
                TO_STRING_SUFFIX;
    }
}
