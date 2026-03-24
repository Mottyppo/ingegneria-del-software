package it.unibs.ingesw.model;

/**
 * Supported data types for field compilation.
 *
 * <p>All instances get serialized using the {@code .name()} method, while
 * they get printed using the {@code .toString()} method.</p>
 *
 * <p><strong>Features:</strong></p>
 *  <ul>
 *      <li>Provides the supported field data types.</li>
 *      <li>Renders each value with a user-friendly label.</li>
 *  </ul>
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
