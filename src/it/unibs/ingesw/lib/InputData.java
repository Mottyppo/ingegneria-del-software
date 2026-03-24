package it.unibs.ingesw.lib;

import java.util.Arrays;
import java.util.Map;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.stream.IntStream;

/**
 * The class <strong>InputData</strong> can read a specific data type inserted
 * in input by the
 * user.It also allows the possibility to make controls on the data inserted.
 */
public final class InputData {
  private static final Scanner reader = createScanner();
  // @formatter:off
  private static final Map<String, String> ERRORS = Map.ofEntries(
      Map.entry("red", FormatStrings.addFormat("Errore!\n", AnsiColors.RED, AnsiWeights.BOLD, null)),
      Map.entry("constructor", "Questa classe non e' istanziabile!"),
      Map.entry(
          "alphanumeric_characters",
          "Sono consentiti solo " + FormatStrings.addFormat("caratteri", null, AnsiWeights.BOLD, null) + " alfanumerici.\n"
      ),
      Map.entry(
          "empty_string",
          "Sono stati inseriti solo " + FormatStrings.addFormat("spazi vuoti", null, AnsiWeights.BOLD, null) + " o nessun carattere "
              + FormatStrings.addFormat("alfanumerico", null, AnsiWeights.BOLD, null) + ".\n"
      ),
      Map.entry("allowed_characters", "Gli unici caratteri consentiti sono: %s\n"),
      Map.entry(
          "integer_format",
          "Il dato inserito e' in un formato " + FormatStrings.addFormat("errato", null, AnsiWeights.BOLD, null)
              + ". E' richiesto un numero " + FormatStrings.addFormat("intero", null, null, AnsiDecorations.UNDERLINE)
              + ".\n"
      ),
      Map.entry(
          "double_format",
          "Il dato inserito e' in un formato " + FormatStrings.addFormat("errato", null, AnsiWeights.BOLD, null)
              + ". E' richiesto un numero " + FormatStrings.addFormat("double", null, null, AnsiDecorations.UNDERLINE)
              + ".\n"
      ),
      Map.entry("minimum_int", "E' richiesto un valore maggiore o uguale a %d.\n"),
      Map.entry("maximum_int", "E' richiesto un valore minore o uguale a %d.\n"),
      Map.entry("minimum_double", "E' richiesto un valore maggiore o uguale a %.2f.\n"),
      Map.entry("maximum_double", "E' richiesto un valore minore o uguale a %.2f.\n")
  );
  // @formatter:on

  private static final String ALPHANUMERIC_CHARACTERS = "abcdefghijklmnopqrstuvwxyzZ0123456789 ";
  private static final char YES_ANSWER = 'y';
  private static final char NO_ANSWER = 'n';

  private static final double EPSILON = 1E-5;

  private InputData() {
    throw new UnsupportedOperationException(InputData.ERRORS.get("constructor"));
  }

  /**
   * Methods that creates a <code>Scanner</code> object.
   *
   * @return A <code>Scanner</code> object.
   */
  private static Scanner createScanner() {
    return new Scanner(System.in).useDelimiter("\\R+");
  }

  /**
   * Verifies if the given message has only alphanumeric characters.
   *
   * @param message The message to verify.
   * 
   * @return A <code>boolean</code> representing if the message is alphanumeric or
   *         not.
   */
  private static boolean hasAlphanumericCharacters(String message) {
    for (char currentChar : message.toCharArray()) {
      if (ALPHANUMERIC_CHARACTERS.indexOf(currentChar) == -1)
        return false;
    }

    return true;
  }

  /**
   * Flushes the scanner, emptying the input buffer.
   */
  private static void flushReader() {
    if (reader.hasNextLine())
      reader.nextLine();
  }

  /**
   * Prints <code>message</code> in the terminal and reads the text inserted by
   * the user. If it isn't
   * a <code>String</code> an error message is printed. It's also possible to
   * select if the inserted
   * text needs to be alphanumeric or not.
   *
   * @param message      The message to print.
   * @param alphanumeric If the input needs to be alphanumeric or not.
   * 
   * @return A <code>String</code> representing the user input.
   */
  public static String readString(String message, boolean alphanumeric) {
    boolean isAlphanumeric;
    String read;

    do {
      System.out.print(message);

      read = reader.next().trim();

      isAlphanumeric = !alphanumeric || InputData.hasAlphanumericCharacters(read);
      if (!isAlphanumeric)
        System.out.println(InputData.ERRORS.get("red") + InputData.ERRORS.get("alphanumeric_characters"));
    } while (!isAlphanumeric);

    return read;
  }

