package it.unibs.ingesw.model;

/**
 * Represents a configurator account used to manage the system.
 *
 * <p>Configurator names are unique in a case-insensitive way.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *     <li>Stores the current credentials.</li>
 *     <li>Tracks whether the account is being accessed for the first time.</li>
 *     <li>Forces credential updates after first access.</li>
 * </ul>
 */
public class Configurator extends User {
    private static final String TO_STRING_PREFIX =      "Configuratore{";
    private static final String USERNAME_LABEL =        "username='";
    private static final String FIRST_ACCESS_LABEL =    ", primo accesso=";
    private static final String TO_STRING_SUFFIX =      "}";

    private boolean firstAccess;

    public Configurator(String username, String password) {
        super(username, password);
        this.firstAccess = true;
    }

    public boolean isFirstAccess() {
        return firstAccess;
    }

    /**
     * Updates the configurator credentials and marks the first access as completed.
     *
     * @param username The new username.
     * @param password The new password.
     */
    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
        this.firstAccess = false;
    }

    @Override
    public String toString() {
        return TO_STRING_PREFIX +
                USERNAME_LABEL + username + '\'' +
                FIRST_ACCESS_LABEL + firstAccess +
                TO_STRING_SUFFIX;
    }
}
