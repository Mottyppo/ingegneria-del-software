package it.unibs.ingesw.console.format;

import it.unibs.ingesw.model.DataType;
import it.unibs.ingesw.model.Proposal;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Utility class for formatting values according to Italian locale conventions
 * for command-line output.
 *
 * <p>
 * The class provides helpers for rendering dates, times, booleans and numeric
 * values in a user-friendly format (e.g. {@code dd/MM/yyyy}, {@code HH:mm}, euro currency).
 * It also offers higher-level methods to format values based on a {@link DataType}
 * or directly from a {@link Proposal}.
 * </p>
 *
 * <p>
 * All methods are static and the class is not meant to be instantiated.
 * </p>
 */
public final class FormatValues {
    private static final String UNSUPPORTED_OP_ERR_MSG = String.format("%sQuesta classe non e' istanziabile!%s",
            AnsiColors.RED, AnsiColors.RESET);
    private static final String EMPTY_VALUE = "-";
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATETIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter IT_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter IT_TIME =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DecimalFormat DECIMAL_FORMAT = createDecimalFormatter();

    /**
     * Prevents instantiation of the utility class.
     *
     * @throws UnsupportedOperationException Always thrown when invoked.
     */
    private FormatValues() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(FormatValues.UNSUPPORTED_OP_ERR_MSG);
    }

    /**
     * Formats a raw value according to the specified {@link DataType}.
     *
     * <p>
     * Supported conversions include:
     * <ul>
     *   <li>{@code DATE} → {@code dd/MM/yyyy}</li>
     *   <li>{@code TIME} → {@code HH:mm}</li>
     *   <li>{@code BOOLEAN} → {@code Si}/{@code No}</li>
     *   <li>{@code DECIMAL} → formatted currency with euro symbol</li>
     * </ul>
     * </p>
     *
     * @param type     The data type describing how the value should be formatted.
     * @param rawValue The raw value to format.
     * @return The formatted value, or a fallback representation if parsing fails.
     */
    public static String formatByType(DataType type, String rawValue) {
        if (rawValue == null)
            return EMPTY_VALUE;

        if (type == null)
            return rawValue;

        try {
            return switch (type) {
                case DATE -> formatDate(rawValue);
                case TIME -> formatTime(rawValue);
                case BOOLEAN -> formatBoolean(rawValue);
                case DECIMAL -> formatCurrency(rawValue);
                default -> rawValue;
            };
        } catch (DateTimeParseException | NumberFormatException e) {
            return rawValue;
        }
    }

    /**
     * Formats a field value using metadata from a {@link Proposal}.
     *
     * <p>
     * This method retrieves the {@link DataType} associated with the given field
     * and delegates formatting to {@link #formatByType(DataType, String)}.
     * </p>
     *
     * @param proposal  The proposal containing field metadata.
     * @param fieldName The field name.
     * @param rawValue  The raw value to format.
     * @return The formatted value, or the raw value if no metadata is available.
     */
    public static String formatField(Proposal proposal, String fieldName, String rawValue) {
        if (proposal == null)
            return rawValue;

        DataType type = proposal.getFieldType(fieldName);
        return formatByType(type, rawValue);
    }

    /**
     * Formats an ISO date string ({@code yyyy-MM-dd}) into Italian format.
     *
     * @param isoDate The ISO date string.
     * @return The formatted date ({@code dd/MM/yyyy}).
     * @throws DateTimeParseException If the input is not a valid ISO date.
     */
    public static String formatDate(String isoDate) {
        LocalDate date = LocalDate.parse(isoDate, ISO_DATE);
        return date.format(IT_DATE);
    }

    /**
     * Formats a time string into {@code HH:mm}.
     *
     * @param time The time string.
     * @return The formatted time.
     * @throws DateTimeParseException If the input is not a valid time.
     */
    public static String formatTime(String time) {
        LocalTime parsed = LocalTime.parse(time, IT_TIME);
        return parsed.format(IT_TIME);
    }

    /**
     * Formats an ISO datetime string into Italian date and time representation.
     *
     * <p>
     * Output format: {@code dd/MM/yyyy HH:mm}
     * </p>
     *
     * @param isoDateTime The ISO datetime string.
     * @return The formatted datetime, or {@code "-"} if null or blank.
     * @throws DateTimeParseException If the input is not a valid ISO datetime.
     */
    public static String formatDateTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isBlank())
            return EMPTY_VALUE;

        LocalDateTime dt = LocalDateTime.parse(isoDateTime, ISO_DATETIME);

        return dt.toLocalDate().format(IT_DATE) + " " +
                dt.toLocalTime().format(IT_TIME);
    }

    /**
     * Formats a boolean string into Italian representation.
     *
     * @param value The boolean string.
     * @return {@code "Si"} if true, {@code "No"} otherwise.
     */
    public static String formatBoolean(String value) {
        return Boolean.parseBoolean(value) ? "Si" : "No";
    }

    /**
     * Formats a numeric string into euro currency representation.
     *
     * <p>
     * The value is normalized (comma or dot) and formatted using Italian locale.
     * </p>
     *
     * @param value The numeric string.
     * @return The formatted currency string (e.g. {@code 1.234,50 €}).
     * @throws NumberFormatException If the input is not a valid number.
     */
    public static String formatCurrency(String value) {
        double decimal = Double.parseDouble(value.replace(',', '.'));
        return DECIMAL_FORMAT.format(decimal) + " €";
    }

    /**
     * Creates the decimal formatter configured for Italian locale.
     *
     * @return A configured {@link DecimalFormat} instance.
     */
    private static DecimalFormat createDecimalFormatter() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ITALY);
        DecimalFormat format = new DecimalFormat("0.00", symbols);
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format;
    }
}