package it.unibs.ingesw.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a category with its own specific fields.
 *
 * <p>Category names are unique in a case-insensitive way.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *     <li>Stores the category name.</li>
 *     <li>Manages the category-specific fields.</li>
 *     <li>Supports adding, removing, and toggling the mandatory flag of specific fields.</li>
 * </ul>
 */
public class Category {
    private static final String TO_STRING_PREFIX =  "Categoria{";
    private static final String NAME_LABEL =        "nome='";
    private static final String FIELDS_LABEL =      ", campi specifici=";
    private static final String TO_STRING_SUFFIX =  "}";

    private final String name;
    private List<Field> specificFields;

    public Category(String name, List<Field> specificFields) {
        this.name = name;
        this.specificFields = specificFields == null ? new ArrayList<>() : specificFields;
    }

    public String getName() {
        return name;
    }

    public List<Field> getSpecificFields() {
        if (specificFields == null) {
            specificFields = new ArrayList<>();
        }
        return specificFields;
    }

    /**
     * Adds a specific field to this category.
     *
     * @param field The field to add.
     */
    public void addSpecificField(Field field) {
        specificFields.add(field);
    }

    /**
     * Removes the specific field at the given index.
     *
     * @param index The index of the field to remove.
     */
    public void removeSpecificField(int index) {
        specificFields.remove(index);
    }

    /**
     * Toggles the mandatoriness of the specific field at the given index.
     *
     * @param index The index of the field to update.
     */
    public void toggleMandatoriness(int index) {
        specificFields.get(index).toggleMandatoriness();
    }

    @Override
    public String toString() {
        return TO_STRING_PREFIX +
                NAME_LABEL + name + '\'' +
                FIELDS_LABEL + specificFields +
                TO_STRING_SUFFIX;
    }
}