  /**
   * Prints <code>message</code> in the terminal and reads the text inserted by
   * the user, given that
   * it isn't empty. If it isn't a <code>String</code> an error message is
   * printed. It's also possible
   * to select if the inserted text needs to be alphanumeric or not.
   *
   * @param message      The message to print.
   * @param alphanumeric If the input needs to be alphanumeric or not.
   * 
   * @return A <code>String</code> representing the user input.
   */
  public static String readNonEmptyString(String message, boolean alphanumeric) {
    boolean isEmpty;
    String read;

    do {
      read = InputData.readString(message, alphanumeric);

      isEmpty = read.isBlank();
      if (isEmpty)
        System.out.println(InputData.ERRORS.get("red") + InputData.ERRORS.get("empty_string"));
    } while (isEmpty);

    return read;
  }

  /**
   * Prints <code>message</code> in the terminal and reads the text inserted by
   * the user. It will take
   * the first <code>char</code> in it and verify if it is in the
   * <code>allowed</code> characters, if
   * not, an error message will be printed.
   *
   * @param message The message to print.
   * @param allowed All the allowed characters.
   * 
   * @return A <code>char</code> representing the character tha was read.
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
        // @formatter:off
        System.out.println(
          InputData.ERRORS.get("red") + 
          InputData.ERRORS.get("allowed_characters").formatted(Arrays.toString(allowed.toCharArray()))
        );
        // @formatter:on
    } while (!isAllowed);

    return readChar;
  }

  /**
   * Prints <code>message</code> in the terminal and reads the text inserted by
   * the user. It will take
   * the first <code>char</code> in it or return empty if the user inserted
   * nothing.
   *
   * @param message The message to print.
   * 
   * @return A <code>char</code> representing the character tha was read or empty.
   */
  public static char readChar(String message) {
    String read = InputData.readString(message, false);

    return read.isBlank() ? '\0' : read.charAt(0);
  }

  /**
   * Prints <code>question</code> in the terminal with the string "? [Y/n] "
   * added. If the user
   * answers with 'y', 'Y' or leaves blank the method will return
   * <code>true</code>,
   * <code>false</code> otherwise.
   *
   * @param question The question to print.
   * 
   * @return A <code>boolean</code> representing the affirmative or negative
   *         answer of the user.
   */
  public static boolean readYesOrNo(String question) {
    question = "%s? [%c/%c] ".formatted(question, Character.toUpperCase(InputData.YES_ANSWER), InputData.NO_ANSWER);

    char readChar = InputData.readChar(question);

    char[] trueAnswers = new char[] {
        InputData.YES_ANSWER, Character.toUpperCase(InputData.YES_ANSWER), '\0'
    };

    return IntStream.range(0, trueAnswers.length).anyMatch(i -> trueAnswers[i] == readChar);
  }

  /**
   * Prints <code>message</code> in the terminal and reads the text inserted by
   * the user. It will
   * print an error message if the text inserted isn't an integer.
   *
   * @return An <code>int</code> representing the integer that was read.
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
        System.out.println(InputData.ERRORS.get("red") + InputData.ERRORS.get("integer_format"));

        isInteger = false;
      } finally {
        InputData.flushReader();
      }
    } while (!isInteger);

    return read;
  }

  /**
   * Prints <code>message</code> in the terminal and reads the text inserted by
   * the user. It will
   * print an error message if the text inserted isn't an integer or if the
   * integer inserted is less
   * than <code>min</code>.
   *
   * @param message The message to print.
   * @param min     The minimum value to read.
   * 
   * @return An <code>int</code> representing the integer that was read.
   */
  public static int readIntegerWithMinimum(String message, int min) {
    boolean isBelowMin;
    int read;

    do {
      read = InputData.readInteger(message);

      isBelowMin = read < min;
      if (isBelowMin)
        // @formatter:off
        System.out.println(
          InputData.ERRORS.get("red") + 
          InputData.ERRORS.get("minimum_int").formatted(min)
        );
        // @formatter:on
    } while (isBelowMin);

    return read;
  }

  /**
   * Prints <code>message</code> in the terminal and reads the text inserted by
   * the user. It will
   * print an error message if the text inserted isn't an integer or if the
   * integer inserted is
   * greater than <code>max</code>.
   *
   * @param message The message to print.
   * @param max     The maximum value to read.
   * 
   * @return An <code>int</code> representing the integer that was read.
   */
  public static int readIntegerWithMaximum(String message, int max) {
    boolean isAboveMax;
    int read;

    do {
      read = InputData.readInteger(message);

      isAboveMax = read > max;
      if (isAboveMax)
        // @formatter:off
        System.out.println(
          InputData.ERRORS.get("red") + 
          InputData.ERRORS.get("maximum_int").formatted(max)
        );
        // @formatter:on
    } while (isAboveMax);

    return read;
  }

