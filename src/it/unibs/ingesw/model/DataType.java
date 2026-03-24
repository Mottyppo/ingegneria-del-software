package it.unibs.ingesw.model;

//TODO: Documentazione

/**
 * Tipi di dato supportati per la compilazione dei campi.
 */
public enum DataType {
    STRING("Testo"),
    INTEGER("Intero"),
    DECIMAL("Decimale"),
    DATE("Data"),
    TIME("Ora"),
    BOOLEAN("Booleano");

    private final String description;

    DataType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.description;
    }
}