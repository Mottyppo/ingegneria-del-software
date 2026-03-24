package it.unibs.ingesw.model;

/**
 * Represents a field used by the system configuration.
 *
 * <p>Field names are unique in a case-insensitive way across base, common, and
 * category-specific fields.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *     <li>Stores the field name, description, and data type.</li>
 *     <li>Tracks whether the field is mandatory.</li>
 *     <li>Supports toggling the mandatory flag.</li>
 * </ul>
 */
public class Field {
    private static final String TO_STRING_PREFIX =  "Campo{";
    private static final String NAME_LABEL =        "nome='";
    private static final String DESCRIPTION_LABEL = ", descrizione='";
    private static final String MANDATORY_LABEL =   ", obbligatorio=";
    private static final String TYPE_LABEL =        ", tipo=";
    private static final String DATA_TYPE_LABEL =   ", tipo di dato=";
    private static final String TO_STRING_SUFFIX =  "}";

    private final String name;
    private final String description;
    private boolean mandatory;
    private final FieldType type;
    private final DataType dataType;

    public Field(String name, String description, boolean mandatory, FieldType type, DataType dataType) {
        this.name = name;
        this.description = description;
        this.mandatory = mandatory;
        this.type = type;
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public FieldType getType() {
        return type;
    }

    public DataType getDataType() {
        return dataType;
    }

    /**
     * Toggles the mandatory state of this field.
     */
    public void toggleMandatoriness() {
        mandatory = !mandatory;
    }

    @Override
    public String toString() {
        return TO_STRING_PREFIX +
                NAME_LABEL + name + '\'' +
                DESCRIPTION_LABEL + description + '\'' +
                MANDATORY_LABEL + mandatory +
                TYPE_LABEL + type +
                DATA_TYPE_LABEL + dataType +
                TO_STRING_SUFFIX;
    }
}
