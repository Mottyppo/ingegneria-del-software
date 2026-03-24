package it.unibs.ingesw.console.input;

import it.unibs.ingesw.console.format.AnsiColors;
import it.unibs.ingesw.console.format.AnsiDecorations;
import it.unibs.ingesw.console.format.AnsiWeights;
import it.unibs.ingesw.console.format.FormatStrings;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.IntStream;

/**
 * Utility class for reading and validating user input from the console.
 *
 * <p>
 * Each reader method keeps prompting until the entered value satisfies the
 * required constraints.
 * </p>
 */
public final class InputData {
  private static final String ERROR_KEY_RED = "red";
  private static final String ERROR_KEY_CONSTRUCTOR = "constructor";
  private static final String ERROR_KEY_ALPHANUMERIC = "alphanumeric_characters";
  private static final String ERROR_KEY_EMPTY = "empty_string";
  private static final String ERROR_KEY_ALLOWED = "allowed_characters";
  private static final String ERROR_KEY_INTEGER_FORMAT = "integer_format";
  private static final String ERROR_KEY_DOUBLE_FORMAT = "double_format";
  private static final String ERROR_KEY_MIN_INT = "minimum_int";
  private static final String ERROR_KEY_MAX_INT = "maximum_int";
  private static final String ERROR_KEY_MIN_DOUBLE = "minimum_double";
  private static final String ERROR_KEY_MAX_DOUBLE = "maximum_double";
  private static final String ERROR_PREFIX = FormatStrings.addFormat("Errore!\n", AnsiColors.RED, AnsiWeights.BOLD, null);
  private static final String CONSTRUCTOR_MESSAGE = "Questa classe non e' istanziabile!";
  private static final String ALPHANUMERIC_PART = FormatStrings.addFormat("caratteri", null, AnsiWeights.BOLD, null);
  private static final String EMPTY_SPACES_PART = FormatStrings.addFormat("spazi vuoti", null, AnsiWeights.BOLD, null);
  private static final String ALPHANUMERIC_WORD_PART = FormatStrings.addFormat("alfanumerico", null, AnsiWeights.BOLD, null);
  private static final String WRONG_FORMAT_PART = FormatStrings.addFormat("errato", null, AnsiWeights.BOLD, null);
  private static final String INTEGER_WORD_PART = FormatStrings.addFormat("intero", null, null, AnsiDecorations.UNDERLINE);
  private static final String DOUBLE_WORD_PART = FormatStrings.addFormat("double", null, null, AnsiDecorations.UNDERLINE);
  private static final String ALPHANUMERIC_CHARACTERS_MESSAGE = "Sono consentiti solo " + ALPHANUMERIC_PART + " alfanumerici.\n";
  private static final String EMPTY_STRING_MESSAGE = "Sono stati inseriti solo " + EMPTY_SPACES_PART + " o nessun carattere "
      + ALPHANUMERIC_WORD_PART + ".\n";
  private static final String ALLOWED_CHARACTERS_MESSAGE = "Gli unici caratteri consentiti sono: %s\n";
  private static final String INTEGER_FORMAT_MESSAGE = "Il dato inserito e' in un formato " + WRONG_FORMAT_PART
      + ". E' richiesto un numero " + INTEGER_WORD_PART + ".\n";
  private static final String DOUBLE_FORMAT_MESSAGE = "Il dato inserito e' in un formato " + WRONG_FORMAT_PART
      + ". E' richiesto un numero " + DOUBLE_WORD_PART + ".\n";
  private static final String MINIMUM_INTEGER_MESSAGE = "E' richiesto un valore maggiore o uguale a %d.\n";
  private static final String MAXIMUM_INTEGER_MESSAGE = "E' richiesto un valore minore o uguale a %d.\n";
  private static final String MINIMUM_DOUBLE_MESSAGE = "E' richiesto un valore maggiore o uguale a %.2f.\n";
  private static final String MAXIMUM_DOUBLE_MESSAGE = "E' richiesto un valore minore o uguale a %.2f.\n";
  private static final String YES_NO_TEMPLATE = "%s? [%c/%c] ";
  private static final String ALPHANUMERIC_CHARACTERS = "abcdefghijklmnopqrstuvwxyzZ0123456789 ";
  private static final char YES_ANSWER = 'y';
  private static final char NO_ANSWER = 'n';
  private static final double EPSILON = 1E-5;
  private static final Scanner reader = createScanner();
  private static final Map<String, String> ERRORS = Map.ofEntries(
      Map.entry(ERROR_KEY_RED, ERROR_PREFIX),
      Map.entry(ERROR_KEY_CONSTRUCTOR, CONSTRUCTOR_MESSAGE),
      Map.entry(ERROR_KEY_ALPHANUMERIC, ALPHANUMERIC_CHARACTERS_MESSAGE),
      Map.entry(ERROR_KEY_EMPTY, EMPTY_STRING_MESSAGE),
      Map.entry(ERROR_KEY_ALLOWED, ALLOWED_CHARACTERS_MESSAGE),
      Map.entry(ERROR_KEY_INTEGER_FORMAT, INTEGER_FORMAT_MESSAGE),
      Map.entry(ERROR_KEY_DOUBLE_FORMAT, DOUBLE_FORMAT_MESSAGE),
      Map.entry(ERROR_KEY_MIN_INT, MINIMUM_INTEGER_MESSAGE),
      Map.entry(ERROR_KEY_MAX_INT, MAXIMUM_INTEGER_MESSAGE),
      Map.entry(ERROR_KEY_MIN_DOUBLE, MINIMUM_DOUBLE_MESSAGE),
      Map.entry(ERROR_KEY_MAX_DOUBLE, MAXIMUM_DOUBLE_MESSAGE)
  );

