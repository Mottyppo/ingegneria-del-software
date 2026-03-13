package it.unibs.ingesw.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import it.unibs.ingesw.model.Categoria;
import it.unibs.ingesw.model.Configuratore;
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
    private static final String CATEGORIE_FILE = "categorie.json";
    private static final String UTENTI_FILE = "utenti.json";

    private final Gson gson;

    public IOManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        ensureDataDir();
    }

    public SystemConfig leggiConfig() {
        Path path = resolve(CONFIG_FILE);
        SystemConfig config = readJson(path, SystemConfig.class, new SystemConfig());
        if (config == null) {
            config = new SystemConfig();
            scriviConfig(config);
        }
        return config;
    }

    public void scriviConfig(SystemConfig config) {
        writeJson(resolve(CONFIG_FILE), config);
    }

    public List<Categoria> leggiCategorie() {
        Type listType = new TypeToken<List<Categoria>>() {
        }.getType();
        List<Categoria> categorie = readJson(resolve(CATEGORIE_FILE), listType, new ArrayList<>());
        return categorie == null ? new ArrayList<>() : categorie;
    }

    public void scriviCategorie(List<Categoria> categorie) {
        writeJson(resolve(CATEGORIE_FILE), categorie);
    }

    public List<Configuratore> leggiConfiguratori() {
        Type listType = new TypeToken<List<Configuratore>>() {
        }.getType();
        List<Configuratore> configuratori = readJson(resolve(UTENTI_FILE), listType, new ArrayList<>());
        return configuratori == null ? new ArrayList<>() : configuratori;
    }

    public void scriviConfiguratori(List<Configuratore> configuratori) {
        writeJson(resolve(UTENTI_FILE), configuratori);
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
