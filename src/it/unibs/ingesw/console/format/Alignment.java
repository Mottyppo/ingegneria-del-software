package it.unibs.ingesw.console.format;

/**
 * Enumeration of the supported horizontal alignments for strings and tables.
 */
public enum Alignment {
  /** Represents the <i>left</i> alignment. */
  LEFT(-1),
  /** Represents the <i>center</i> alignment. */
  CENTER(0),
  /** Represents the <i>right</i> alignment. */
  RIGHT(1);

  private final int index;

  Alignment(int index) {
    this.index = index;
  }

  public int getIndex() {
    return index;
  }
}
