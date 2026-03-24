package it.unibs.ingesw.console.menu;

import it.unibs.ingesw.console.format.Alignment;
import it.unibs.ingesw.console.format.AnsiColors;
import it.unibs.ingesw.console.format.AnsiWeights;
import it.unibs.ingesw.console.format.FormatStrings;
import it.unibs.ingesw.console.format.FrameSettings;
import it.unibs.ingesw.console.input.InputData;

import java.util.List;
import java.util.StringJoiner;

/**
 * Utility class for rendering and handling command-line menus.
 *
 * <p>
 * Zero is treated as the exit option whenever the menu is configured to show it.
 * </p>
 */
public class Menu {
  private static final char NEW_LINE = '\n';
  private static final String ENTRY_PREFIX = ". ";
  private static final String EXIT_ENTRY = "0. Esci";
  private static final String INSERT_REQUEST = "> ";
  private static final String ATTENTION_LABEL = FormatStrings.addFormat("Attenzione!", AnsiColors.RED, AnsiWeights.BOLD, null);
  private static final String NEGATIVE_MILLIS_ERROR = ATTENTION_LABEL + NEW_LINE
      + FormatStrings.addFormat("Il tempo di attesa non puo' essere negativo.", null, AnsiWeights.ITALIC, null);
  private static final int DEFAULT_LOADING_DELAY_MS = 500;
  private static final int DEFAULT_LOADING_STEPS = 3;

  /**
   * Represents the title of the menu.
   */
  private final String title;
  /**
   * Contains all the menu entries.
   */
  private final List<String> entries;
  /**
   * Indicates whether the exit option should be displayed.
   */
  private final boolean useExitEntry;
  /**
   * Settings used to render the title frame.
   */
  private final FrameSettings titleSettings;

  /**
   * Creates a menu with the provided title, entries and rendering settings.
   *
   * @param title            The menu title.
   * @param entries          The menu entries.
   * @param useExitEntry     Whether to display the exit option.
   * @param titleAlignment   The alignment of the title.
   * @param useVerticalFrame Whether to show vertical borders around the title.
   */
  public Menu(String title, List<String> entries, boolean useExitEntry, Alignment titleAlignment,
      boolean useVerticalFrame) {
    this.title = title;
    this.entries = entries;
    this.useExitEntry = useExitEntry;
    this.titleSettings = new FrameSettings(this.calculateFrameLength(title, entries), titleAlignment, useVerticalFrame);
  }

  /**
   * Clears the terminal console.
   */
  public static void clearConsole() {
    System.out.println(AnsiColors.CLEAR);
    System.out.flush();
  }

  /**
   * Pauses the current thread for the specified time.
   *
   * @param milliseconds The waiting time in milliseconds.
   * @throws InterruptedException If the current thread is interrupted while waiting.
   */
  public static void wait(int milliseconds) throws InterruptedException {
    try {
      Thread.sleep(milliseconds);
    } catch (IllegalArgumentException e) {
      System.out.println(Menu.NEGATIVE_MILLIS_ERROR);
    }
  }

  /**
   * Prints a short loading message with a small animated delay.
   *
   * @param message The message to display.
   * @throws InterruptedException If the current thread is interrupted while waiting.
   */
  public static void loadingMessage(String message) throws InterruptedException {
    System.out.print(FormatStrings.addFormat(message, AnsiColors.BLUE, AnsiWeights.BOLD, null));
    for (int i = 0; i < Menu.DEFAULT_LOADING_STEPS; i++) {
      System.out.print(".");
      Menu.wait(Menu.DEFAULT_LOADING_DELAY_MS);
    }
    System.out.println();
    Menu.clearConsole();
  }

  /**
   * Prints the menu and lets the user choose an option.
   *
   * @return The selected option index.
   */
  public int choose() {
    this.printMenu();

    if (this.useExitEntry)
      return InputData.readIntegerBetween(Menu.INSERT_REQUEST, 0, this.entries.size());
    else
      return InputData.readIntegerBetween(Menu.INSERT_REQUEST, 1, this.entries.size());
  }

  /**
   * Calculates the frame length by considering the longest title or entry.
   *
   * @param title   The menu title.
   * @param entries The menu entries.
   * @return The calculated frame width.
   */
  private int calculateFrameLength(String title, List<String> entries) {
    int frameLength = title.length();

    for (String entry : entries)
      frameLength = Math.max(entry.length(), frameLength);

    return frameLength + 10;
  }

  /**
   * Prints the menu title and entries.
   */
  private void printMenu() {
    StringJoiner stringedMenu = new StringJoiner(String.valueOf(Menu.NEW_LINE));

    stringedMenu.add(FormatStrings.frame(this.title, this.titleSettings));

    for (int i = 0; i < this.entries.size(); i++)
      stringedMenu.add("%d%s%s".formatted(i + 1, Menu.ENTRY_PREFIX, this.entries.get(i)));

    if (this.useExitEntry)
      stringedMenu.add(FormatStrings.isolatedLine(Menu.EXIT_ENTRY));

    System.out.println(stringedMenu);
  }
}
