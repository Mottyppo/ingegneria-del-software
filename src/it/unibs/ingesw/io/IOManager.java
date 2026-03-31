package it.unibs.ingesw.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import it.unibs.ingesw.model.Archive;
import it.unibs.ingesw.console.format.AnsiColors;
import it.unibs.ingesw.console.format.AnsiWeights;
import it.unibs.ingesw.console.format.FormatStrings;
import it.unibs.ingesw.model.Category;
import it.unibs.ingesw.model.Configurator;
import it.unibs.ingesw.model.Participant;
import it.unibs.ingesw.model.SystemConfig;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides persistence operations for various model entities.
 *
 * <p>The class manages JSON serialization and deserialization in the local data directory,
 * guaranteeing file creation and safe fallbacks when files are missing or unreadable.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Reads and writes the system configuration.</li>
 *   <li>Reads and writes categories, configurator and fruitori credentials.</li>
 *   <li>Reads and writes the full proposal archive.</li>
 *   <li>Creates the data folder automatically and prints formatted I/O errors.</li>
 * </ul>
 */
public class IOManager {
    private static final String DEFAULT_DATA_DIR = "data";
    private static final String DATA_DIR_PROPERTY = "ingesw.data.dir";
    private static final String CONFIG_FILE = "config.json";
    private static final String CATEGORIES_FILE = "categories.json";
    private static final String CONFIGURATORS_FILE = "configurators.json";
    private static final String PARTICIPANTS_FILE = "participants.json";
    private static final String PROPOSALS_FILE = "proposals.json";

    private static final String ERROR_DIR_CREATION_TEMPLATE = "Impossibile creare la cartella dati: %s";
    private static final String ERROR_READ_TEMPLATE = "Errore nella lettura del file %s: %s";
    private static final String ERROR_WRITE_TEMPLATE = "Errore nella scrittura del file %s: %s";

    private final Gson gson;
    private final Path dataDir;

    /**
     * Creates an I/O manager and ensures the data directory exists.
     *
     * <p>The base folder can be overridden through system property
     * {@code ingesw.data.dir}; when missing, {@code data} is used.</p>
     */
    public IOManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataDir = Paths.get(System.getProperty(DATA_DIR_PROPERTY, DEFAULT_DATA_DIR));
        ensureDataDir();
    }

    /**
     * Loads system configuration from disk, creating a default file if needed.
     *
     * @return The loaded system configuration.
     */
    public SystemConfig readConfig() {
        Path path = resolve(CONFIG_FILE);
        SystemConfig config = readJson(path, SystemConfig.class, new SystemConfig());
        if (config == null) {
            config = new SystemConfig();
            writeConfig(config);
        }
        return config;
    }

    /**
     * Persists the system configuration to disk.
     *
     * @param config The configuration to store.
     */
    public void writeConfig(SystemConfig config) {
        writeJson(resolve(CONFIG_FILE), config);
    }

    /**
     * Loads all saved categories from disk.
     *
     * @return The list of categories, or an empty list when no data is available.
     */
    public List<Category> readCategories() {
        Type listType = new TypeToken<List<Category>>() {
        }.getType();
        List<Category> categories = readJson(resolve(CATEGORIES_FILE), listType, new ArrayList<>());
        return categories == null ? new ArrayList<>() : categories;
    }

    /**
     * Persists categories to disk.
     *
     * @param categories The categories to store.
     */
    public void writeCategories(List<Category> categories) {
        writeJson(resolve(CATEGORIES_FILE), categories);
    }

    /**
     * Loads all configurator credentials from disk.
     *
     * @return The list of configurators, or an empty list when no data is available.
     */
    public List<Configurator> readConfigurators() {
        Type listType = new TypeToken<List<Configurator>>() {
        }.getType();
        List<Configurator> configurators = readJson(resolve(CONFIGURATORS_FILE), listType, new ArrayList<>());
        return configurators == null ? new ArrayList<>() : configurators;
    }

    /**
     * Persists configurator credentials to disk.
     *
     * @param configurators The configurators to store.
     */
    public void writeConfigurators(List<Configurator> configurators) {
        writeJson(resolve(CONFIGURATORS_FILE), configurators);
    }

    /**
     * Loads all fruitori credentials and personal spaces from disk.
     *
     * @return The list of fruitori, or an empty list when no data is available.
     */
    public List<Participant> readParticipants() {
        Type listType = new TypeToken<List<Participant>>() {
        }.getType();
        List<Participant> participants = readJson(resolve(PARTICIPANTS_FILE), listType, new ArrayList<>());
        return participants == null ? new ArrayList<>() : participants;
    }

    /**
     * Persists fruitori credentials and personal spaces to disk.
     *
     * @param participants The fruitori to store.
     */
    public void writeParticipants(List<Participant> participants) {
        writeJson(resolve(PARTICIPANTS_FILE), participants);
    }

    /**
     * Loads the proposal archive from disk.
     *
     * @return The proposal archive, or an empty archive if no data is available.
     */
    public Archive readArchive() {
        Archive archive = readJson(resolve(PROPOSALS_FILE), Archive.class, new Archive());
        return archive == null ? new Archive() : archive;
    }

    /**
     * Persists the proposal archive to disk.
     *
     * @param archive The archive to store.
     */
    public void writeArchive(Archive archive) {
        writeJson(resolve(PROPOSALS_FILE), archive);
    }

    /**
     * Ensures that the data directory exists before file operations.
     */
    private void ensureDataDir() {
        try {
            Files.createDirectories(dataDir);
        } catch (IOException exception) {
            String message = ERROR_DIR_CREATION_TEMPLATE.formatted(exception.getMessage());
            printError(message);
        }
    }

    /**
     * Resolves a file name inside the data directory.
     *
     * @param filename The file name to resolve.
     * @return The resolved path.
     */
    private Path resolve(String filename) {
        return dataDir.resolve(filename);
    }

    /**
     * Reads a JSON file and deserializes it into the requested type.
     *
     * @param path          The file path to read.
     * @param type          The target deserialization type.
     * @param defaultValue  The fallback value when file is missing, blank, or unreadable.
     * @param <T>           The expected return type.
     * @return The deserialized value, or {@code defaultValue} on read failures.
     */
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
        } catch (IOException exception) {
            String message = ERROR_READ_TEMPLATE.formatted(path, exception.getMessage());
            printError(message);
            return defaultValue;
        }
    }

    /**
     * Serializes and writes an object as JSON to the target path.
     *
     * @param path      The file path to write.
     * @param object    The object to serialize.
     */
    private void writeJson(Path path, Object object) {
        try {
            String json = gson.toJson(object);
            Files.writeString(path, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            String message = ERROR_WRITE_TEMPLATE.formatted(path, exception.getMessage());
            printError(message);
        }
    }

    /**
     * Prints a persistence error in red, bold text.
     *
     * @param message The message to print.
     */
    private void printError(String message) {
        System.err.println(FormatStrings.addFormat(message, AnsiColors.RED, AnsiWeights.BOLD, null));
    }
}
