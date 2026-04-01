package it.unibs.ingesw.persistence;

import it.unibs.ingesw.model.Archive;

/**
 * Repository for the persisted proposal archive.
 *
 * <p>The repository stores the whole archive aggregate containing proposals and
 * their lifecycle history.</p>
 */
public interface ArchiveRepository {

    /**
     * Loads the persisted proposal archive.
     *
     * @return The stored archive, or an empty archive when unavailable.
     */
    Archive read();

    /**
     * Stores the proposal archive.
     *
     * @param archive The archive to persist.
     */
    void write(Archive archive);
}