  /**
   * Prints <code>message</code> in the terminal and reads the text inserted by
   * the user. It will
   * print an error message if the text inserted isn't an integer or if the
   * integer inserted isn't
   * between or equal than <code>min</code> and <code>max</code>.
   *
   * @param message The message to print.
   * @param min     The minimum value to read.
   * @param max     The maximum value to read.
   * @return An <code>int</code> representing the integer that was read.
   */
  public static int readIntegerBetween(String message, int min, int max) {
    boolean isBelowMin;
    boolean isAboveMax;
    int read;

    do {
      read = InputData.readInteger(message);

      isBelowMin = read < min;
      if (isBelowMin)
        // @formatter:off
        System.out.println(
          InputData.ERRORS.get("red") + 
          InputData.ERRORS.get("minimum_int").formatted(min)
        );
        // @formatter:on

      isAboveMax = read > max;
      if (isAboveMax)
        // @formatter:off
        System.out.println(
          InputData.ERRORS.get("red") + 
          InputData.ERRORS.get("maximum_int").formatted(max)
        );
        // @formatter:on
    } while (isBelowMin || isAboveMax);

    return read;
  }

  /**
   * Prints <code>message</code> in the terminal and reads the text inserted by
   * the user. It will
   * print an error message if the text inserted isn't a double.
   *
   * @param message The message to print.
   * @return A <code>double</code> representing the double that was read.
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
        System.out.println(InputData.ERRORS.get("red") + InputData.ERRORS.get("double_format"));

        isDouble = false;
      } finally {
        InputData.flushReader();
      }
    } while (!isDouble);

    return read;
  }

  /**
   * Prints <code>message</code> in the terminal and reads the text inserted by
   * the user. It will
   * print an error message if the text inserted isn't a double or if the double
   * inserted isn't
   * greater equal than <code>min</code>.
   *
   * @param message The message to print.
   * @param min     The minimum value to read.
   * @return A <code>double</code> representing the double that was read.
   */
  public static double readDoubleWithMinimum(String message, double min) {
    boolean isBelowMin;
    double read;

    do {
      read = InputData.readDouble(message);

      isBelowMin = (min - read) > InputData.EPSILON;
      if (isBelowMin)
        // @formatter:off
        System.out.println(
          InputData.ERRORS.get("red") + 
          InputData.ERRORS.get("minimum_double").formatted(min)
        );
        // @formatter:on
    } while (isBelowMin);

    return read;
  }

  /**
   * Prints <code>message</code> in the terminal and reads the text inserted by
   * the user. It will
   * print an error message if the text inserted isn't a double or if the double
   * inserted isn't less
   * equal than <code>max</code>.
   *
   * @param message The message to print.
   * @param max     The maximum value to read.
   * @return An <code>double</code> representing the double that was read.
   */
  public static double readDoubleWithMaximum(String message, double max) {
    boolean isAboveMax;
    double read;

    do {
      read = InputData.readDouble(message);

      isAboveMax = (read - max) > InputData.EPSILON;
      if (isAboveMax)
        // @formatter:off
        System.out.println(
          InputData.ERRORS.get("red") + 
          InputData.ERRORS.get("maximum_double").formatted(max)
        );
        // @formatter:on
    } while (isAboveMax);

    return read;
  }

  /**
   * Prints <code>message</code> in the terminal and reads the text inserted by
   * the user. It will
   * print an error message if the text inserted isn't a double or if the double
   * inserted isn't
   * between or equal than <code>min</code> and <code>max</code>.
   *
   * @param message The message to print.
   * @param min     The minimum value to read.
   * @param max     The maximum value to read.
   * @return An <code>double</code> representing the double that was read.
   */
  public static double readDoubleBetween(String message, double min, double max) {
    boolean isBelowMin;
    boolean isAboveMax;
    double read;

    do {
      read = InputData.readDouble(message);

      isBelowMin = (min - read) > InputData.EPSILON;
      if (isBelowMin)
        // @formatter:off
        System.out.println(
          InputData.ERRORS.get("red") + 
          InputData.ERRORS.get("minimum_double").formatted(min)
        );
        // @formatter:on

      isAboveMax = (read - max) > InputData.EPSILON;
      if (isAboveMax)
        // @formatter:off
        System.out.println(
          InputData.ERRORS.get("red") + 
          InputData.ERRORS.get("maximum_double").formatted(max)
        );
        // @formatter:on
    } while (isBelowMin || isAboveMax);

    return read;
  }
}