package it.unibs.ingesw.console.format;

import java.util.StringJoiner;

/**
 * Utility class for composing and decorating strings for terminal output.
 *
 * <p>
 * The class groups helpers for framing, alignment, repetition and ANSI styling.
 * </p>
 */
public class FormatStrings {
  private static final String UNSUPPORTED_OP_ERR_MSG = String.format("%sQuesta classe non e' istanziabile!%s",
      AnsiColors.RED, AnsiColors.RESET);
  private static final char SPACE = ' ';
  private static final char NEW_LINE = '\n';

  private FormatStrings() throws UnsupportedOperationException {
    throw new UnsupportedOperationException(FormatStrings.UNSUPPORTED_OP_ERR_MSG);
  }

  /**
   * Builds a framed representation of the provided string.
   *
   * @param toFrame  The string to frame.
   * @param settings The frame rendering settings.
   * @return The framed string.
   */
  public static String frame(String toFrame, FrameSettings settings) {
    String hFrame = FormatStrings.repeatChar(settings.getHorizontalFrame(), settings.getWidth());
    StringJoiner framed = new StringJoiner(String.valueOf(FormatStrings.NEW_LINE));

    framed.add(hFrame);

    StringBuilder titleLine = new StringBuilder();
    if (settings.isVerticalFrameEnabled()) {
      settings.setWidth(settings.getWidth() - 2);

      titleLine.append(settings.getVerticalFrame());
      titleLine.append(settings.getVerticalFrame());
    }

    int offset = settings.isVerticalFrameEnabled() ? 1 : 0;
    if (settings.getAlignment().equals(Alignment.CENTER))
      titleLine.insert(offset, FormatStrings.center(toFrame, settings.getWidth()));
    else
      // @formatter:off
      titleLine.insert(offset, FormatStrings.column(
        toFrame,
        settings.getWidth(),
        settings.getAlignment().equals(Alignment.LEFT)
      ));
      // @formatter:on

    framed.add(titleLine);

    framed.add(hFrame);

    return framed.toString();
  }

  /**
   * Aligns the given string inside a fixed-width column.
   *
   * @param toColumnize The string to align.
   * @param width       The target width of the column.
   * @param left        Whether the text should be aligned to the left.
   * @return The aligned string.
   */
  public static String column(String toColumnize, int width, boolean left) {
    String columned = toColumnize.length() > width ? toColumnize.substring(0, width) : toColumnize;
    String spaces = FormatStrings.repeatChar(FormatStrings.SPACE, width - columned.length());

    return left ? columned.concat(spaces) : spaces.concat(columned);
  }

  /**
   * Centers the given string in a fixed-width space.
   *
   * @param toCenter The string to center.
   * @param width    The target width.
   * @return The centered string.
   */
  public static String center(String toCenter, int width) {
    int toCenterLength = toCenter.length();

    if (toCenterLength > width)
      return toCenter.substring(0, width);

    if (toCenterLength == width)
      return toCenter;

    StringBuilder builder = new StringBuilder(width);
    int whitespaces = width - toCenterLength;
    int whitespacesBefore = Math.floorDiv(whitespaces, 2);
    int whitespacesAfter = whitespaces - whitespacesBefore;

    builder.append(FormatStrings.repeatChar(FormatStrings.SPACE, whitespacesBefore));
    builder.append(toCenter);
    builder.append(FormatStrings.repeatChar(FormatStrings.SPACE, whitespacesAfter));

    return builder.toString();
  }

  /**
   * Repeats a character the requested number of times.
   *
   * @param character The character to repeat.
   * @param times     The repetition count.
   * @return The repeated character sequence.
   */
  public static String repeatChar(char character, int times) {
    return String.valueOf(character).repeat(Math.max(0, times));
  }

  /**
   * Wraps the given string with blank lines around it.
   *
   * @param toIsolate The string to isolate.
   * @return The isolated string.
   */
  public static String isolatedLine(String toIsolate) {
    return FormatStrings.NEW_LINE + toIsolate + FormatStrings.NEW_LINE;
  }

  /**
   * Formats a string by applying the provided ANSI color, weight and decoration.
   *
   * @param toFormat   The string to format.
   * @param color      The color to apply, or {@code null} for no color.
   * @param weight     The weight to apply, or {@code null} for no weight.
   * @param decoration The decoration to apply, or {@code null} for no decoration.
   * @return The formatted string.
   */
  public static String addFormat(String toFormat, AnsiColors color, AnsiWeights weight, AnsiDecorations decoration) {
    StringBuilder builder = new StringBuilder();
    boolean reset = false;

    if (color != null) {
      reset = true;
      builder.append(color);
    }

    if (weight != null) {
      reset = true;
      builder.append(weight);
    }

    if (decoration != null) {
      reset = true;
      builder.append(decoration);
    }

    builder.append(toFormat);

    if (reset)
      builder.append(AnsiColors.RESET);

    return builder.toString();
  }
}
