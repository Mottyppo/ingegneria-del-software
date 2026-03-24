package it.unibs.ingesw.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import it.unibs.ingesw.model.Category;
import it.unibs.ingesw.model.Configurator;
import it.unibs.ingesw.model.SystemConfig;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class IOManager {
    private static final String DATA_DIR = "data";
    private static final String CONFIG_FILE = "config.json";
    private static final String CATEGORIES_FILE = "categories.json";
    private static final String USERS_FILE = "users.json";

    private final Gson gson;

    public IOManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        ensureDataDir();
    }

    public SystemConfig readConfig() {
        Path path = resolve(CONFIG_FILE);
        SystemConfig config = readJson(path, SystemConfig.class, new SystemConfig());
        if (config == null) {
            config = new SystemConfig();
            writeConfig(config);
        }
        return config;
    }

    public void writeConfig(SystemConfig config) {
        writeJson(resolve(CONFIG_FILE), config);
    }

    public List<Category> readCategories() {
        Type listType = new TypeToken<List<Category>>() {
        }.getType();
        List<Category> categories = readJson(resolve(CATEGORIES_FILE), listType, new ArrayList<>());
        return categories == null ? new ArrayList<>() : categories;
    }

    public void writeCategories(List<Category> categories) {
        writeJson(resolve(CATEGORIES_FILE), categories);
    }

    public List<Configurator> readConfigurators() {
        Type listType = new TypeToken<List<Configurator>>() {
        }.getType();
        List<Configurator> configurators = readJson(resolve(USERS_FILE), listType, new ArrayList<>());
        return configurators == null ? new ArrayList<>() : configurators;
    }

    public void writeConfigurators(List<Configurator> configurators) {
        writeJson(resolve(USERS_FILE), configurators);
    }

    private void ensureDataDir() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Impossibile creare la cartella dati: " + e.getMessage());
        }
    }

    private Path resolve(String filename) {
        return Paths.get(DATA_DIR, filename);
    }

    private <T> T readJson(Path path, Type type, T defaultValue) {
        if (!Files.exists(path)) {
            return defaultValue;
        }
        try {
            String json = Files.readString(path);
            if (json == null || json.isBlank()) {
                return defaultValue;
            }
            return gson.fromJson(json, type);
        } catch (IOException e) {
            System.err.println("Errore nella lettura del file " + path + ": " + e.getMessage());
            return defaultValue;
        }
    }

    private void writeJson(Path path, Object obj) {
        try {
            String json = gson.toJson(obj);
            Files.writeString(path, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Errore nella scrittura del file " + path + ": " + e.getMessage());
        }
    }
}
