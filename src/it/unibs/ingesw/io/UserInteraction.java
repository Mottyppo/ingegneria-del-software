package it.unibs.ingesw.io;

import it.unibs.ingesw.controller.SystemManager;
import it.unibs.ingesw.lib.Alignment;
import it.unibs.ingesw.lib.CommandLineTable;
import it.unibs.ingesw.lib.InputData;
import it.unibs.ingesw.lib.Menu;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.Category;
import it.unibs.ingesw.model.Configurator;
import it.unibs.ingesw.model.FieldType;
import it.unibs.ingesw.model.DataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UserInteraction {
    private static final List<String[]> BASE_FIELDS = List.of(
        new String[]{"Titolo", "nome di fantasia (esplicativo) attribuito all'iniziativa"},
        new String[]{"Numero di partecipanti", "numero di persone da coinvolgere nell'iniziativa"},
        new String[]{"Termine ultimo di iscrizione", "ultimo giorno utile per iscriversi all'iniziativa"},
        new String[]{"Luogo", "indirizzo del luogo che ospitera' l'iniziativa"},
        new String[]{"Data", "data di inizio dell'iniziativa"},
        new String[]{"Ora", "ora di ritrovo dei partecipanti"},
        new String[]{"Quota individuale", "spesa individuale stimata per l'iniziativa"},
        new String[]{"Data conclusiva", "data di conclusione dell'iniziativa"}
    );

    private final SystemManager manager;

    public UserInteraction(SystemManager manager) {
        this.manager = manager;
    }

    public void start() {
        Menu.clearConsole();
        System.out.println(
            "██ ▄▄  ▄▄  ▄▄▄▄ ▄▄▄▄▄  ▄▄▄▄ ▄▄  ▄▄ ▄▄▄▄▄ ▄▄▄▄  ▄▄  ▄▄▄    ▄▄▄▄  ▄▄▄▄▄ ▄▄      ▄█████  ▄▄▄  ▄▄▄▄▄ ▄▄▄▄▄▄ ▄▄   ▄▄  ▄▄▄  ▄▄▄▄  ▄▄▄▄▄\n" +
            "██ ███▄██ ██ ▄▄ ██▄▄  ██ ▄▄ ███▄██ ██▄▄  ██▄█▄ ██ ██▀██   ██▀██ ██▄▄  ██      ▀▀▀▄▄▄ ██▀██ ██▄▄    ██   ██ ▄ ██ ██▀██ ██▄█▄ ██▄▄\n" +
            "██ ██ ▀██ ▀███▀ ██▄▄▄ ▀███▀ ██ ▀██ ██▄▄▄ ██ ██ ██ ██▀██   ████▀ ██▄▄▄ ██▄▄▄   █████▀ ▀███▀ ██      ██    ▀█▀█▀  ██▀██ ██ ██ ██▄▄▄\n\n" +
            "Masciali Luca - 747335\n" +
            "Mottinelli Matteo - 745550\n" +
            "Nizzotti Mattia - 746348\n"
        );

        System.out.println("=== Backend Configuratore ===");
        Configurator configurator = login();
        if (configurator == null) return;

        if (configurator.isFirstAccess()) {
            manageFirstAccess(configurator);
        }

        if (!manager.areBaseFieldsSet()) {
            System.out.println("\nPrima configurazione: impostazione campi base.");
            setupBaseFields();
        }

        mainMenu();
    }

    public void end() {
        System.out.println("\n=== Chiusura programma ===");
    }

    private Configurator login() {
        while (true) {
            String username = InputData.readNonEmptyString("Username: ", true).trim();
            String password = InputData.readNonEmptyString("Password: ", false);
            Configurator configurator = manager.authenticateConfigurator(username, password);
            if (configurator != null) return configurator;
            System.out.println("Credenziali non valide. Riprova.\n");
        }
    }

    private void manageFirstAccess(Configurator configurator) {
        System.out.println("\nPrimo accesso: scegli le tue credenziali personali.");
        while (true) {
            String newUsername = InputData.readNonEmptyString("Nuovo username: ", true).trim();
            String newPassword = InputData.readNonEmptyString("Nuova password: ", false);
            if (manager.updateCredentials(configurator, newUsername, newPassword)) {
                System.out.println("Credenziali aggiornate con successo.\n");
                return;
            }
            System.out.println("Username gia' in uso. Riprova.\n");
        }
    }

    private void setupBaseFields() {
        List<Field> fields = new ArrayList<>();
        Set<String> localNames = new HashSet<>();

        for (String[] field : BASE_FIELDS) {
            DataType dataType = chooseDataType("Scegli il tipo di dato per il campo base \"" + field[0] + "\"");
            if (dataType == null) {
                System.out.println("Operazione annullata.\n");
                return;
            }
            fields.add(new Field(field[0], field[1], true, FieldType.BASE, dataType));
            localNames.add(field[0].toLowerCase());
        }

        System.out.println("Tipi di dato dei campi base inseriti.");

        while (InputData.readYesOrNo("Vuoi aggiungere un altro campo base")) {
            Field customField = promptForNewField(FieldType.BASE, true, null, localNames);
            if (customField != null) {
                fields.add(customField);
                localNames.add(customField.getName().toLowerCase());
            }
        }

        executeAndPrint(manager.setBaseFields(fields), "Campi base impostati correttamente.", "I campi base risultano gia' impostati.");
    }

    private void mainMenu() {
        boolean exit = false;
        while (!exit) {
            List<String> entries = new ArrayList<>();
            entries.add(manager.areBaseFieldsSet() ? "Visualizza campi base" : "Imposta campi base");
            entries.add("Gestisci campi comuni");
            entries.add("Gestisci categorie");
            entries.add("Visualizza categorie e campi");

            int choice = new Menu("Menu Configuratore", entries, true, Alignment.CENTER, true).choose();
            switch (choice) {
                case 0 -> exit = true;
                case 1 -> {
                    if (manager.areBaseFieldsSet()) showFields("Campi base", manager.getBaseFields());
                    else setupBaseFields();
                }
                case 2 -> { if (requireBaseFields()) commonFieldsMenu(); }
                case 3 -> { if (requireBaseFields()) categoriesMenu(); }
                case 4 -> { if (requireBaseFields()) showFullCategories(); }
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private boolean requireBaseFields() {
        if (!manager.areBaseFieldsSet()) {
            System.out.println("Prima imposta i campi base.\n");
            return false;
        }
        return true;
    }

    private void commonFieldsMenu() {
        boolean exit = false;
        while (!exit) {
            int choice = new Menu("Campi Comuni", List.of("Aggiungi campo comune", "Rimuovi campo comune", "Cambia obbligatorieta'", "Visualizza campi comuni"), true, Alignment.CENTER, true).choose();
            switch (choice) {
                case 0 -> exit = true;
                case 1 -> {
                    Field field = promptForNewField(FieldType.COMMON, false, null, null);
                    if (field != null) executeAndPrint(manager.addCommonField(field), "Campo comune aggiunto.", "Impossibile aggiungere il campo comune.");
                }
                case 2 -> {
                    int index = chooseIndex(manager.getCommonFields(), "Seleziona il campo comune da rimuovere", Field::getName);
                    if (index >= 0) executeAndPrint(manager.removeCommonField(index), "Campo comune rimosso.", "Impossibile rimuovere il campo comune.");
                }
                case 3 -> {
                    int index = chooseIndex(manager.getCommonFields(), "Seleziona il campo comune da modificare", Field::getName);
                    if (index >= 0) executeAndPrint(manager.toggleMandatorinessCommonField(index), "Obbligatorieta' aggiornata.", "Impossibile aggiornare il campo comune.");
                }
                case 4 -> showFields("Campi comuni", manager.getCommonFields());
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private void categoriesMenu() {
        boolean exit = false;
        while (!exit) {
            int choice = new Menu("Categorie", List.of("Aggiungi categoria", "Rimuovi categoria", "Gestisci campi specifici", "Visualizza categorie e campi"), true, Alignment.CENTER, true).choose();
            switch (choice) {
                case 0 -> exit = true;
                case 1 -> addCategory();
                case 2 -> {
                    int index = chooseIndex(manager.getCategories(), "Seleziona la categoria da rimuovere", Category::getName);
                    if (index >= 0) executeAndPrint(manager.removeCategory(index), "Categoria rimossa.", "Impossibile rimuovere la categoria.");
                }
                case 3 -> manageSpecificFields();
                case 4 -> showFullCategories();
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private void addCategory() {
        String name = InputData.readNonEmptyString("Nome categoria: ", false).trim();
        if (!manager.isCategoryNameAvailable(name)) {
            System.out.println("Nome categoria gia' in uso.\n");
            return;
        }

        List<Field> specificFields = new ArrayList<>();
        Set<String> specificNames = new HashSet<>();

        while (InputData.readYesOrNo("Vuoi aggiungere un campo specifico")) {
            Field field = promptForNewField(FieldType.SPECIFIC, false, null, specificNames);
            if (field != null) {
                specificFields.add(field);
                specificNames.add(field.getName().toLowerCase());
            }
        }

        executeAndPrint(manager.addCategory(name, specificFields), "Categoria aggiunta.", "Impossibile aggiungere la categoria.");
    }

    private void manageSpecificFields() {
        int catIndex = chooseIndex(manager.getCategories(), "Seleziona la categoria", Category::getName);
        if (catIndex < 0) return;

        Category category = manager.getCategories().get(catIndex);
        boolean exit = false;

        while (!exit) {
            int choice = new Menu("Campi specifici: " + category.getName(), List.of("Aggiungi campo specifico", "Rimuovi campo specifico", "Cambia obbligatorieta'", "Visualizza campi specifici"), true, Alignment.CENTER, true).choose();
            switch (choice) {
                case 0 -> exit = true;
                case 1 -> {
                    Field field = promptForNewField(FieldType.SPECIFIC, false, category, null);
                    if (field != null) executeAndPrint(manager.addSpecificField(catIndex, field), "Campo specifico aggiunto.", "Impossibile aggiungere il campo specifico.");
                }
                case 2 -> {
                    int fieldIndex = chooseIndex(category.getSpecificFields(), "Seleziona il campo da rimuovere", Field::getName);
                    if (fieldIndex >= 0) executeAndPrint(manager.removeSpecificField(catIndex, fieldIndex), "Campo specifico rimosso.", "Impossibile rimuovere il campo specifico.");
                }
                case 3 -> {
                    int fieldIndex = chooseIndex(category.getSpecificFields(), "Seleziona il campo da modificare", Field::getName);
                    if (fieldIndex >= 0) executeAndPrint(manager.toggleMandatorinessSpecificField(catIndex, fieldIndex), "Obbligatorieta' aggiornata.", "Impossibile aggiornare il campo specifico.");
                }
                case 4 -> showFields("Campi specifici - " + category.getName(), category.getSpecificFields());
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    /**
     * Generalizza la richiesta di creazione di un nuovo campo (Base, Comune o Specifico).
     * @param type Il tipo di campo da creare.
     * @param forceMandatory Se true, salta la domanda "Il campo è obbligatorio?" e lo imposta a true.
     * @param contextCategory La categoria in cui si sta inserendo il campo (può essere null).
     * @param localReservedNames Un set opzionale di nomi attualmente in creazione ma non ancora salvati nel manager.
     */
    private Field promptForNewField(FieldType type, boolean forceMandatory, Category contextCategory, Set<String> localReservedNames) {
        String name = InputData.readNonEmptyString("Nome campo: ", false).trim();

        boolean takenGlobally = !manager.isFieldNameAvailableForCategory(name, contextCategory);
        boolean takenLocally = localReservedNames != null && localReservedNames.contains(name.toLowerCase());

        if (takenGlobally || takenLocally) {
            System.out.println("Nome campo gia' in uso.\n");
            return null;
        }

        String description = InputData.readNonEmptyString("Descrizione: ", false).trim();
        boolean mandatory = forceMandatory || InputData.readYesOrNo("Il campo e' obbligatorio");
        DataType dataType = chooseDataType("Scegli tipo dato per il campo");

        if (dataType == null) {
            System.out.println("Operazione annullata.\n");
            return null;
        }

        return new Field(name, description, mandatory, type, dataType);
    }

    /**
     * Metodo generico per stampare un menu di scelta in base a una lista di oggetti arbitrari.
     * @param items La lista da visualizzare.
     * @param title Il titolo del menu.
     * @param nameExtractor Funzione per estrarre la stringa da stampare per ogni oggetto.
     * @return L'indice scelto, oppure -1 in caso di uscita/annullamento.
     */
    private <T> int chooseIndex(List<T> items, String title, Function<T, String> nameExtractor) {
        if (items == null || items.isEmpty()) {
            System.out.println("Nessun elemento disponibile.\n");
            return -1;
        }
        List<String> entries = items.stream().map(nameExtractor).collect(Collectors.toList());
        int choice = new Menu(title, entries, true, Alignment.CENTER, true).choose();
        return choice == 0 ? -1 : choice - 1;
    }

    private DataType chooseDataType(String title) {
        List<String> entries = Arrays.stream(DataType.values()).map(Enum::toString).collect(Collectors.toList());
        int choice = new Menu(title, entries, true, Alignment.CENTER, true).choose();
        return choice == 0 ? null : DataType.values()[choice - 1];
    }

    private void executeAndPrint(boolean result, String successMsg, String failMsg) {
        System.out.println((result ? successMsg : failMsg) + "\n");
    }

    private void showFullCategories() {
        List<Category> categories = manager.getCategories();
        if (categories.isEmpty()) {
            System.out.println("Nessuna categoria presente.\n");
            return;
        }
        for (Category category : categories) {
            System.out.println("\nCategoria: " + category.getName());
            showFields("Campi", manager.getSharedFieldsForCategory(category));
        }
    }

    private void showFields(String title, List<Field> fields) {
        System.out.println("\n== " + title + " ==");
        if (fields == null || fields.isEmpty()) {
            System.out.println("Nessun campo presente.\n");
            return;
        }
        CommandLineTable table = new CommandLineTable();
        table.setShowVLines(true);
        table.setCellsAlignment(Alignment.LEFT);
        table.addHeaders(List.of("N", "Nome", "Descrizione", "Obblig.", "Tipo", "Dato"));

        List<List<String>> rows = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            Field f = fields.get(i);
            rows.add(List.of(
                    String.valueOf(i + 1), f.getName(), f.getDescription(),
                    f.isMandatory() ? "Si" : "No", f.getType().toString(), f.getDataType().toString()
            ));
        }
        table.addRows(rows);
        System.out.println(table);
    }
}