package it.unibs.ingesw.console.format;

/**
 * Configuration object used to render framed text with {@link FormatStrings}.
 */
public class FrameSettings {
  private static final char DEFAULT_HORIZONTAL_FRAME = '-';
  private static final char DEFAULT_VERTICAL_FRAME = '|';

  private int width;
  private Alignment alignment;
  private char horizontalFrame;
  private boolean verticalFrameEnabled;
  private char verticalFrame;

  /**
   * Creates a new settings instance with the default frame characters.
   *
   * @param width                The width of the frame.
   * @param alignment            The alignment of the frame.
   * @param verticalFrameEnabled Whether the vertical frame is enabled.
   */
  public FrameSettings(int width, Alignment alignment, boolean verticalFrameEnabled) {
    this(width, alignment, verticalFrameEnabled, DEFAULT_HORIZONTAL_FRAME, DEFAULT_VERTICAL_FRAME);
  }

  /**
   * Creates a new settings instance with custom frame characters.
   *
   * @param width                The width of the frame.
   * @param alignment            The alignment of the frame.
   * @param verticalFrameEnabled Whether the vertical frame is enabled.
   * @param hFrame               The horizontal frame character.
   * @param vFrame               The vertical frame character.
   */
  public FrameSettings(int width, Alignment alignment, boolean verticalFrameEnabled, char hFrame, char vFrame) {
    this.width = width;
    this.alignment = alignment;
    this.horizontalFrame = hFrame;
    this.verticalFrameEnabled = verticalFrameEnabled;
    this.verticalFrame = vFrame;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public Alignment getAlignment() {
    return alignment;
  }

  public void setAlignment(Alignment alignment) {
    this.alignment = alignment;
  }

  public char getHorizontalFrame() {
    return horizontalFrame;
  }

  public void setHorizontalFrame(char horizontalFrame) {
    this.horizontalFrame = horizontalFrame;
  }

  public boolean isVerticalFrameEnabled() {
    return verticalFrameEnabled;
  }

  public void setVerticalFrameEnabled(boolean useVerticalFrame) {
    this.verticalFrameEnabled = useVerticalFrame;
  }

  public char getVerticalFrame() {
    return verticalFrame;
  }

  public void setVerticalFrame(char verticalFrame) {
    this.verticalFrame = verticalFrame;
  }
}
