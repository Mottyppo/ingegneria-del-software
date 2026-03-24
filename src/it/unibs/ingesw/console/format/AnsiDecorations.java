package it.unibs.ingesw.console.format;

/**
 * Enumeration of ANSI decorations used to enrich terminal output.
 */
public enum AnsiDecorations {
  /** Formats the words with an <i>underline</i> decoration. */
  UNDERLINE(underlineCode());

  private static final String UNDERLINE_CODE = "\u001B[4m";

  private static String underlineCode() {
    return UNDERLINE_CODE;
  }

  private final String ansiCode;

  AnsiDecorations(String ansiCode) {
    this.ansiCode = ansiCode;
  }

  @Override
  public String toString() {
    return ansiCode;
  }
}
