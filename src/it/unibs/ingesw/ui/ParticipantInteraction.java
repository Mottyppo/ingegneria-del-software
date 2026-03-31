package it.unibs.ingesw.ui;

import it.unibs.ingesw.console.format.Alignment;
import it.unibs.ingesw.console.format.AnsiColors;
import it.unibs.ingesw.console.format.AnsiDecorations;
import it.unibs.ingesw.console.format.AnsiWeights;
import it.unibs.ingesw.console.format.FormatStrings;
import it.unibs.ingesw.console.format.FormatValues;
import it.unibs.ingesw.console.input.InputData;
import it.unibs.ingesw.console.menu.Menu;
import it.unibs.ingesw.model.Notification;
import it.unibs.ingesw.model.Proposal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Handles command-line interactions dedicated to fruitori.
 */
public class ParticipantInteraction {
    private static final String FRONTEND_TITLE = "=== Frontend Fruitore ===";

    private static final String ACCESS_MENU_TITLE = "Accesso Fruitore";
    private static final String ACCESS_LOGIN = "Login";
    private static final String ACCESS_SIGNUP = "Sign-up";

    private static final String MAIN_MENU_TITLE = "Menu Fruitore";
    private static final String MAIN_MENU_SHOW_BOARD = "Visualizza bacheca per categoria";
    private static final String MAIN_MENU_SUBSCRIBE = "Aderisci a proposta aperta";
    private static final String MAIN_MENU_OPEN_SPACE = "Apri spazio personale";

    private static final String LOGIN_USERNAME_PROMPT = "Username fruitore: ";
    private static final String LOGIN_PASSWORD_PROMPT = "Password fruitore: ";
    private static final String SIGNUP_NAME_PROMPT = "Nome: ";
    private static final String SIGNUP_SURNAME_PROMPT = "Cognome: ";
    private static final String SIGNUP_USERNAME_PROMPT = "Scegli username: ";
    private static final String SIGNUP_PASSWORD_PROMPT = "Scegli password: ";

    private static final String INVALID_CREDENTIALS_MESSAGE = "Credenziali fruitore non valide.";
    private static final String SIGNUP_SUCCESS_MESSAGE = "Sign-up completato con successo.";
    private static final String SIGNUP_FAILURE_MESSAGE = "Sign-up non riuscito. Username gia' in uso o dati non validi.";
    private static final String SUBSCRIPTION_SUCCESS_MESSAGE = "Iscrizione completata.";
    private static final String SUBSCRIPTION_FAILURE_MESSAGE = "Iscrizione non riuscita (proposta non aperta, scaduta o piena).";
    private static final String NOTIFICATION_REMOVE_SUCCESS_MESSAGE = "Notifica rimossa.";
    private static final String NOTIFICATION_REMOVE_FAILURE_MESSAGE = "Impossibile rimuovere la notifica.";
    private static final String NO_OPEN_PROPOSALS_MESSAGE = "Bacheca vuota.";
    private static final String NO_NOTIFICATIONS_MESSAGE = "Spazio personale vuoto.";
    private static final String CHOOSE_OPEN_PROPOSAL_TITLE = "Seleziona la proposta a cui aderire";
    private static final String CHOOSE_NOTIFICATION_TO_REMOVE_TITLE = "Seleziona la notifica da cancellare";
    private static final String ASK_DELETE_NOTIFICATION = "Vuoi cancellare una notifica";

    private static final String CATEGORY_TITLE_TEMPLATE = "Categoria: %s";
    private static final String NEW_LINE = "\n";

    private static final List<String> ACCESS_MENU_ENTRIES = List.of(
            ACCESS_LOGIN,
            ACCESS_SIGNUP
    );

    private static final List<String> MAIN_MENU_ENTRIES = List.of(
            MAIN_MENU_SHOW_BOARD,
            MAIN_MENU_SUBSCRIBE,
            MAIN_MENU_OPEN_SPACE
    );

    public void printFrontEndTitle() {
        printInfo(FormatStrings.addFormat(FRONTEND_TITLE, AnsiColors.BLUE, AnsiWeights.BOLD, AnsiDecorations.UNDERLINE));
    }

    public int chooseAccessMenu() {
        return new Menu(ACCESS_MENU_TITLE, ACCESS_MENU_ENTRIES, true, Alignment.CENTER, true).choose();
    }

