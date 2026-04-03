package it.unibs.ingesw.ui;

import it.unibs.ingesw.console.format.*;
import it.unibs.ingesw.console.input.InputData;
import it.unibs.ingesw.console.menu.Menu;
import it.unibs.ingesw.console.table.CommandLineTable;
import it.unibs.ingesw.service.BatchImportReport;
import it.unibs.ingesw.model.DataType;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.StateLog;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles command-line interactions dedicated to configurators.
 */
public class ConfiguratorInteraction extends UserInteraction {
    private static final String BACKEND_TITLE = "=== Backend Configuratore ===";
    private static final String FIRST_CONFIGURATION_NOTICE = "Prima configurazione: impostazione campi base.";

    private static final String LOGIN_USERNAME_PROMPT = "Username: ";
    private static final String LOGIN_PASSWORD_PROMPT = "Password: ";
    private static final String NEW_USERNAME_PROMPT = "Nuovo username: ";
    private static final String NEW_PASSWORD_PROMPT = "Nuova password: ";
    private static final String CATEGORY_NAME_PROMPT = "Nome categoria: ";
    private static final String FIELD_NAME_PROMPT = "Nome campo: ";
    private static final String FIELD_DESCRIPTION_PROMPT = "Descrizione: ";

    private static final String INVALID_CREDENTIALS_MESSAGE = "Credenziali non valide. Riprova.";
    private static final String FIRST_ACCESS_MESSAGE = "Primo accesso: scegli le tue credenziali personali.";
    private static final String CREDENTIALS_UPDATED_MESSAGE = "Credenziali aggiornate con successo.";
    private static final String USERNAME_ALREADY_USED_MESSAGE = "Username gia' in uso. Riprova.";
    private static final String BASE_TYPES_INSERTED_MESSAGE = "Tipi di dato dei campi base inseriti.";
    private static final String OPERATION_CANCELLED_MESSAGE = "Operazione annullata.";
    private static final String INVALID_CHOICE_MESSAGE = "Scelta non valida.";
    private static final String BASE_FIELDS_REQUIRED_MESSAGE = "Prima imposta i campi base.";
    private static final String NO_CATEGORY_AVAILABLE_MESSAGE = "Nessuna categoria presente.";
    private static final String NO_FIELD_AVAILABLE_MESSAGE = "Nessun campo presente.";
    private static final String CATEGORY_NAME_ALREADY_USED_MESSAGE = "Nome categoria gia' in uso.";
    private static final String FIELD_NAME_ALREADY_USED_MESSAGE = "Nome campo gia' in uso.";
    private static final String BASE_FIELDS_SET_SUCCESS_MESSAGE = "Campi base impostati correttamente.";
    private static final String BASE_FIELDS_SET_FAILURE_MESSAGE = "I campi base risultano gia' impostati.";
    private static final String COMMON_FIELD_ADD_SUCCESS_MESSAGE = "Campo comune aggiunto.";
    private static final String COMMON_FIELD_ADD_FAILURE_MESSAGE = "Impossibile aggiungere il campo comune.";
    private static final String COMMON_FIELD_REMOVE_SUCCESS_MESSAGE = "Campo comune rimosso.";
    private static final String COMMON_FIELD_REMOVE_FAILURE_MESSAGE = "Impossibile rimuovere il campo comune.";
    private static final String FIELD_TOGGLE_SUCCESS_MESSAGE = "Obbligatorieta' aggiornata.";
    private static final String COMMON_FIELD_TOGGLE_FAILURE_MESSAGE = "Impossibile aggiornare il campo comune.";
    private static final String CATEGORY_ADD_SUCCESS_MESSAGE = "Categoria aggiunta.";
    private static final String CATEGORY_ADD_FAILURE_MESSAGE = "Impossibile aggiungere la categoria.";
    private static final String CATEGORY_REMOVE_SUCCESS_MESSAGE = "Categoria rimossa.";
    private static final String CATEGORY_REMOVE_FAILURE_MESSAGE = "Impossibile rimuovere la categoria.";
    private static final String SPECIFIC_FIELD_ADD_SUCCESS_MESSAGE = "Campo specifico aggiunto.";
    private static final String SPECIFIC_FIELD_ADD_FAILURE_MESSAGE = "Impossibile aggiungere il campo specifico.";
    private static final String SPECIFIC_FIELD_REMOVE_SUCCESS_MESSAGE = "Campo specifico rimosso.";
    private static final String SPECIFIC_FIELD_REMOVE_FAILURE_MESSAGE = "Impossibile rimuovere il campo specifico.";
    private static final String SPECIFIC_FIELD_TOGGLE_FAILURE_MESSAGE = "Impossibile aggiornare il campo specifico.";
    private static final String PROPOSAL_INVALID_MESSAGE = "Proposta non valida: controlla campi e vincoli.";
    private static final String PROPOSAL_VALID_MESSAGE_TEMPLATE = "Proposta valida creata e salvata (ID: %d).";
    private static final String PROPOSAL_DISCARDED_MESSAGE = "Proposta valida non pubblicata: resta valida in archivio.";
    private static final String PROPOSAL_CREATED_NOT_VALID_TEMPLATE = "Proposta #%d creata e salvata in archivio ma non valida.";
    private static final String PROPOSAL_PUBLISH_SUCCESS_MESSAGE = "Proposta pubblicata in bacheca.";
    private static final String PROPOSAL_PUBLISH_FAILURE_MESSAGE = "Impossibile pubblicare la proposta.";
    private static final String PROPOSAL_WITHDRAW_SUCCESS_MESSAGE = "Proposta ritirata.";
    private static final String PROPOSAL_WITHDRAW_FAILURE_MESSAGE = "Impossibile ritirare la proposta.";
    private static final String NO_ARCHIVED_PROPOSALS_MESSAGE = "Archivio proposte vuoto.";
    private static final String DATE_FORMAT_ERROR_MESSAGE = "Formato data non valido. Usa GG/MM/AAAA.";
    private static final String TIME_FORMAT_ERROR_MESSAGE = "Formato ora non valido. Usa HH:MM.";
    private static final String DECIMAL_FORMAT_ERROR_MESSAGE = "Formato decimale non valido. Usa es. 12,50 oppure 12.50.";
    private static final String BOOLEAN_VALUE_PROMPT_TEMPLATE = "\"%s\" vale Si";
    private static final String FIELD_VALUE_PROMPT_TEMPLATE = "Valore \"%s\" (%s): ";
    private static final String ASK_OPTIONAL_FIELD_TEMPLATE = "Vuoi compilare il campo facoltativo \"%s\"";
    private static final String ASK_PUBLISH_PROPOSAL = "Vuoi pubblicare in bacheca la proposta valida";

