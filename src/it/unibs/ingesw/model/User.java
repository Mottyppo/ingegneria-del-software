package it.unibs.ingesw.model;

/**
 * Base abstract class for system users.
 *
 * <p>Usernames are unique in a case-insensitive way.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *     <li>Stores the username.</li>
 *     <li>Stores the password.</li>
 *     <li>Provides shared credential accessors for subclasses.</li>
 * </ul>
 */
public abstract class User {
    private static final String TO_STRING_PREFIX =  "Utente{";
    private static final String USERNAME_LABEL =    "username='";
    private static final String TO_STRING_SUFFIX =  "}";

    protected String username;
    protected String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return TO_STRING_PREFIX +
                USERNAME_LABEL + username + '\'' +
                TO_STRING_SUFFIX;
    }
}
