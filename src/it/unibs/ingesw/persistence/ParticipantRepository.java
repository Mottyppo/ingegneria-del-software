package it.unibs.ingesw.persistence;

import it.unibs.ingesw.model.Participant;

import java.util.List;

/**
 * Repository for persisted participant accounts and personal spaces.
 *
 * <p>The repository stores the participant collection together with their
 * notification state.</p>
 */
public interface ParticipantRepository {

    /**
     * Loads all persisted participants.
     *
     * @return The stored participants, or an empty list when unavailable.
     */
    List<Participant> readAll();

    /**
     * Stores the full participant collection.
     *
     * @param participants The participants to persist.
     */
    void writeAll(List<Participant> participants);
}
