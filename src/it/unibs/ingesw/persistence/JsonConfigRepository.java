package it.unibs.ingesw.persistence;

import it.unibs.ingesw.model.SystemConfig;

/**
 * JSON-backed implementation of {@link ConfigRepository}.
 */
public class JsonConfigRepository extends JsonRepositorySupport implements ConfigRepository {
    private static final String CONFIG_FILE = "config.json";

    @Override
    public SystemConfig read() {
        SystemConfig config = readJson(resolve(CONFIG_FILE), SystemConfig.class, new SystemConfig());
        if (config == null) {
            config = new SystemConfig();
            write(config);
        }
        return config;
    }

    @Override
    public void write(SystemConfig config) {
        writeJson(resolve(CONFIG_FILE), config);
    }
}
