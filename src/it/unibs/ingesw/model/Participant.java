package it.unibs.ingesw.model;

/**
 * Represents a "fruitore" account interacting with the front-end side of the application.
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Stores personal name and surname.</li>
 *   <li>Stores credentials inherited from {@link User}.</li>
 *   <li>Owns a personal space containing received notifications.</li>
 * </ul>
 */
public class Participant extends User {
    private static final String TO_STRING_PREFIX =      "Fruitore{";
    private static final String NAME_LABEL =            "nome='";
    private static final String SURNAME_LABEL =         "', cognome='";
    private static final String USERNAME_LABEL =        "', username='";
    private static final String TO_STRING_SUFFIX =      "'}";

    private final String name;
    private final String surname;
    private PersonalSpace personalSpace;

    public Participant(String name, String surname, String username, String password) {
        this(name, surname, username, password, null);
    }

    public Participant(
            String name,
            String surname,
            String username,
            String password,
            PersonalSpace personalSpace
    ) {
        super(username, password);
        this.name = name;
        this.surname = surname;
        this.personalSpace = personalSpace == null ? new PersonalSpace() : personalSpace;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public PersonalSpace getPersonalSpace() {
        if (personalSpace == null) {
            personalSpace = new PersonalSpace();
        }
        return personalSpace;
    }

    @Override
    public String toString() {
        return TO_STRING_PREFIX +
                NAME_LABEL + name +
                SURNAME_LABEL + surname +
                USERNAME_LABEL + username +
                TO_STRING_SUFFIX;
    }
}
