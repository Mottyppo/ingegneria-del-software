package it.unibs.ingesw.lib;

/**
 * <code>Enum</code> that collects some ansi decorations in order to format
 * strings in the terminal. It can add decorations to the strings.
 *
 */
public enum AnsiDecorations {
  /** Formats the words with an <i>underline</i> decoration. */
  UNDERLINE("\u001B[4m");

  private final String ansiCode;

  AnsiDecorations(String ansiCode) {
    this.ansiCode = ansiCode;
  }

  @Override
  public String toString() {
    return ansiCode;
  }
}
