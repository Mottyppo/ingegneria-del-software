package it.unibs.ingesw.lib;

/**
 * <code>Enum</code> that collects some ansi colors in order to format strings
 * in the terminal.
 */
public enum AnsiColors {
  /** Ansi code for clearing the terminal */
  CLEAR("\033[H\033[2J"),
  /** Resets the colors of the terminal. */
  RESET("\u001B[0m"),
  /** Colors the words <i>red</i>. */
  RED("\u001B[31m"),
  /** Colors the words <i>green</i>. */
  GREEN("\u001B[32m"),
  /** Colors the words <i>yellow</i>. */
  YELLOW("\u001B[33m"),
  /** Colors the words <i>blue</i>. */
  BLUE("\u001B[34m");

  private final String ansiCode;

  AnsiColors(String ansiCode) {
    this.ansiCode = ansiCode;
  }

  @Override
  public String toString() {
    return ansiCode;
  }
}
