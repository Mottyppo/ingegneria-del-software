package it.unibs.ingesw.persistence;

import it.unibs.ingesw.model.Configurator;

import java.util.List;

/**
 * Repository for persisted configurator accounts.
 *
 * <p>The repository abstracts storage of all configurators used by the
 * authentication and first-access flows.</p>
 */
public interface ConfiguratorRepository {

    /**
     * Loads all persisted configurators.
     *
     * @return The stored configurators, or an empty list when unavailable.
     */
    List<Configurator> readAll();

    /**
     * Stores the full configurator collection.
     *
     * @param configurators The configurators to persist.
     */
    void writeAll(List<Configurator> configurators);
}
