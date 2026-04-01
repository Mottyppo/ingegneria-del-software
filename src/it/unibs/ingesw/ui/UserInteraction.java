package it.unibs.ingesw.ui;

import it.unibs.ingesw.console.format.Alignment;
import it.unibs.ingesw.console.format.AnsiColors;
import it.unibs.ingesw.console.format.AnsiDecorations;
import it.unibs.ingesw.console.format.AnsiWeights;
import it.unibs.ingesw.console.format.FormatStrings;
import it.unibs.ingesw.console.format.FormatValues;
import it.unibs.ingesw.console.menu.Menu;
import it.unibs.ingesw.model.Proposal;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Shared command-line interaction utilities for all application users.
 *
 * <p>The class contains common rendering helpers and generic selection utilities reused by
 * role-specific interaction adapters such as configurator and fruitore views.</p>
 */
public class UserInteraction {
    private static final String BANNER =
            """
            ██ ▄▄  ▄▄  ▄▄▄▄ ▄▄▄▄▄  ▄▄▄▄ ▄▄  ▄▄ ▄▄▄▄▄ ▄▄▄▄  ▄▄  ▄▄▄    ▄▄▄▄  ▄▄▄▄▄ ▄▄      ▄█████  ▄▄▄  ▄▄▄▄▄ ▄▄▄▄▄▄ ▄▄   ▄▄  ▄▄▄  ▄▄▄▄  ▄▄▄▄▄
            ██ ███▄██ ██ ▄▄ ██▄▄  ██ ▄▄ ███▄██ ██▄▄  ██▄█▄ ██ ██▀██   ██▀██ ██▄▄  ██      ▀▀▀▄▄▄ ██▀██ ██▄▄    ██   ██ ▄ ██ ██▀██ ██▄█▄ ██▄▄
            ██ ██ ▀██ ▀███▀ ██▄▄▄ ▀███▀ ██ ▀██ ██▄▄▄ ██ ██ ██ ██▀██   ████▀ ██▄▄▄ ██▄▄▄   █████▀ ▀███▀ ██      ██    ▀█▀█▀  ██▀██ ██ ██ ██▄▄▄

            Masciali Luca - 747335
            Mottinelli Matteo - 745550
            Nizzotti Mattia - 746348
            """;
    private static final String APP_TITLE = "=== Sistema Gestione Iniziative Culturali ===";
    private static final String SHUTDOWN_MESSAGE = "=== Chiusura programma ===";
    private static final String NO_ELEMENT_AVAILABLE_MESSAGE = "Nessun elemento disponibile.";
    private static final String NO_OPEN_PROPOSALS_MESSAGE = "Bacheca vuota.";
    private static final String CATEGORY_TITLE_TEMPLATE = "Categoria: %s";
    private static final String NEW_LINE = "\n";

    public void clearConsole() {
        Menu.clearConsole();
    }

    public void printBanner() {
        System.out.println(BANNER);
    }

    public void printApplicationTitle() {
        printInfo(FormatStrings.addFormat(APP_TITLE, AnsiColors.BLUE, AnsiWeights.BOLD, AnsiDecorations.UNDERLINE));
    }

    public void printProgramClosure() {
        printInfo(FormatStrings.addFormat(SHUTDOWN_MESSAGE, AnsiColors.BLUE, AnsiWeights.BOLD, AnsiDecorations.UNDERLINE));
    }

    protected void printInfo(String message) {
        System.out.println(NEW_LINE + message);
    }

    protected void printError(String message) {
        System.out.println(FormatStrings.addFormat(message, AnsiColors.RED, AnsiWeights.BOLD, null) + NEW_LINE);
    }

    protected void printCancelled(String message) {
        System.out.println(FormatStrings.addFormat(message, AnsiColors.YELLOW, AnsiWeights.ITALIC, null) + NEW_LINE);
    }

    protected void printSuccess(String message) {
        System.out.println(FormatStrings.addFormat(message, AnsiColors.BLUE, AnsiWeights.BOLD, AnsiDecorations.UNDERLINE) + NEW_LINE);
    }

    protected String summarizeProposalForSelection(Proposal proposal) {
        String title = proposal.getFieldValues().getOrDefault("Titolo", "(senza titolo)");
        return "#" + proposal.getId() + " | " + proposal.getCategoryName() + " | " + title;
    }

    protected void showBoardByCategory(Map<String, List<Proposal>> board) {
        if (board == null || board.isEmpty()) {
            printCancelled(NO_OPEN_PROPOSALS_MESSAGE);
            return;
        }

        for (Map.Entry<String, List<Proposal>> entry : board.entrySet()) {
            printInfo(CATEGORY_TITLE_TEMPLATE.formatted(entry.getKey()));
            for (Proposal proposal : entry.getValue()) {
                String publication = FormatValues.formatDateTime(proposal.getPublicationDate());
                System.out.println("- Proposta #" + proposal.getId()
                        + " | Stato: " + proposal.getCurrentStatus()
                        + " | Pubblicata: " + publication
                        + " | Iscritti: " + proposal.getSubscribers().size());
                for (Map.Entry<String, String> valueEntry : proposal.getFieldValues().entrySet()) {
                    String formattedValue = FormatValues.formatField(
                            proposal,
                            valueEntry.getKey(),
                            valueEntry.getValue()
                    );
                    System.out.println("  - " + valueEntry.getKey() + ": " + formattedValue);
                }
            }
            System.out.println();
        }
    }

    protected <T> int chooseIndex(List<T> items, String title, Function<T, String> nameExtractor) {
        return chooseIndex(items, title, nameExtractor, true);
    }

    protected <T> int chooseIndex(
            List<T> items,
            String title,
            Function<T, String> nameExtractor,
            boolean printMessageWhenEmpty
    ) {
        if (items == null || items.isEmpty()) {
            if (printMessageWhenEmpty) {
                printCancelled(NO_ELEMENT_AVAILABLE_MESSAGE);
            }
            return -1;
        }

        List<String> entries = items.stream().map(nameExtractor).collect(Collectors.toList());
        int choice = new Menu(title, entries, true, Alignment.CENTER, true).choose();
        return choice == 0 ? -1 : choice - 1;
    }
}
