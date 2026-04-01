package it.unibs.ingesw.service;

import it.unibs.ingesw.model.Configurator;
import it.unibs.ingesw.model.Participant;
import it.unibs.ingesw.persistence.ConfiguratorRepository;
import it.unibs.ingesw.persistence.ParticipantRepository;

import java.util.List;

/**
 * Handles authentication and account-related use cases.
 *
 * <p>The service centralizes credential checks and user registration logic,
 * keeping controller/facade classes focused on orchestration rather than
 * account validation rules.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Authenticates configurators and participants.</li>
 *   <li>Registers new participants.</li>
 *   <li>Updates configurator credentials.</li>
 *   <li>Ensures global username uniqueness across all user roles.</li>
 * </ul>
 */
public class AuthenticationService {
    private final List<Configurator> configurators;
    private final List<Participant> participants;
    private final ConfiguratorRepository configuratorRepository;
    private final ParticipantRepository participantRepository;

    /**
     * Creates an authentication service over the shared in-memory state.
     *
     * @param configurators             The configurators currently loaded in memory.
     * @param participants              The participants currently loaded in memory.
     * @param configuratorRepository    The configurator repository used to store mutations.
     * @param participantRepository     The participant repository used to store mutations.
     */
    public AuthenticationService(
            List<Configurator> configurators,
            List<Participant> participants,
            ConfiguratorRepository configuratorRepository,
            ParticipantRepository participantRepository
    ) {
        this.configurators = configurators;
        this.participants = participants;
        this.configuratorRepository = configuratorRepository;
        this.participantRepository = participantRepository;
    }

    /**
     * Authenticates a configurator using username and password.
     *
     * @param username The username to verify.
     * @param password The password to verify.
     * @return The matching configurator, or {@code null} if credentials are invalid.
     */
    public Configurator authenticateConfigurator(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        for (Configurator configurator : configurators) {
            if (configurator.getUsername().equalsIgnoreCase(username)
                    && configurator.getPassword().equals(password)) {
                return configurator;
            }
        }
        return null;
    }

    /**
     * Authenticates a participant using username and password.
     *
     * @param username The username to verify.
     * @param password The password to verify.
     * @return The matching participant, or {@code null} if credentials are invalid.
     */
    public Participant authenticateParticipant(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        for (Participant participant : participants) {
            if (participant.getUsername().equalsIgnoreCase(username)
                    && participant.getPassword().equals(password)) {
                return participant;
            }
        }
        return null;
    }

    /**
     * Registers a new participant when data is valid and the username is available.
     *
     * @param name      The participant name.
     * @param surname   The participant surname.
     * @param username  The desired username.
     * @param password  The desired password.
     * @return The created participant, or {@code null} if validation fails.
     */
    public Participant signUpParticipant(String name, String surname, String username, String password) {
        if (name == null || surname == null || username == null || password == null) {
            return null;
        }

        String normalizedName = name.trim();
        String normalizedSurname = surname.trim();
        String normalizedUsername = username.trim();

        if (normalizedName.isBlank() || normalizedSurname.isBlank()
                || normalizedUsername.isBlank() || password.isBlank()) {
            return null;
        }
        if (!isUsernameAvailable(normalizedUsername, null, null)) {
            return null;
        }

        Participant participant = new Participant(normalizedName, normalizedSurname, normalizedUsername, password);
        participants.add(participant);
        participantRepository.writeAll(participants);
        return participant;
    }

    /**
     * Updates the credentials of the given configurator.
     *
     * @param configurator  The configurator to update.
     * @param newUsername   The new username.
     * @param newPassword   The new password.
     * @return {@code true} if the credentials were updated, {@code false} otherwise.
     */
    public boolean updateCredentials(Configurator configurator, String newUsername, String newPassword) {
        if (configurator == null || newUsername == null || newUsername.isBlank()
                || newPassword == null || newPassword.isBlank()) {
            return false;
        }

        String normalized = newUsername.trim();
        if (!isUsernameAvailable(normalized, configurator, null)) {
            return false;
        }

        configurator.setCredentials(normalized, newPassword);
        configuratorRepository.writeAll(configurators);
        return true;
    }

    /**
     * Stores default configurators when no persisted ones are available yet.
     *
     * @param firstUsername     The first default username.
     * @param firstPassword     The first default password.
     * @param secondUsername    The second default username.
     * @param secondPassword    The second default password.
     */
    public void initializeDefaultConfiguratorsIfNeeded(
            String firstUsername,
            String firstPassword,
            String secondUsername,
            String secondPassword
    ) {
        if (!configurators.isEmpty()) {
            return;
        }

        configurators.add(new Configurator(firstUsername, firstPassword));
        configurators.add(new Configurator(secondUsername, secondPassword));
        configuratorRepository.writeAll(configurators);
    }

    /**
     * Checks whether a username is globally available across all users.
     *
     * @param username              The username to validate.
     * @param excludeConfigurator   An optional configurator to exclude from the check.
     * @param excludeParticipant    An optional participant to exclude from the check.
     * @return {@code true} if the username is available, {@code false} otherwise.
     */
    private boolean isUsernameAvailable(
            String username,
            Configurator excludeConfigurator,
            Participant excludeParticipant
    ) {
        if (username == null || username.trim().isBlank()) {
            return false;
        }

        String normalized = username.trim();

        for (Configurator configurator : configurators) {
            if (excludeConfigurator != null && configurator == excludeConfigurator) {
                continue;
            }
            if (configurator.getUsername().equalsIgnoreCase(normalized)) {
                return false;
            }
        }

        for (Participant participant : participants) {
            if (excludeParticipant != null && participant == excludeParticipant) {
                continue;
            }
            if (participant.getUsername().equalsIgnoreCase(normalized)) {
                return false;
            }
        }

        return true;
    }
}
