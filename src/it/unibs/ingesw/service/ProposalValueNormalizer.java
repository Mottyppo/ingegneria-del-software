package it.unibs.ingesw.service;

import it.unibs.ingesw.model.DataType;
import it.unibs.ingesw.model.Field;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Normalizes raw proposal values coming from the UI.
 *
 * <p>The normalizer converts user-entered strings into the canonical storage
 * representation used by the domain model and persistence layer.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Validates mandatory fields.</li>
 *   <li>Normalizes dates to ISO format.</li>
 *   <li>Normalizes times, decimals, and booleans.</li>
 *   <li>Rejects structurally invalid user input.</li>
 * </ul>
 */
public class ProposalValueNormalizer {
    private static final DateTimeFormatter USER_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter USER_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    /**
     * Normalizes raw field values according to the expected proposal fields.
     *
     * @param fields    The fields that compose the proposal template.
     * @param rawValues The raw values coming from the UI.
     * @return A normalized map, or {@code null} if validation fails.
     */
    public Map<String, String> normalizeAndValidateValues(List<Field> fields, Map<String, String> rawValues) {
        Map<String, String> normalized = new LinkedHashMap<>();
        for (Field field : fields) {
            String fieldName = field.getName();
            String value = rawValues.get(fieldName);
            if (value != null) {
                value = value.trim();
            }

            if (value == null || value.isBlank()) {
                if (field.isMandatory()) {
                    return null;
                }
                continue;
            }

            String canonical = normalizeValue(value, field.getDataType());
            if (canonical == null) {
                return null;
            }
            normalized.put(fieldName, canonical);
        }
        return normalized;
    }

    /**
     * Extracts the field data types associated with a normalized value set.
     *
     * @param fields The template fields.
     * @param values The normalized values.
     * @return A map of field names to data types.
     */
    public Map<String, DataType> extractFieldTypes(List<Field> fields, Map<String, String> values) {
        Map<String, DataType> types = new LinkedHashMap<>();
        for (Field field : fields) {
            if (values.containsKey(field.getName())) {
                types.put(field.getName(), field.getDataType());
            }
        }
        return types;
    }

    /**
     * Converts a raw user value to its canonical storage form.
     *
     * @param rawValue The raw user value.
     * @param dataType The expected data type.
     * @return The canonical value, or {@code null} if conversion fails.
     */
    private String normalizeValue(String rawValue, DataType dataType) {
        try {
            return switch (dataType) {
                case STRING -> rawValue;
                case INTEGER -> Integer.toString(Integer.parseInt(rawValue));
                case DECIMAL -> Double.toString(Double.parseDouble(rawValue));
                case DATE -> LocalDate.parse(rawValue, USER_DATE_FORMATTER).format(DateTimeFormatter.ISO_LOCAL_DATE);
                case TIME -> LocalTime.parse(rawValue, USER_TIME_FORMATTER).format(USER_TIME_FORMATTER);
                case BOOLEAN -> parseBoolean(rawValue);
            };
        } catch (NumberFormatException | DateTimeParseException exception) {
            return null;
        }
    }

    /**
     * Normalizes a boolean-like user input.
     *
     * @param value The raw user value.
     * @return {@code "true"} or {@code "false"}, or {@code null} if the input is invalid.
     */
    private String parseBoolean(String value) {
        String normalized = value.trim().toLowerCase();
        if (normalized.equals("true") || normalized.equals("si") || normalized.equals("s")
                || normalized.equals("yes") || normalized.equals("y")) {
            return Boolean.TRUE.toString();
        }
        if (normalized.equals("false") || normalized.equals("no") || normalized.equals("n")) {
            return Boolean.FALSE.toString();
        }
        return null;
    }
}
