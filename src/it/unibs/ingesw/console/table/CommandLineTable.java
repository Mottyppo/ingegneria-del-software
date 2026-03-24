package it.unibs.ingesw.console.table;

import it.unibs.ingesw.console.format.Alignment;
import it.unibs.ingesw.console.format.AnsiColors;
import it.unibs.ingesw.console.format.AnsiWeights;
import it.unibs.ingesw.console.format.FormatStrings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Utility class for rendering tables in a command-line interface.
 */
public class CommandLineTable {
  private static final String ERROR_LABEL = FormatStrings.addFormat("Errore!", AnsiColors.RED, AnsiWeights.BOLD, null);
  private static final String ADD_ROWS_EXCEPTION = "Tutte le righe devono essere liste della stessa lunghezza dell'intestazione.";
  private static final String EMPTY_CELL = "";
  private static final String COLUMN_SEPARATOR = " ";
  private static final String EMPTY_ROW_SEPARATOR = "";
  private static final String HORIZONTAL_FRAME_JOIN = "+";
  private static final char HORIZONTAL_SEPARATOR = '-';
  private static final char VERTICAL_SEPARATOR = '|';

  /**
   * Indicates whether vertical separators should be drawn between cells.
   */
  private boolean showVLines;
  /**
   * Controls the alignment used for each table cell.
   */
  private Alignment cellsAlignment;
  /**
   * Stores the table headers.
   */
  private List<String> headers;
  /**
   * Stores the table rows.
   */
  private List<List<String>> rows;

  /**
   * Creates an empty command-line table with left-aligned cells.
   */
  public CommandLineTable() {
    this.showVLines = false;
    this.cellsAlignment = Alignment.LEFT;
    this.headers = new ArrayList<>();
    this.rows = new ArrayList<>();
  }

  /**
   * Returns whether vertical separators are enabled.
   *
   * @return {@code true} if vertical separators are enabled.
   */
  public boolean isShowVLines() {
    return showVLines;
  }

  /**
   * Sets whether vertical separators should be shown.
   *
   * @param showVLines {@code true} to show vertical separators.
   */
  public void setShowVLines(boolean showVLines) {
    this.showVLines = showVLines;
  }

  /**
   * Returns the current cell alignment.
   *
   * @return The cell alignment.
   */
  public Alignment getCellsAlignment() {
    return cellsAlignment;
  }

  /**
   * Sets the cell alignment.
   *
   * @param cellsAlignment The desired alignment.
   */
  public void setCellsAlignment(Alignment cellsAlignment) {
    this.cellsAlignment = cellsAlignment;
  }

  /**
   * Returns the headers currently stored in the table.
   *
   * @return The headers list.
   */
  public List<String> getHeaders() {
    return headers;
  }

  /**
   * Replaces the current headers list.
   *
   * @param headers The new headers.
   */
  public void setHeaders(List<String> headers) {
    this.headers = headers;
  }

  /**
   * Returns the rows currently stored in the table.
   *
   * @return The rows list.
   */
  public List<List<String>> getRows() {
    return rows;
  }

  /**
   * Replaces the current rows list.
   *
   * @param rows The new rows.
   */
  public void setRows(List<List<String>> rows) {
    this.rows = rows;
  }

  /**
   * Adds one or more headers to the table and expands existing rows accordingly.
   *
   * @param headers The headers to add.
   */
  public void addHeaders(List<String> headers) {
    this.headers.addAll(headers);

    List<String> rowFillings = Collections.nCopies(headers.size(), EMPTY_CELL);
    for (List<String> row : this.rows)
      row.addAll(rowFillings);
  }

  /**
   * Adds rows to the table after validating their size against the headers.
   *
   * @param rows The rows to add.
   * @throws IllegalArgumentException If a row has a different size from the headers.
   */
  public void addRows(List<List<String>> rows) throws IllegalArgumentException {
    for (List<String> row : rows) {
      if (row.size() != this.headers.size())
        throw new IllegalArgumentException(CommandLineTable.ERROR_LABEL + "\n" + CommandLineTable.ADD_ROWS_EXCEPTION);
    }

    this.rows.addAll(rows);
  }