    private static final String MAIN_MENU_TITLE = "Menu Configuratore";
    private static final String MAIN_MENU_SHOW_BASE = "Visualizza campi base";
    private static final String MAIN_MENU_SHOW_BASE_TITLE = "Campi Base";
    private static final String MAIN_MENU_SET_BASE = "Imposta campi base";
    private static final String MAIN_MENU_MANAGE_COMMON = "Gestisci campi comuni";
    private static final String MAIN_MENU_MANAGE_CATEGORIES = "Gestisci categorie";
    private static final String MAIN_MENU_MANAGE_PROPOSALS = "Gestisci proposte";
    private static final String MAIN_MENU_IMPORT_BATCH = "Importa batch JSON";
    private static final String MAIN_MENU_SHOW_CATEGORIES = "Visualizza categorie e campi";
    private static final String MAIN_MENU_SHOW_ARCHIVE = "Visualizza archivio proposte";

    private static final String COMMON_FIELDS_MENU_TITLE = "Campi Comuni";
    private static final String COMMON_FIELDS_ADD = "Aggiungi campo comune";
    private static final String COMMON_FIELDS_REMOVE = "Rimuovi campo comune";
    private static final String COMMON_FIELDS_TOGGLE = "Cambia obbligatorieta'";
    private static final String COMMON_FIELDS_SHOW = "Visualizza campi comuni";

    private static final String CATEGORIES_MENU_TITLE = "Categorie";
    private static final String CATEGORIES_ADD = "Aggiungi categoria";
    private static final String CATEGORIES_REMOVE = "Rimuovi categoria";
    private static final String CATEGORIES_MANAGE_SPECIFICS = "Gestisci campi specifici";
    private static final String CATEGORIES_SHOW = "Visualizza categorie e campi";

    private static final String SPECIFIC_FIELDS_MENU_TITLE_TEMPLATE = "Campi specifici: %s";
    private static final String SPECIFIC_FIELDS_ADD = "Aggiungi campo specifico";
    private static final String SPECIFIC_FIELDS_REMOVE = "Rimuovi campo specifico";
    private static final String SPECIFIC_FIELDS_TOGGLE = "Cambia obbligatorieta'";
    private static final String SPECIFIC_FIELDS_SHOW = "Visualizza campi specifici";

