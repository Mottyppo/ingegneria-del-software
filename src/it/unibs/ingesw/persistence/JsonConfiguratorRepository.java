package it.unibs.ingesw.persistence;

import com.google.gson.reflect.TypeToken;
import it.unibs.ingesw.model.Configurator;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON-backed implementation of {@link ConfiguratorRepository}.
 */
public class JsonConfiguratorRepository extends JsonRepositorySupport implements ConfiguratorRepository {
    private static final String CONFIGURATORS_FILE = "configurators.json";

    @Override
    public List<Configurator> readAll() {
        Type listType = new TypeToken<List<Configurator>>() {
        }.getType();
        List<Configurator> configurators = readJson(resolve(CONFIGURATORS_FILE), listType, new ArrayList<>());
        return configurators == null ? new ArrayList<>() : configurators;
    }

    @Override
    public void writeAll(List<Configurator> configurators) {
        writeJson(resolve(CONFIGURATORS_FILE), configurators);
    }
}