  /**
   * Fills empty cells using the provided nested list of replacement values.
   *
   * @param fillings The replacement values for empty cells.
   */
  public void fillHoles(List<List<String>> fillings) {
    int fillI = 0;

    for (List<String> row : this.rows) {
      int fillJ = 0;
      boolean filled = false;

      for (int j = 0; j < row.size(); j++) {
        String cell = row.get(j);

        if (fillI >= fillings.size() || fillJ >= fillings.get(fillI).size() || !cell.isEmpty())
          continue;

        row.set(j, fillings.get(fillI).get(fillJ));
        filled = true;
        fillJ++;
      }

      fillI += filled ? 1 : 0;
    }
  }

  /**
   * Builds the horizontal frame for the table.
   *
   * @param widths The width of each column.
   * @return The horizontal frame string.
   */
  private String buildHFrame(List<Integer> widths) {
    StringBuilder frame = new StringBuilder();

    for (int i = 0; i < widths.size(); i++) {
      int width = widths.get(i);
      StringBuilder framePiece = new StringBuilder();

      if (i == 0 && this.showVLines)
        framePiece.append(CommandLineTable.HORIZONTAL_FRAME_JOIN);

      framePiece.append(FormatStrings.repeatChar(CommandLineTable.HORIZONTAL_SEPARATOR, width));

      framePiece.append(this.showVLines ? CommandLineTable.HORIZONTAL_FRAME_JOIN : CommandLineTable.COLUMN_SEPARATOR);

      frame.append(framePiece.toString());
    }

    return frame.toString();
  }

  /**
   * Computes the maximum width of each column.
   *
   * @param table The table rows, including the headers.
   * @return The maximum width per column.
   */
  private List<Integer> getMaxWidthsPerColumn(List<List<String>> table) {
    List<Integer> widths = new ArrayList<>(Collections.nCopies(this.headers.size(), 0));

    for (List<String> row : table) {
      for (int i = 0; i < row.size(); i++) {
        String cell = row.get(i);
        widths.set(i, Math.max(widths.get(i), cell.length()));
      }
    }

    return widths.stream()
        .map(x -> x + (this.cellsAlignment == Alignment.CENTER ? 2 : 1))
        .collect(Collectors.toList());
  }

  /**
   * Renders the table as a formatted string.
   *
   * @return The textual representation of the table.
   */
  @Override
  public String toString() {
    StringJoiner tableStrJoiner = new StringJoiner("\n");

    List<List<String>> table = new ArrayList<>(this.rows);
    table.addFirst(this.headers);

    List<Integer> widths = this.getMaxWidthsPerColumn(table);

    String hFrame = this.buildHFrame(widths);

    for (List<String> row : table) {
      tableStrJoiner.add(hFrame);

      StringJoiner rowStrJoiner = new StringJoiner(this.showVLines ? EMPTY_ROW_SEPARATOR : COLUMN_SEPARATOR);
      for (int i = 0; i < row.size(); i++) {
        String cell = row.get(i);

        if (i == 0 && this.showVLines)
          rowStrJoiner.add(Character.toString(CommandLineTable.VERTICAL_SEPARATOR));

        String formattedCell = "";
        if (this.cellsAlignment.equals(Alignment.CENTER)) {
          formattedCell = FormatStrings.center(cell, widths.get(i));
        } else {
          boolean left = this.cellsAlignment.getIndex() < 0;
          formattedCell = FormatStrings.column(cell, widths.get(i), left);
        }

        rowStrJoiner.add(formattedCell);

        if (this.showVLines)
          rowStrJoiner.add(Character.toString(CommandLineTable.VERTICAL_SEPARATOR));
      }

      tableStrJoiner.add(rowStrJoiner.toString());
    }

    tableStrJoiner.add(hFrame);

    return tableStrJoiner.toString();
  }
}