    private static final String PROPOSALS_MENU_TITLE = "Proposte";
    private static final String PROPOSALS_CREATE = "Crea proposta";
    private static final String PROPOSALS_PUBLISH_VALID = "Pubblica proposta valida";
    private static final String PROPOSALS_WITHDRAW = "Ritira proposta aperta o confermata";
    private static final String PROPOSALS_SHOW_BOARD = "Visualizza bacheca per categoria";

    private static final String BATCH_IMPORT_MENU_TITLE = "Importa Batch JSON";
    private static final String BATCH_IMPORT_FIELDS = "Importa campi";
    private static final String BATCH_IMPORT_CATEGORIES = "Importa categorie";
    private static final String BATCH_IMPORT_PROPOSALS = "Importa proposte";
    private static final String BATCH_IMPORT_PATH_PROMPT = "Percorso file JSON: ";
    private static final String BATCH_IMPORT_REPORT_TITLE_TEMPLATE = "== Report Import %s ==";
    private static final String BATCH_IMPORT_SOURCE_TEMPLATE = "File: %s";
    private static final String BATCH_IMPORT_TOTAL_TEMPLATE = "Totale elementi: %d";
    private static final String BATCH_IMPORT_IMPORTED_TEMPLATE = "Importati: %d";
    private static final String BATCH_IMPORT_DISCARDED_TEMPLATE = "Scartati: %d";
    private static final String BATCH_IMPORT_NOTES_LABEL = "Note:";
    private static final String BATCH_IMPORT_ISSUES_LABEL = "Segnalazioni:";
    private static final String BATCH_IMPORT_ENTRY_TEMPLATE = "- %s";

    private static final String CHOOSE_COMMON_TO_REMOVE = "Seleziona il campo comune da rimuovere";
    private static final String CHOOSE_COMMON_TO_EDIT = "Seleziona il campo comune da modificare";
    private static final String CHOOSE_CATEGORY_TO_REMOVE = "Seleziona la categoria da rimuovere";
    private static final String CHOOSE_CATEGORY = "Seleziona la categoria";
    private static final String CHOOSE_VALID_PROPOSAL = "Seleziona la proposta valida da pubblicare";
    private static final String CHOOSE_WITHDRAWABLE_PROPOSAL = "Seleziona la proposta da ritirare";
    private static final String CHOOSE_SPECIFIC_TO_REMOVE = "Seleziona il campo da rimuovere";
    private static final String CHOOSE_SPECIFIC_TO_EDIT = "Seleziona il campo da modificare";

    private static final String ASK_ADD_BASE_FIELD = "Vuoi aggiungere un altro campo base";
    private static final String ASK_ADD_SPECIFIC_FIELD = "Vuoi aggiungere un campo specifico";
    private static final String ASK_FIELD_MANDATORY = "Il campo e' obbligatorio";

    private static final String CHOOSE_DATA_TYPE_BASE_TEMPLATE = "Scegli il tipo di dato per il campo base \"%s\"";
    private static final String CHOOSE_DATA_TYPE_FIELD = "Scegli tipo dato per il campo";

    private static final String FULL_CATEGORY_TITLE_TEMPLATE = "Categoria: %s";
    private static final String FIELD_TABLE_TITLE_TEMPLATE = "== %s ==";
    private static final String ALL_CATEGORY_FIELDS_TITLE = "Campi";
    private static final String ARCHIVE_TITLE = "== Archivio Proposte ==";
    private static final String DATE_TYPE_LABEL_TEMPLATE = "%s, formato GG/MM/AAAA";
    private static final String TIME_TYPE_LABEL_TEMPLATE = "%s, formato HH:MM";
    private static final String DECIMAL_TYPE_LABEL_TEMPLATE = "%s, es. 12,50 o 12.50";
    private static final String ARCHIVE_PROPOSAL_TEMPLATE = "- Proposta #%d | Categoria: %s | Stato corrente: %s";
    private static final String ARCHIVE_SUBSCRIBERS_TEMPLATE = "  Iscritti: %s";
    private static final String ARCHIVE_FIELDS_LABEL = "  Campi:";
    private static final String ARCHIVE_FIELD_ENTRY_TEMPLATE = "  - %s: %s";
    private static final String ARCHIVE_STATUS_HISTORY_LABEL = "  Storico stati:";
    private static final String ARCHIVE_STATUS_ENTRY_TEMPLATE = "  - %s (%s)";

    private static final String TABLE_HEADER_INDEX = "N";
    private static final String TABLE_HEADER_NAME = "Nome";
    private static final String TABLE_HEADER_DESCRIPTION = "Descrizione";
    private static final String TABLE_HEADER_MANDATORY = "Obblig.";
    private static final String TABLE_HEADER_FIELD_TYPE = "Tipo";
    private static final String TABLE_HEADER_DATA_TYPE = "Dato";
    private static final String TABLE_MANDATORY_YES = "Si";
    private static final String TABLE_MANDATORY_NO = "No";

