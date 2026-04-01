package it.unibs.ingesw.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Shared JSON persistence utilities reused by the concrete repositories.
 *
 * <p>The support class centralizes data-directory resolution, Gson setup, and
 * resilient read/write helpers so that each repository can focus only on the
 * aggregate it manages.</p>
 */
abstract class JsonRepositorySupport {
    private static final String DEFAULT_DATA_DIR = "data";
    private static final String DATA_DIR_PROPERTY = "ingesw.data.dir";
    private static final String ERROR_DIR_CREATION_TEMPLATE = "Impossibile creare la cartella dati: %s";
    private static final String ERROR_WRITE_TEMPLATE = "Errore nella scrittura del file %s: %s";

    private final Gson gson;
    private final Path dataDir;

    /**
     * Creates the shared JSON repository support and ensures the data directory exists.
     */
    protected JsonRepositorySupport() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataDir = Paths.get(System.getProperty(DATA_DIR_PROPERTY, DEFAULT_DATA_DIR));
        ensureDataDir();
    }

    /**
     * Resolves a file path inside the configured data directory.
     *
     * @param filename The target file name.
     * @return The resolved file path.
     */
    protected Path resolve(String filename) {
        return dataDir.resolve(filename);
    }

    /**
     * Reads and deserializes JSON content from the given path.
     *
     * @param path          The file to read.
     * @param type          The expected target type.
     * @param defaultValue  The fallback value for missing or unreadable files.
     * @param <T>           The resulting value type.
     * @return The deserialized value, or {@code defaultValue} when unavailable.
     */
    protected <T> T readJson(Path path, Type type, T defaultValue) {
        if (!Files.exists(path)) {
            return defaultValue;
        }

        try {
            String json = Files.readString(path);
            if (json == null || json.isBlank()) {
                return defaultValue;
            }

            T value = gson.fromJson(json, type);
            return value == null ? defaultValue : value;
        } catch (IOException exception) {
            return defaultValue;
        }
    }

    /**
     * Serializes and writes JSON content to the given path.
     *
     * @param path   The destination file.
     * @param object The object to serialize.
     */
    protected void writeJson(Path path, Object object) {
        try {
            String json = gson.toJson(object);
            Files.writeString(path, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException(ERROR_WRITE_TEMPLATE.formatted(path, exception.getMessage()), exception);
        }
    }

    /**
     * Ensures that the configured data directory exists.
     */
    private void ensureDataDir() {
        try {
            Files.createDirectories(dataDir);
        } catch (IOException exception) {
            throw new IllegalStateException(ERROR_DIR_CREATION_TEMPLATE.formatted(exception.getMessage()), exception);
        }
    }
}
