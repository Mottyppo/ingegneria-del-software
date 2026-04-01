package it.unibs.ingesw.persistence;

import it.unibs.ingesw.model.SystemConfig;

/**
 * Repository for the persisted system configuration.
 *
 * <p>The repository isolates loading and storing the unique configuration
 * aggregate that contains base and common fields.</p>
 */
public interface ConfigRepository {

    /**
     * Loads the persisted configuration snapshot.
     *
     * @return The stored configuration, or a default one when unavailable.
     */
    SystemConfig read();

    /**
     * Stores the configuration snapshot.
     *
     * @param config The configuration to persist.
     */
    void write(SystemConfig config);
}
