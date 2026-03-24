package it.unibs.ingesw.console.format;

/**
 * Enumeration of ANSI font weights used in terminal output.
 */
public enum AnsiWeights {
  /** Formats the words in a <i>bold</i> weight. */
  BOLD(boldCode()),
  /** Formats the words in an <i>italic</i> weight. */
  ITALIC(italicCode());

  private static final String BOLD_CODE = "\u001B[1m";
  private static final String ITALIC_CODE = "\u001B[3m";

  private static String boldCode() {
    return BOLD_CODE;
  }

  private static String italicCode() {
    return ITALIC_CODE;
  }

  private final String ansiCode;

  AnsiWeights(String ansiCode) {
    this.ansiCode = ansiCode;
  }

  @Override
  public String toString() {
    return ansiCode;
  }
}