  private InputData() {
    throw new UnsupportedOperationException(InputData.ERRORS.get(ERROR_KEY_CONSTRUCTOR));
  }

  /**
   * Reads a string from the console and validates it according to the selected
   * constraints.
   *
   * @param message      The prompt to display.
   * @param alphanumeric Whether the input must contain only alphanumeric
   *                     characters and spaces.
   * @return The entered string.
   */
  public static String readString(String message, boolean alphanumeric) {
    boolean isAlphanumeric;
    String read;

    do {
      System.out.print(message);

      read = reader.next().trim();

      isAlphanumeric = !alphanumeric || InputData.hasAlphanumericCharacters(read);
      if (!isAlphanumeric)
        System.out.println(InputData.ERRORS.get(ERROR_KEY_RED) + InputData.ERRORS.get(ERROR_KEY_ALPHANUMERIC));
    } while (!isAlphanumeric);

    return read;
  }

  /**
   * Reads a non-empty string from the console.
   *
   * @param message      The prompt to display.
   * @param alphanumeric Whether the input must be alphanumeric.
   * @return The entered string.
   */
  public static String readNonEmptyString(String message, boolean alphanumeric) {
    boolean isEmpty;
    String read;

    do {
      read = InputData.readString(message, alphanumeric);

      isEmpty = read.isBlank();
      if (isEmpty)
        System.out.println(InputData.ERRORS.get(ERROR_KEY_RED) + InputData.ERRORS.get(ERROR_KEY_EMPTY));
    } while (isEmpty);

    return read;
  }

  /**
   * Reads the first character of a non-empty console input and validates it
   * against the allowed characters.
   *
   * @param message The prompt to display.
   * @param allowed The set of allowed characters.
   * @return The validated character.
   */
  public static char readChar(String message, String allowed) {
    boolean isAllowed;
    String read;
    char readChar;

    do {
      read = InputData.readNonEmptyString(message, false);
      readChar = read.charAt(0);

      isAllowed = allowed.indexOf(readChar) != -1;
      if (!isAllowed)
        System.out.println(InputData.ERRORS.get(ERROR_KEY_RED)
            + InputData.ERRORS.get(ERROR_KEY_ALLOWED).formatted(Arrays.toString(allowed.toCharArray())));
    } while (!isAllowed);

    return readChar;
  }