    private static final String BASE_FIELD_TITLE_NAME = "Titolo";
    private static final String BASE_FIELD_TITLE_DESCRIPTION = "nome di fantasia (esplicativo) attribuito all'iniziativa";
    private static final String BASE_FIELD_PARTICIPANTS_NAME = "Numero di partecipanti";
    private static final String BASE_FIELD_PARTICIPANTS_DESCRIPTION = "numero di persone da coinvolgere nell'iniziativa";
    private static final String BASE_FIELD_DEADLINE_NAME = "Termine ultimo di iscrizione";
    private static final String BASE_FIELD_DEADLINE_DESCRIPTION = "ultimo giorno utile per iscriversi all'iniziativa";
    private static final String BASE_FIELD_PLACE_NAME = "Luogo";
    private static final String BASE_FIELD_PLACE_DESCRIPTION = "indirizzo del luogo che ospitera' l'iniziativa";
    private static final String BASE_FIELD_START_DATE_NAME = "Data";
    private static final String BASE_FIELD_START_DATE_DESCRIPTION = "data di inizio dell'iniziativa";
    private static final String BASE_FIELD_TIME_NAME = "Ora";
    private static final String BASE_FIELD_TIME_DESCRIPTION = "ora di ritrovo dei partecipanti";
    private static final String BASE_FIELD_FEE_NAME = "Quota individuale";
    private static final String BASE_FIELD_FEE_DESCRIPTION = "spesa individuale stimata per l'iniziativa";
    private static final String BASE_FIELD_END_DATE_NAME = "Data conclusiva";
    private static final String BASE_FIELD_END_DATE_DESCRIPTION = "data di conclusione dell'iniziativa";
    private static final String USER_DATE_PATTERN = "dd/MM/uuuu";
    private static final String USER_TIME_PATTERN = "HH:mm";
    private static final DateTimeFormatter USER_DATE_FORMATTER = DateTimeFormatter
            .ofPattern(USER_DATE_PATTERN)
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter USER_TIME_FORMATTER = DateTimeFormatter
            .ofPattern(USER_TIME_PATTERN)
            .withResolverStyle(ResolverStyle.STRICT);

    private static final List<BaseFieldTemplate> BASE_FIELDS = List.of(
            new BaseFieldTemplate(BASE_FIELD_TITLE_NAME, BASE_FIELD_TITLE_DESCRIPTION),
            new BaseFieldTemplate(BASE_FIELD_PARTICIPANTS_NAME, BASE_FIELD_PARTICIPANTS_DESCRIPTION),
            new BaseFieldTemplate(BASE_FIELD_DEADLINE_NAME, BASE_FIELD_DEADLINE_DESCRIPTION),
            new BaseFieldTemplate(BASE_FIELD_PLACE_NAME, BASE_FIELD_PLACE_DESCRIPTION),
            new BaseFieldTemplate(BASE_FIELD_START_DATE_NAME, BASE_FIELD_START_DATE_DESCRIPTION),
            new BaseFieldTemplate(BASE_FIELD_END_DATE_NAME, BASE_FIELD_END_DATE_DESCRIPTION),
            new BaseFieldTemplate(BASE_FIELD_TIME_NAME, BASE_FIELD_TIME_DESCRIPTION),
            new BaseFieldTemplate(BASE_FIELD_FEE_NAME, BASE_FIELD_FEE_DESCRIPTION)
    );

    private static final List<String> COMMON_FIELDS_MENU_ENTRIES = List.of(
            COMMON_FIELDS_ADD,
            COMMON_FIELDS_REMOVE,
            COMMON_FIELDS_TOGGLE,
            COMMON_FIELDS_SHOW
    );

    private static final List<String> CATEGORIES_MENU_ENTRIES = List.of(
            CATEGORIES_ADD,
            CATEGORIES_REMOVE,
            CATEGORIES_MANAGE_SPECIFICS,
            CATEGORIES_SHOW
    );

    private static final List<String> SPECIFIC_FIELDS_MENU_ENTRIES = List.of(
            SPECIFIC_FIELDS_ADD,
            SPECIFIC_FIELDS_REMOVE,
            SPECIFIC_FIELDS_TOGGLE,
            SPECIFIC_FIELDS_SHOW
    );

    private static final List<String> PROPOSALS_MENU_ENTRIES = List.of(
            PROPOSALS_CREATE,
            PROPOSALS_PUBLISH_VALID,
            PROPOSALS_WITHDRAW,
            PROPOSALS_SHOW_BOARD
    );