    public int chooseMainMenu() {
        return new Menu(MAIN_MENU_TITLE, MAIN_MENU_ENTRIES, true, Alignment.CENTER, true).choose();
    }

    public String readLoginUsername() {
        return InputData.readNonEmptyString(LOGIN_USERNAME_PROMPT, true).trim();
    }

    public String readLoginPassword() {
        return InputData.readNonEmptyString(LOGIN_PASSWORD_PROMPT, false);
    }

    public String readSignUpName() {
        return InputData.readNonEmptyString(SIGNUP_NAME_PROMPT, false).trim();
    }

    public String readSignUpSurname() {
        return InputData.readNonEmptyString(SIGNUP_SURNAME_PROMPT, false).trim();
    }

    public String readSignUpUsername() {
        return InputData.readNonEmptyString(SIGNUP_USERNAME_PROMPT, true).trim();
    }

    public String readSignUpPassword() {
        return InputData.readNonEmptyString(SIGNUP_PASSWORD_PROMPT, false);
    }

    public void printInvalidCredentials() {
        printError(INVALID_CREDENTIALS_MESSAGE);
    }

    public void printSignUpSuccess() {
        printSuccess(SIGNUP_SUCCESS_MESSAGE);
    }

    public void printSignUpFailure() {
        printError(SIGNUP_FAILURE_MESSAGE);
    }

    public void printSubscriptionResult(boolean result) {
        if (result) {
            printSuccess(SUBSCRIPTION_SUCCESS_MESSAGE);
            return;
        }
        printError(SUBSCRIPTION_FAILURE_MESSAGE);
    }

    public void printNotificationRemoveResult(boolean result) {
        if (result) {
            printSuccess(NOTIFICATION_REMOVE_SUCCESS_MESSAGE);
            return;
        }
        printError(NOTIFICATION_REMOVE_FAILURE_MESSAGE);
    }

    public void showBoardByCategory(Map<String, List<Proposal>> board) {
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

    public int chooseOpenProposal(List<Proposal> proposals) {
        if (proposals == null || proposals.isEmpty()) {
            printCancelled(NO_OPEN_PROPOSALS_MESSAGE);
            return -1;
        }
        return chooseIndex(
                proposals,
                CHOOSE_OPEN_PROPOSAL_TITLE,
                this::summarizeProposalForSelection
        );
    }

    public void showNotifications(List<Notification> notifications) {
        printInfo("== Spazio Personale ==");
        if (notifications == null || notifications.isEmpty()) {
            printCancelled(NO_NOTIFICATIONS_MESSAGE);
            return;
        }
        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            System.out.println((i + 1) + ") " + notification.getMessage());
        }
        System.out.println();
    }

    public boolean askDeleteNotification() {
        return InputData.readYesOrNo(ASK_DELETE_NOTIFICATION);
    }

    public int chooseNotificationToDelete(List<Notification> notifications) {
        return chooseIndex(
                notifications,
                CHOOSE_NOTIFICATION_TO_REMOVE_TITLE,
                Notification::getMessage
        );
    }

    public <T> int chooseIndex(List<T> items, String title, Function<T, String> nameExtractor) {
        if (items == null || items.isEmpty()) {
            return -1;
        }
        List<String> entries = items.stream().map(nameExtractor).collect(Collectors.toCollection(ArrayList::new));
        int choice = new Menu(title, entries, true, Alignment.CENTER, true).choose();
        return choice == 0 ? -1 : choice - 1;
    }

    private String summarizeProposalForSelection(Proposal proposal) {
        String title = proposal.getFieldValues().getOrDefault("Titolo", "(senza titolo)");
        return "#" + proposal.getId() + " | " + proposal.getCategoryName() + " | " + title;
    }

    private void printInfo(String message) {
        System.out.println(NEW_LINE + message);
    }

    private void printError(String message) {
        System.out.println(FormatStrings.addFormat(message, AnsiColors.RED, AnsiWeights.BOLD, null) + NEW_LINE);
    }

    private void printCancelled(String message) {
        System.out.println(FormatStrings.addFormat(message, AnsiColors.YELLOW, AnsiWeights.ITALIC, null) + NEW_LINE);
    }

    private void printSuccess(String message) {
        System.out.println(FormatStrings.addFormat(message, AnsiColors.BLUE, AnsiWeights.BOLD, AnsiDecorations.UNDERLINE) + NEW_LINE);
    }
}
