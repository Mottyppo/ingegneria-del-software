package it.unibs.ingesw.model;

//TODO: Documentazione

public class Field {
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

    public void toggleMandatoriness() {
        mandatory = !mandatory;
    }

    @Override
    public String toString() {
        return "Campo{" +
                "nome='" + name + '\'' +
                ", descrizione='" + description + '\'' +
                ", obbligatorio=" + mandatory +
                ", tipo=" + type +
                ", tipo di dato=" + dataType +
                '}';
    }
}