    private static final List<String> BATCH_IMPORT_MENU_ENTRIES = List.of(
            BATCH_IMPORT_FIELDS,
            BATCH_IMPORT_CATEGORIES,
            BATCH_IMPORT_PROPOSALS
    );

    public void printBackEndTitle() {
        printInfo(FormatStrings.addFormat(BACKEND_TITLE, AnsiColors.BLUE, AnsiWeights.BOLD, AnsiDecorations.UNDERLINE));
    }

    public void printFirstConfigurationNotice() {
        printInfo(FIRST_CONFIGURATION_NOTICE);
    }

    public String readLoginUsername() {
        return InputData.readNonEmptyString(LOGIN_USERNAME_PROMPT, true).trim();
    }

    public String readLoginPassword() {
        return InputData.readNonEmptyString(LOGIN_PASSWORD_PROMPT, false);
    }

    public void printInvalidCredentials() {
        printError(INVALID_CREDENTIALS_MESSAGE);
    }

    public void printFirstAccessMessage() {
        printInfo(FIRST_ACCESS_MESSAGE);
    }

    public String readNewUsername() {
        return InputData.readNonEmptyString(NEW_USERNAME_PROMPT, true).trim();
    }

    public String readNewPassword() {
        return InputData.readNonEmptyString(NEW_PASSWORD_PROMPT, false);
    }

    public void printCredentialsUpdated() {
        printSuccess(CREDENTIALS_UPDATED_MESSAGE);
    }

    public void printUsernameAlreadyUsed() {
        printError(USERNAME_ALREADY_USED_MESSAGE);
    }

    public List<BaseFieldTemplate> baseFieldTemplates() {
        return BASE_FIELDS;
    }

    public void printBaseFieldDataTypesInserted() {
        printSuccess(BASE_TYPES_INSERTED_MESSAGE);
    }

    public boolean askAddAnotherBaseField() {
        return InputData.readYesOrNo(ASK_ADD_BASE_FIELD);
    }

    public void printOperationCancelled() {
        printCancelled(OPERATION_CANCELLED_MESSAGE);
    }

    public DataType chooseBaseFieldDataType(String fieldName) {
        return chooseDataType(CHOOSE_DATA_TYPE_BASE_TEMPLATE.formatted(fieldName));
    }

    public int chooseMainMenu(boolean baseFieldsSet) {
        List<String> entries = new ArrayList<>();
        entries.add(baseFieldsSet ? MAIN_MENU_SHOW_BASE : MAIN_MENU_SET_BASE);
        entries.add(MAIN_MENU_MANAGE_COMMON);
        entries.add(MAIN_MENU_MANAGE_CATEGORIES);
        entries.add(MAIN_MENU_MANAGE_PROPOSALS);
        entries.add(MAIN_MENU_IMPORT_BATCH);
        entries.add(MAIN_MENU_SHOW_CATEGORIES);
        entries.add(MAIN_MENU_SHOW_ARCHIVE);
        return new Menu(MAIN_MENU_TITLE, entries, true, Alignment.CENTER, true).choose();
    }

    public void printInvalidChoice() {
        printError(INVALID_CHOICE_MESSAGE);
    }

    public void printBaseFieldsRequired() {
        printError(BASE_FIELDS_REQUIRED_MESSAGE);
    }

    public int chooseCommonFieldsMenu() {
        return new Menu(COMMON_FIELDS_MENU_TITLE, COMMON_FIELDS_MENU_ENTRIES, true, Alignment.CENTER, true).choose();
    }

    public int chooseCategoriesMenu() {
        return new Menu(CATEGORIES_MENU_TITLE, CATEGORIES_MENU_ENTRIES, true, Alignment.CENTER, true).choose();
    }

    public int chooseProposalsMenu() {
        return new Menu(PROPOSALS_MENU_TITLE, PROPOSALS_MENU_ENTRIES, true, Alignment.CENTER, true).choose();
    }

    public int chooseBatchImportMenu() {
        return new Menu(BATCH_IMPORT_MENU_TITLE, BATCH_IMPORT_MENU_ENTRIES, true, Alignment.CENTER, true).choose();
    }

    public int chooseSpecificFieldsMenu(String categoryName) {
        String title = SPECIFIC_FIELDS_MENU_TITLE_TEMPLATE.formatted(categoryName);
        return new Menu(title, SPECIFIC_FIELDS_MENU_ENTRIES, true, Alignment.CENTER, true).choose();
    }

    public String readCategoryName() {
        return InputData.readNonEmptyString(CATEGORY_NAME_PROMPT, false).trim();
    }

