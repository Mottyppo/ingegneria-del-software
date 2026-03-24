package it.unibs.ingesw.console.format;

/**
 * Enumeration of ANSI colors used to format terminal output.
 */
public enum AnsiColors {
  /** Ansi code for clearing the terminal. */
  CLEAR(clearCode()),
  /** Resets the colors of the terminal. */
  RESET(resetCode()),
  /** Colors the words <i>red</i>. */
  RED(redCode()),
  /** Colors the words <i>green</i>. */
  GREEN(greenCode()),
  /** Colors the words <i>yellow</i>. */
  YELLOW(yellowCode()),
  /** Colors the words <i>blue</i>. */
  BLUE(blueCode());

  private static final String CLEAR_CODE = "\033[H\033[2J";
  private static final String RESET_CODE = "\u001B[0m";
  private static final String RED_CODE = "\u001B[31m";
  private static final String GREEN_CODE = "\u001B[32m";
  private static final String YELLOW_CODE = "\u001B[33m";
  private static final String BLUE_CODE = "\u001B[34m";

  private static String clearCode() {
    return CLEAR_CODE;
  }

  private static String resetCode() {
    return RESET_CODE;
  }

  private static String redCode() {
    return RED_CODE;
  }

  private static String greenCode() {
    return GREEN_CODE;
  }

  private static String yellowCode() {
    return YELLOW_CODE;
  }

  private static String blueCode() {
    return BLUE_CODE;
  }

  private final String ansiCode;

  AnsiColors(String ansiCode) {
    this.ansiCode = ansiCode;
  }

  @Override
  public String toString() {
    return ansiCode;
  }
}
