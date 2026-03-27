package it.unibs.ingesw.model;

/**
 * Supported lifecycle states for initiative proposals.
 *
 * <p>All instances get serialized using the {@code .name()} method, while
 * they get printed using the {@code .toString()} method.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Represents all states defined by the application lifecycle.</li>
 *   <li>Provides user-friendly labels for terminal rendering.</li>
 * </ul>
 */
public enum ProposalStatus {
    CREATED("Creata"),
    VALID("Valida"),
    OPEN("Aperta"),
    CONFIRMED("Confermata"),
    CLOSE("Chiusa"),
    WITHDRAWED("Ritirata"),
    CANCELED("Annullata");

    private final String description;

    ProposalStatus(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