    public void printCategoryNameAlreadyUsed() {
        printError(CATEGORY_NAME_ALREADY_USED_MESSAGE);
    }

    public String readFieldName() {
        return InputData.readNonEmptyString(FIELD_NAME_PROMPT, false).trim();
    }

    public boolean askAddSpecificField() {
        return InputData.readYesOrNo(ASK_ADD_SPECIFIC_FIELD);
    }

    public void printFieldNameAlreadyUsed() {
        printError(FIELD_NAME_ALREADY_USED_MESSAGE);
    }

    public String readFieldDescription() {
        return InputData.readNonEmptyString(FIELD_DESCRIPTION_PROMPT, false).trim();
    }

    public boolean askFieldMandatory() {
        return InputData.readYesOrNo(ASK_FIELD_MANDATORY);
    }

    public DataType chooseFieldDataType() {
        return chooseDataType(CHOOSE_DATA_TYPE_FIELD);
    }

    public boolean askFillOptionalField(String fieldName) {
        return InputData.readYesOrNo(ASK_OPTIONAL_FIELD_TEMPLATE.formatted(fieldName));
    }

    public String readFieldValue(Field field) {
        String dataTypeLabel = switch (field.getDataType()) {
            case DATE -> DATE_TYPE_LABEL_TEMPLATE.formatted(field.getDataType());
            case TIME -> TIME_TYPE_LABEL_TEMPLATE.formatted(field.getDataType());
            case DECIMAL -> DECIMAL_TYPE_LABEL_TEMPLATE.formatted(field.getDataType());
            default -> field.getDataType().toString();
        };
        String prompt = FIELD_VALUE_PROMPT_TEMPLATE.formatted(field.getName(), dataTypeLabel);
        return switch (field.getDataType()) {
            case STRING -> InputData.readNonEmptyString(prompt, false).trim();
            case INTEGER -> Integer.toString(InputData.readInteger(prompt));
            case DECIMAL -> readDecimal(prompt);
            case DATE -> readDate(prompt);
            case TIME -> readTime(prompt);
            case BOOLEAN -> Boolean.toString(InputData.readYesOrNo(BOOLEAN_VALUE_PROMPT_TEMPLATE.formatted(field.getName())));
        };
    }

    public boolean askPublishProposal() {
        return InputData.readYesOrNo(ASK_PUBLISH_PROPOSAL);
    }

    public String readBatchImportPath() {
        return InputData.readNonEmptyString(BATCH_IMPORT_PATH_PROMPT, false).trim();
    }

    public void printProposalInvalid() {
        printError(PROPOSAL_INVALID_MESSAGE);
    }

    public void printProposalValid(int proposalId) {
        printSuccess(PROPOSAL_VALID_MESSAGE_TEMPLATE.formatted(proposalId));
    }

    public void printProposalDiscarded() {
        printCancelled(PROPOSAL_DISCARDED_MESSAGE);
    }

    public void printProposalCreatedNotValid(int proposalId) {
        printCancelled(PROPOSAL_CREATED_NOT_VALID_TEMPLATE.formatted(proposalId));
    }

    public void printOperationResult(boolean result, String successMessage, String failMessage) {
        if (result) {
            printSuccess(successMessage);
            return;
        }
        printError(failMessage);
    }

    public void printNoCategoryAvailable() {
        printCancelled(NO_CATEGORY_AVAILABLE_MESSAGE);
    }

    public void showCategoryFields(String categoryName, List<Field> fields) {
        printInfo(FULL_CATEGORY_TITLE_TEMPLATE.formatted(categoryName));
        showFields(ALL_CATEGORY_FIELDS_TITLE, fields);
    }

    public void showFields(String title, List<Field> fields) {
        printInfo(FIELD_TABLE_TITLE_TEMPLATE.formatted(title));
        if (fields == null || fields.isEmpty()) {
            printCancelled(NO_FIELD_AVAILABLE_MESSAGE);
            return;
        }

        CommandLineTable table = new CommandLineTable();
        table.setShowVLines(true);
        table.setCellsAlignment(Alignment.LEFT);
        table.addHeaders(List.of(
                TABLE_HEADER_INDEX,
                TABLE_HEADER_NAME,
                TABLE_HEADER_DESCRIPTION,
                TABLE_HEADER_MANDATORY,
                TABLE_HEADER_FIELD_TYPE,
                TABLE_HEADER_DATA_TYPE
        ));

        List<List<String>> rows = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            Field currentField = fields.get(i);
            rows.add(List.of(
                    String.valueOf(i + 1),
                    currentField.getName(),
                    currentField.getDescription(),
                    currentField.isMandatory() ? TABLE_MANDATORY_YES : TABLE_MANDATORY_NO,
                    currentField.getType().toString(),
                    currentField.getDataType().toString()
            ));
        }