  /**
   * Reads the first character of an optional console input.
   *
   * @param message The prompt to display.
   * @return The first entered character, or {@code '\0'} if the input is empty.
   */
  public static char readChar(String message) {
    String read = InputData.readString(message, false);

    return read.isBlank() ? '\0' : read.charAt(0);
  }

  /**
   * Reads a yes-or-no answer from the console.
   *
   * @param question The question to ask.
   * @return {@code true} for yes or blank input, {@code false} otherwise.
   */
  public static boolean readYesOrNo(String question) {
    question = InputData.YES_NO_TEMPLATE.formatted(question, Character.toUpperCase(InputData.YES_ANSWER), InputData.NO_ANSWER);

    char readChar = InputData.readChar(question);

    char[] trueAnswers = new char[] {
        InputData.YES_ANSWER, Character.toUpperCase(InputData.YES_ANSWER), '\0'
    };

    return IntStream.range(0, trueAnswers.length).anyMatch(i -> trueAnswers[i] == readChar);
  }

  /**
   * Reads an integer from the console.
   *
   * @param message The prompt to display.
   * @return The entered integer.
   */
  public static int readInteger(String message) {
    boolean isInteger;
    int read = 0;

    do {
      try {
        System.out.print(message);

        read = reader.nextInt();

        isInteger = true;
      } catch (InputMismatchException e) {
        System.out.println(InputData.ERRORS.get(ERROR_KEY_RED) + InputData.ERRORS.get(ERROR_KEY_INTEGER_FORMAT));

        isInteger = false;
      } finally {
        InputData.flushReader();
      }
    } while (!isInteger);

    return read;
  }

  /**
   * Reads an integer that is greater than or equal to the provided minimum.
   *
   * @param message The prompt to display.
   * @param min     The minimum accepted value.
   * @return The entered integer.
   */
  public static int readIntegerWithMinimum(String message, int min) {
    boolean isBelowMin;
    int read;

    do {
      read = InputData.readInteger(message);

      isBelowMin = read < min;
      if (isBelowMin)
        System.out.println(InputData.ERRORS.get(ERROR_KEY_RED) + InputData.ERRORS.get(ERROR_KEY_MIN_INT).formatted(min));
    } while (isBelowMin);

    return read;
  }

  /**
   * Reads an integer that is less than or equal to the provided maximum.
   *
   * @param message The prompt to display.
   * @param max     The maximum accepted value.
   * @return The entered integer.
   */
  public static int readIntegerWithMaximum(String message, int max) {
    boolean isAboveMax;
    int read;

    do {
      read = InputData.readInteger(message);

      isAboveMax = read > max;
      if (isAboveMax)
        System.out.println(InputData.ERRORS.get(ERROR_KEY_RED) + InputData.ERRORS.get(ERROR_KEY_MAX_INT).formatted(max));
    } while (isAboveMax);

    return read;
  }

  /**
   * Reads an integer within the provided inclusive range.
   *
   * @param message The prompt to display.
   * @param min     The minimum accepted value.
   * @param max     The maximum accepted value.
   * @return The entered integer.
   */
  public static int readIntegerBetween(String message, int min, int max) {
    boolean isBelowMin;
    boolean isAboveMax;
    int read;

    do {
      read = InputData.readInteger(message);

      isBelowMin = read < min;
      if (isBelowMin)
        System.out.println(InputData.ERRORS.get(ERROR_KEY_RED) + InputData.ERRORS.get(ERROR_KEY_MIN_INT).formatted(min));

      isAboveMax = read > max;
      if (isAboveMax)
        System.out.println(InputData.ERRORS.get(ERROR_KEY_RED) + InputData.ERRORS.get(ERROR_KEY_MAX_INT).formatted(max));
    } while (isBelowMin || isAboveMax);

    return read;
  }

