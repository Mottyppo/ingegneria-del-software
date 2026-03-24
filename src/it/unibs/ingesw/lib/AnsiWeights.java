package it.unibs.ingesw.lib;

/**
 * <code>Enum</code> that collects some ansi weights in order to format
 * strings in the terminal. It can change the font weight of strings.
 *
 */
public enum AnsiWeights {
  /** Formats the words in a <i>bold</i> weight. */
  BOLD("\u001B[1m"),
  /** Formats the words in an <i>italic</i> weight. */
  ITALIC("\u001B[3m");

  private final String ansiCode;

  AnsiWeights(String ansiCode) {
    this.ansiCode = ansiCode;
  }

  @Override
  public String toString() {
    return ansiCode;
  }
}