        table.addRows(rows);
        System.out.println(table);
    }

    public int chooseValidProposalToPublish(List<Proposal> validProposals) {
        return chooseIndex(
                validProposals,
                CHOOSE_VALID_PROPOSAL,
                this::summarizeProposalForSelection
        );
    }

    public int chooseWithdrawableProposal(List<Proposal> proposals) {
        return chooseIndex(
                proposals,
                CHOOSE_WITHDRAWABLE_PROPOSAL,
                this::summarizeProposalForSelection
        );
    }

    public String commonFieldToRemoveTitle() {
        return CHOOSE_COMMON_TO_REMOVE;
    }

    public String commonFieldToEditTitle() {
        return CHOOSE_COMMON_TO_EDIT;
    }

    public String categoryToRemoveTitle() {
        return CHOOSE_CATEGORY_TO_REMOVE;
    }

    public String categorySelectionTitle() {
        return CHOOSE_CATEGORY;
    }

    public String specificFieldToRemoveTitle() {
        return CHOOSE_SPECIFIC_TO_REMOVE;
    }

    public String specificFieldToEditTitle() {
        return CHOOSE_SPECIFIC_TO_EDIT;
    }

    public String baseFieldsTitle() {
        return MAIN_MENU_SHOW_BASE_TITLE;
    }

    public String commonFieldsTitle() {
        return COMMON_FIELDS_MENU_TITLE;
    }

    public String specificFieldsTitle(String categoryName) {
        return SPECIFIC_FIELDS_MENU_TITLE_TEMPLATE.formatted(categoryName);
    }

    public String baseFieldsSetSuccessMessage() {
        return BASE_FIELDS_SET_SUCCESS_MESSAGE;
    }

    public String baseFieldsSetFailureMessage() {
        return BASE_FIELDS_SET_FAILURE_MESSAGE;
    }

    public String commonFieldAddSuccessMessage() {
        return COMMON_FIELD_ADD_SUCCESS_MESSAGE;
    }

    public String commonFieldAddFailureMessage() {
        return COMMON_FIELD_ADD_FAILURE_MESSAGE;
    }

    public String commonFieldRemoveSuccessMessage() {
        return COMMON_FIELD_REMOVE_SUCCESS_MESSAGE;
    }

    public String commonFieldRemoveFailureMessage() {
        return COMMON_FIELD_REMOVE_FAILURE_MESSAGE;
    }

    public String commonFieldToggleSuccessMessage() {
        return FIELD_TOGGLE_SUCCESS_MESSAGE;
    }

    public String commonFieldToggleFailureMessage() {
        return COMMON_FIELD_TOGGLE_FAILURE_MESSAGE;
    }

    public String categoryAddSuccessMessage() {
        return CATEGORY_ADD_SUCCESS_MESSAGE;
    }

    public String categoryAddFailureMessage() {
        return CATEGORY_ADD_FAILURE_MESSAGE;
    }

    public String categoryRemoveSuccessMessage() {
        return CATEGORY_REMOVE_SUCCESS_MESSAGE;
    }

    public String categoryRemoveFailureMessage() {
        return CATEGORY_REMOVE_FAILURE_MESSAGE;
    }

    public String specificFieldAddSuccessMessage() {
        return SPECIFIC_FIELD_ADD_SUCCESS_MESSAGE;
    }

    public String specificFieldAddFailureMessage() {
        return SPECIFIC_FIELD_ADD_FAILURE_MESSAGE;
    }

    public String specificFieldRemoveSuccessMessage() {
        return SPECIFIC_FIELD_REMOVE_SUCCESS_MESSAGE;
    }

    public String specificFieldRemoveFailureMessage() {
        return SPECIFIC_FIELD_REMOVE_FAILURE_MESSAGE;
    }

    public String specificFieldToggleSuccessMessage() {
        return FIELD_TOGGLE_SUCCESS_MESSAGE;
    }

    public String specificFieldToggleFailureMessage() {
        return SPECIFIC_FIELD_TOGGLE_FAILURE_MESSAGE;
    }

    public String proposalPublishSuccessMessage() {
        return PROPOSAL_PUBLISH_SUCCESS_MESSAGE;
    }

    public String proposalPublishFailureMessage() {
        return PROPOSAL_PUBLISH_FAILURE_MESSAGE;
    }

    public String proposalWithdrawSuccessMessage() {
        return PROPOSAL_WITHDRAW_SUCCESS_MESSAGE;
    }

    public String proposalWithdrawFailureMessage() {
        return PROPOSAL_WITHDRAW_FAILURE_MESSAGE;
    }

    public void showArchive(List<Proposal> proposals) {
        printInfo(ARCHIVE_TITLE);
        if (proposals == null || proposals.isEmpty()) {
            printCancelled(NO_ARCHIVED_PROPOSALS_MESSAGE);
            return;
        }

        for (Proposal proposal : proposals) {
            if (proposal == null) {
                continue;
            }
            System.out.printf(
                    (ARCHIVE_PROPOSAL_TEMPLATE) + "%n", proposal.getId(),
                    proposal.getCategoryName(),
                    proposal.getCurrentStatus()
            );
            System.out.printf((ARCHIVE_SUBSCRIBERS_TEMPLATE) + "%n", proposal.getSubscribers());
            System.out.println(ARCHIVE_FIELDS_LABEL);
            for (Map.Entry<String, String> valueEntry : proposal.getFieldValues().entrySet()) {
                String formattedValue = FormatValues.formatField(
                        proposal,
                        valueEntry.getKey(),
                        valueEntry.getValue()
                );
                System.out.printf((ARCHIVE_FIELD_ENTRY_TEMPLATE) + "%n", valueEntry.getKey(), formattedValue);
            }

            System.out.println(ARCHIVE_STATUS_HISTORY_LABEL);
            for (StateLog stateLog : proposal.getStatusHistory()) {
                String when = FormatValues.formatDateTime(stateLog.getTimestamp());
                System.out.printf((ARCHIVE_STATUS_ENTRY_TEMPLATE) + "%n", stateLog.getStatus(), when);
            }
            System.out.println();
        }
    }

    public void showBatchImportReport(BatchImportReport report) {
        printInfo(BATCH_IMPORT_REPORT_TITLE_TEMPLATE.formatted(report.getImportName()));
        System.out.printf((BATCH_IMPORT_SOURCE_TEMPLATE) + "%n", report.getSourcePath());
        System.out.printf((BATCH_IMPORT_TOTAL_TEMPLATE) + "%n", report.getTotalEntries());
        System.out.printf((BATCH_IMPORT_IMPORTED_TEMPLATE) + "%n", report.getImportedEntries());
        System.out.printf((BATCH_IMPORT_DISCARDED_TEMPLATE) + "%n", report.getDiscardedEntries());

        if (!report.getNotes().isEmpty()) {
            System.out.println(BATCH_IMPORT_NOTES_LABEL);
            for (String note : report.getNotes()) {
                System.out.printf((BATCH_IMPORT_ENTRY_TEMPLATE) + "%n", note);
            }
        }

        if (!report.getIssues().isEmpty()) {
            System.out.println(BATCH_IMPORT_ISSUES_LABEL);
            for (String issue : report.getIssues()) {
                System.out.printf((BATCH_IMPORT_ENTRY_TEMPLATE) + "%n", issue);
            }
        }

        System.out.println();
    }

    private DataType chooseDataType(String title) {
        List<String> entries = Arrays.stream(DataType.values()).map(Enum::toString).collect(Collectors.toList());
        int choice = new Menu(title, entries, true, Alignment.CENTER, true).choose();
        return choice == 0 ? null : DataType.values()[choice - 1];
    }

    private String readDecimal(String prompt) {
        while (true) {
            String raw = InputData.readNonEmptyString(prompt, false).trim();
            String normalized = raw.replace(',', '.');
            try {
                double value = Double.parseDouble(normalized);
                return Double.toString(value);
            } catch (NumberFormatException exception) {
                printError(DECIMAL_FORMAT_ERROR_MESSAGE);
            }
        }
    }

    private String readDate(String prompt) {
        while (true) {
            String raw = InputData.readNonEmptyString(prompt, false).trim();
            try {
                LocalDate parsed = LocalDate.parse(raw, USER_DATE_FORMATTER);
                return parsed.format(USER_DATE_FORMATTER);
            } catch (DateTimeParseException exception) {
                printError(DATE_FORMAT_ERROR_MESSAGE);
            }
        }
    }

    private String readTime(String prompt) {
        while (true) {
            String raw = InputData.readNonEmptyString(prompt, false).trim();
            try {
                LocalTime parsed = LocalTime.parse(raw, USER_TIME_FORMATTER);
                return parsed.format(USER_TIME_FORMATTER);
            } catch (DateTimeParseException exception) {
                printError(TIME_FORMAT_ERROR_MESSAGE);
            }
        }
    }

    public record BaseFieldTemplate(String name, String description) {
    }
}
