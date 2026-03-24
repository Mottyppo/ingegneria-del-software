package it.unibs.ingesw.model;

/**
 * Supported field categories used by the application.
 *
 * <p>All instances get serialized using the {@code .name()} method, while
 * they get printed using the {@code .toString()} method.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Distinguishes base, common, and specific fields.</li>
 *   <li>Renders each value with a user-friendly label.</li>
 * </ul>
 */
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
