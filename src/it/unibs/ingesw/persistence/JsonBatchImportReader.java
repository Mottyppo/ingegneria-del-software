package it.unibs.ingesw.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import it.unibs.ingesw.model.Category;
import it.unibs.ingesw.model.Field;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reads batch-import JSON files from arbitrary filesystem locations.
 *
 * <p>The reader is intentionally separate from the domain model: it only
 * deserializes external file contents into lightweight structures that will
 * later be validated and applied by application services.</p>
 */
public class JsonBatchImportReader {
    private static final String EMPTY_PATH_ERROR = "Percorso file mancante.";
    private static final String INVALID_PATH_TEMPLATE = "Percorso file non valido: %s";
    private static final String FILE_NOT_FOUND_TEMPLATE = "File non trovato: %s";
    private static final String FILE_NOT_READABLE_TEMPLATE = "File non leggibile: %s";
    private static final String FILE_EMPTY_TEMPLATE = "File vuoto: %s";
    private static final String FILE_MALFORMED_TEMPLATE = "JSON non valido nel file %s.";
    private static final String FILE_READ_ERROR_TEMPLATE = "Errore nella lettura del file %s: %s";

    private final Gson gson;

    /**
     * Creates a JSON batch reader.
     */
    public JsonBatchImportReader() {
        this.gson = new GsonBuilder().create();
    }

    /**
     * Reads a fields batch file.
     *
     * @param rawPath The user-provided path.
     * @return The parsed result or an error description.
     */
    public ReadResult<FieldsFile> readFieldsFile(String rawPath) {
        return read(rawPath, FieldsFile.class);
    }

    /**
     * Reads a categories batch file.
     *
     * @param rawPath The user-provided path.
     * @return The parsed result or an error description.
     */
    public ReadResult<List<Category>> readCategoriesFile(String rawPath) {
        Type listType = new TypeToken<List<Category>>() {
        }.getType();
        ReadResult<List<Category>> readResult = read(rawPath, listType);
        if (!readResult.isSuccess()) {
            return readResult;
        }

        List<Category> categories = readResult.value() == null ? new ArrayList<>() : readResult.value();
        return ReadResult.success(categories, readResult.sourcePath());
    }

    /**
     * Reads a proposals batch file.
     *
     * @param rawPath The user-provided path.
     * @return The parsed result or an error description.
     */
    public ReadResult<List<ProposalSeed>> readProposalsFile(String rawPath) {
        ReadResult<ProposalsFile> readResult = read(rawPath, ProposalsFile.class);
        if (!readResult.isSuccess()) {
            return ReadResult.failure(readResult.sourcePath(), readResult.errorMessage());
        }

        List<ProposalSeed> proposals = readResult.value() == null || readResult.value().proposals() == null
                ? new ArrayList<>()
                : readResult.value().proposals();
        return ReadResult.success(proposals, readResult.sourcePath());
    }

    private <T> ReadResult<T> read(String rawPath, Type type) {
        String trimmedPath = rawPath == null ? null : rawPath.trim();
        if (trimmedPath == null || trimmedPath.isBlank()) {
            return ReadResult.failure(trimmedPath, EMPTY_PATH_ERROR);
        }

        final Path path;
        try {
            path = Path.of(trimmedPath).toAbsolutePath().normalize();
        } catch (InvalidPathException exception) {
            return ReadResult.failure(trimmedPath, INVALID_PATH_TEMPLATE.formatted(exception.getInput()));
        }

        if (!Files.exists(path)) {
            return ReadResult.failure(path.toString(), FILE_NOT_FOUND_TEMPLATE.formatted(path));
        }
        if (!Files.isReadable(path)) {
            return ReadResult.failure(path.toString(), FILE_NOT_READABLE_TEMPLATE.formatted(path));
        }

        try {
            String json = Files.readString(path);
            if (json == null || json.isBlank()) {
                return ReadResult.failure(path.toString(), FILE_EMPTY_TEMPLATE.formatted(path));
            }

            T value = gson.fromJson(json, type);
            return ReadResult.success(value, path.toString());
        } catch (JsonSyntaxException exception) {
            return ReadResult.failure(path.toString(), FILE_MALFORMED_TEMPLATE.formatted(path));
        } catch (IOException exception) {
            return ReadResult.failure(
                    path.toString(),
                    FILE_READ_ERROR_TEMPLATE.formatted(path, exception.getMessage())
            );
        }
    }

    /**
     * Parsed content of a fields batch file.
     *
     * @param baseFields   The optional base fields section.
     * @param commonFields The optional common fields section.
     */
    public record FieldsFile(List<Field> baseFields, List<Field> commonFields) {
    }

    /**
     * Parsed content of a proposals batch file.
     *
     * @param categoryName The target proposal category.
     * @param fieldValues  The raw proposal values as provided in the JSON file.
     */
    public record ProposalSeed(String categoryName, Map<String, String> fieldValues) {
    }

    /**
     * Generic read outcome.
     *
     * @param value        The parsed value.
     * @param sourcePath   The resolved source path.
     * @param errorMessage The failure description, or {@code null} on success.
     * @param <T>          The value type.
     */
    public record ReadResult<T>(T value, String sourcePath, String errorMessage) {
        public boolean isSuccess() {
            return errorMessage == null;
        }

        public static <T> ReadResult<T> success(T value, String sourcePath) {
            return new ReadResult<>(value, sourcePath, null);
        }

        public static <T> ReadResult<T> failure(String sourcePath, String errorMessage) {
            return new ReadResult<>(null, sourcePath, errorMessage);
        }
    }

    private record ProposalsFile(List<ProposalSeed> proposals) {
    }
}
