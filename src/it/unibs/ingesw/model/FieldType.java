package it.unibs.ingesw.model;

//TODO: Documentazione

public enum FieldType {
    BASE("Base"),
    COMMON("Comune"),
    SPECIFIC("Specifico");

    private final String description;

    FieldType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.description;
    }
}