  /**
   * Reads a double from the console.
   *
   * @param message The prompt to display.
   * @return The entered double.
   */
  public static double readDouble(String message) {
    boolean isDouble;
    double read = Double.NaN;

    do {
      try {
        System.out.print(message);

        read = reader.nextDouble();

        isDouble = true;
      } catch (InputMismatchException e) {
        System.out.println(InputData.ERRORS.get(ERROR_KEY_RED) + InputData.ERRORS.get(ERROR_KEY_DOUBLE_FORMAT));

        isDouble = false;
      } finally {
        InputData.flushReader();
      }
    } while (!isDouble);

    return read;
  }

  /**
   * Reads a double that is greater than or equal to the provided minimum.
   *
   * @param message The prompt to display.
   * @param min     The minimum accepted value.
   * @return The entered double.
   */
  public static double readDoubleWithMinimum(String message, double min) {
    boolean isBelowMin;
    double read;

    do {
      read = InputData.readDouble(message);

      isBelowMin = (min - read) > InputData.EPSILON;
      if (isBelowMin)
        System.out.println(InputData.ERRORS.get(ERROR_KEY_RED) + InputData.ERRORS.get(ERROR_KEY_MIN_DOUBLE).formatted(min));
    } while (isBelowMin);

    return read;
  }

  /**
   * Reads a double that is less than or equal to the provided maximum.
   *
   * @param message The prompt to display.
   * @param max     The maximum accepted value.
   * @return The entered double.
   */
  public static double readDoubleWithMaximum(String message, double max) {
    boolean isAboveMax;
    double read;

    do {
      read = InputData.readDouble(message);

      isAboveMax = (read - max) > InputData.EPSILON;
      if (isAboveMax)
        System.out.println(InputData.ERRORS.get(ERROR_KEY_RED) + InputData.ERRORS.get(ERROR_KEY_MAX_DOUBLE).formatted(max));
    } while (isAboveMax);

    return read;
  }

  /**
   * Reads a double within the provided inclusive range.
   *
   * @param message The prompt to display.
   * @param min     The minimum accepted value.
   * @param max     The maximum accepted value.
   * @return The entered double.
   */
  public static double readDoubleBetween(String message, double min, double max) {
    boolean isBelowMin;
    boolean isAboveMax;
    double read;

    do {
      read = InputData.readDouble(message);

      isBelowMin = (min - read) > InputData.EPSILON;
      if (isBelowMin)
        System.out.println(InputData.ERRORS.get(ERROR_KEY_RED) + InputData.ERRORS.get(ERROR_KEY_MIN_DOUBLE).formatted(min));

      isAboveMax = (read - max) > InputData.EPSILON;
      if (isAboveMax)
        System.out.println(InputData.ERRORS.get(ERROR_KEY_RED) + InputData.ERRORS.get(ERROR_KEY_MAX_DOUBLE).formatted(max));
    } while (isBelowMin || isAboveMax);

    return read;
  }

  /**
   * Creates the scanner used to read console input.
   *
   * @return The configured scanner.
   */
  private static Scanner createScanner() {
    return new Scanner(System.in).useDelimiter("\\R+");
  }

  /**
   * Checks whether the provided string contains only allowed characters.
   *
   * @param message The string to validate.
   * @return {@code true} if the string is valid, {@code false} otherwise.
   */
  private static boolean hasAlphanumericCharacters(String message) {
    for (char currentChar : message.toCharArray()) {
      if (ALPHANUMERIC_CHARACTERS.indexOf(currentChar) == -1)
        return false;
    }

    return true;
  }

  /**
   * Empties the scanner buffer after failed numeric reads.
   */
  private static void flushReader() {
    if (reader.hasNextLine())
      reader.nextLine();
  }
}
