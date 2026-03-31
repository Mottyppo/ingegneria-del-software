package it.unibs.ingesw.ui;

import it.unibs.ingesw.controller.SystemManager;
import it.unibs.ingesw.model.Category;
import it.unibs.ingesw.model.Configurator;
import it.unibs.ingesw.model.DataType;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.FieldType;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Coordinates application use cases and delegates all terminal I/O to {@link UserInteraction}.
 *
 * <p>The class contains the interaction flow logic of the application while keeping
 * display and input operations separated in the dedicated user interaction adapter.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Authenticates configurators and manages first-access credential updates.</li>
 *   <li>Drives base, common, and specific field management workflows.</li>
 *   <li>Handles category CRUD operations through menu-based interactions.</li>
 *   <li>Handles proposal creation, optional publication, and board visualization.</li>
 * </ul>
 */
public class ConfiguratorInteractionManager {

    private final SystemManager manager;
    private final UserInteraction interaction;

    /**
     * Creates a coordinator bound to the given system manager.
     *
     * @param manager The system manager that executes business operations.
     */
    public ConfiguratorInteractionManager(SystemManager manager) {
        this.manager = manager;
        this.interaction = new UserInteraction();
    }

    /**
     * Starts the complete interactive flow of the configurator backend.
     */
    public void start() {
        interaction.clearConsole();
        interaction.printBanner();
        interaction.printApplicationTitle();

        Configurator configurator = login();
        if (configurator == null) {
            return;
        }

        if (configurator.isFirstAccess()) {
            manageFirstAccess(configurator);
        }

        if (!manager.areBaseFieldsSet()) {
            interaction.printFirstConfigurationNotice();
            setupBaseFields();
        }

        manager.refreshProposalLifecycle();
        mainMenu();
    }

    /**
     * Runs the login prompt loop until valid credentials are provided.
     *
     * @return The authenticated configurator.
     */
    private Configurator login() {
        while (true) {
            String username = interaction.readLoginUsername();
            String password = interaction.readLoginPassword();
            Configurator configurator = manager.authenticateConfigurator(username, password);
            if (configurator != null) {
                return configurator;
            }
            interaction.printInvalidCredentials();
        }
    }

    /**
     * Handles first-access credential update for the authenticated configurator.
     *
     * @param configurator The configurator that must update credentials.
     */
    private void manageFirstAccess(Configurator configurator) {
        interaction.printFirstAccessMessage();

        while (true) {
            String newUsername = interaction.readNewUsername();
            String newPassword = interaction.readNewPassword();
            if (manager.updateCredentials(configurator, newUsername, newPassword)) {
                interaction.printCredentialsUpdated();
                return;
            }
            interaction.printUsernameAlreadyUsed();
        }
    }

    /**
     * Guides the user through initial base field configuration.
     */
    private void setupBaseFields() {
        List<Field> fields = new ArrayList<>();
        Set<String> localNames = new HashSet<>();

        for (UserInteraction.BaseFieldTemplate baseField : interaction.baseFieldTemplates()) {
            DataType dataType = interaction.chooseBaseFieldDataType(baseField.name());
            if (dataType == null) {
                interaction.printOperationCancelled();
                return;
            }
            fields.add(new Field(baseField.name(), baseField.description(), true, FieldType.BASE, dataType));
            localNames.add(baseField.name().toLowerCase());
        }

        interaction.printBaseFieldDataTypesInserted();

        while (interaction.askAddAnotherBaseField()) {
            Field customField = promptForNewField(FieldType.BASE, true, null, localNames);
            if (customField != null) {
                fields.add(customField);
                localNames.add(customField.getName().toLowerCase());
            }
        }

        executeAndPrint(
            manager.setBaseFields(fields),
            interaction.baseFieldsSetSuccessMessage(),
            interaction.baseFieldsSetFailureMessage()
        );
    }

    /**
     * Displays and handles the top-level menu loop.
     */
    private void mainMenu() {
        boolean exit = false;

        while (!exit) {
            int choice = interaction.chooseMainMenu(manager.areBaseFieldsSet());
            switch (choice) {
                case 0 -> exit = true;
                case 1 -> {
                    if (manager.areBaseFieldsSet()) {
                        interaction.showFields(interaction.baseFieldsTitle(), manager.getBaseFields());
                    } else {
                        setupBaseFields();
                    }
                }
                case 2 -> {
                    if (requireBaseFields()) {
                        commonFieldsMenu();
                    }
                }
                case 3 -> {
                    if (requireBaseFields()) {
                        categoriesMenu();
                    }
                }
                case 4 -> {
                    if (requireBaseFields()) {
                        proposalsMenu();
                    }
                }
                case 5 -> {
                    if (requireBaseFields()) {
                        showFullCategories();
                    }
                }
                case 6 -> {
                    if (requireBaseFields()) {
                        showArchive();
                    }
                }
                default -> interaction.printInvalidChoice();
            }
        }
    }

    /**
     * Ensures base fields are configured before allowing dependent operations.
     *
     * @return {@code true} if base fields are available, {@code false} otherwise.
     */
    private boolean requireBaseFields() {
        if (!manager.areBaseFieldsSet()) {
            interaction.printBaseFieldsRequired();
            return false;
        }
        return true;
    }

    /**
     * Displays and handles the common fields management menu loop.
     */
    private void commonFieldsMenu() {
        boolean exit = false;

        while (!exit) {
            int choice = interaction.chooseCommonFieldsMenu();
            switch (choice) {
                case 0 -> exit = true;
                case 1 -> {
                    Field field = promptForNewField(FieldType.COMMON, false, null, null);
                    if (field != null) {
                        executeAndPrint(
                            manager.addCommonField(field),
                            interaction.commonFieldAddSuccessMessage(),
                            interaction.commonFieldAddFailureMessage()
                        );
                    }
                }
                case 2 -> {
                    int index = interaction.chooseIndex(
                        manager.getCommonFields(),
                        interaction.commonFieldToRemoveTitle(),
                        Field::getName
                    );
                    if (index >= 0) {
                        executeAndPrint(
                            manager.removeCommonField(index),
                            interaction.commonFieldRemoveSuccessMessage(),
                            interaction.commonFieldRemoveFailureMessage()
                        );
                    }
                }
                case 3 -> {
                    int index = interaction.chooseIndex(
                        manager.getCommonFields(),
                        interaction.commonFieldToEditTitle(),
                        Field::getName
                    );
                    if (index >= 0) {
                        executeAndPrint(
                            manager.toggleMandatorinessCommonField(index),
                            interaction.commonFieldToggleSuccessMessage(),
                            interaction.commonFieldToggleFailureMessage()
                        );
                    }
                }
                case 4 -> interaction.showFields(interaction.commonFieldsTitle(), manager.getCommonFields());
                default -> interaction.printInvalidChoice();
            }
        }
    }

    /**
     * Displays and handles the categories management menu loop.
     */
    private void categoriesMenu() {
        boolean exit = false;

        while (!exit) {
            int choice = interaction.chooseCategoriesMenu();
            switch (choice) {
                case 0 -> exit = true;
                case 1 -> addCategory();
                case 2 -> {
                    int index = interaction.chooseIndex(
                        manager.getCategories(),
                        interaction.categoryToRemoveTitle(),
                        Category::getName
                    );
                    if (index >= 0) {
                        executeAndPrint(
                            manager.removeCategory(index),
                            interaction.categoryRemoveSuccessMessage(),
                            interaction.categoryRemoveFailureMessage()
                        );
                    }
                }
                case 3 -> manageSpecificFields();
                case 4 -> showFullCategories();
                default -> interaction.printInvalidChoice();
            }
        }
    }

    /**
     * Prompts and executes category creation.
     */
    private void addCategory() {
        String name = interaction.readCategoryName();
        if (!manager.isCategoryNameAvailable(name)) {
            interaction.printCategoryNameAlreadyUsed();
            return;
        }

        List<Field> specificFields = new ArrayList<>();
        Set<String> specificNames = new HashSet<>();

        while (interaction.askAddSpecificField()) {
            Field field = promptForNewField(FieldType.SPECIFIC, false, null, specificNames);
            if (field != null) {
                specificFields.add(field);
                specificNames.add(field.getName().toLowerCase());
            }
        }

        executeAndPrint(
            manager.addCategory(name, specificFields),
            interaction.categoryAddSuccessMessage(),
            interaction.categoryAddFailureMessage()
        );
    }

    /**
     * Displays and handles specific field management for a selected category.
     */
    private void manageSpecificFields() {
        int categoryIndex = interaction.chooseIndex(
            manager.getCategories(),
            interaction.categorySelectionTitle(),
            Category::getName
        );

        if (categoryIndex < 0) {
            return;
        }

        Category category = manager.getCategories().get(categoryIndex);
        boolean exit = false;

        while (!exit) {
            int choice = interaction.chooseSpecificFieldsMenu(category.getName());
            switch (choice) {
                case 0 -> exit = true;
                case 1 -> {
                    Field field = promptForNewField(FieldType.SPECIFIC, false, category, null);
                    if (field != null) {
                        executeAndPrint(
                            manager.addSpecificField(categoryIndex, field),
                            interaction.specificFieldAddSuccessMessage(),
                            interaction.specificFieldAddFailureMessage()
                        );
                    }
                }
                case 2 -> {
                    int fieldIndex = interaction.chooseIndex(
                        category.getSpecificFields(),
                        interaction.specificFieldToRemoveTitle(),
                        Field::getName
                    );
                    if (fieldIndex >= 0) {
                        executeAndPrint(
                            manager.removeSpecificField(categoryIndex, fieldIndex),
                            interaction.specificFieldRemoveSuccessMessage(),
                            interaction.specificFieldRemoveFailureMessage()
                        );
                    }
                }
                case 3 -> {
                    int fieldIndex = interaction.chooseIndex(
                        category.getSpecificFields(),
                        interaction.specificFieldToEditTitle(),
                        Field::getName
                    );
                    if (fieldIndex >= 0) {
                        executeAndPrint(
                            manager.toggleMandatorinessSpecificField(categoryIndex, fieldIndex),
                            interaction.specificFieldToggleSuccessMessage(),
                            interaction.specificFieldToggleFailureMessage()
                        );
                    }
                }
                case 4 -> interaction.showFields(
                    interaction.specificFieldsTitle(category.getName()),
                    category.getSpecificFields()
                );
                default -> interaction.printInvalidChoice();
            }
        }
    }

    /**
     * Displays and handles proposal workflows.
     */
    private void proposalsMenu() {
        boolean exit = false;

        while (!exit) {
            int choice = interaction.chooseProposalsMenu();
            switch (choice) {
                case 0 -> exit = true;
                case 1 -> createProposal();
                case 2 -> publishValidProposal();
                case 3 -> interaction.showBoardByCategory(manager.getBoardByCategory());
                default -> interaction.printInvalidChoice();
            }
        }
    }

    /**
     * Guides the user in creating a proposal and optionally publishing it.
     */
    private void createProposal() {
        List<Category> categories = manager.getCategories();
        if (categories.isEmpty()) {
            interaction.printNoCategoryAvailable();
            return;
        }

        int categoryIndex = interaction.chooseIndex(
            categories,
            interaction.categorySelectionTitle(),
            Category::getName
        );
        if (categoryIndex < 0) {
            return;
        }

        Category category = categories.get(categoryIndex);
        List<Field> fields = manager.getSharedFieldsForCategory(category);
        Map<String, String> rawValues = new LinkedHashMap<>();

        for (Field field : fields) {
            if (!field.isMandatory() && !interaction.askFillOptionalField(field.getName())) {
                continue;
            }
            rawValues.put(field.getName(), interaction.readFieldValue(field));
        }

        Proposal proposal = manager.createProposal(categoryIndex, rawValues);
        if (proposal == null) {
            interaction.printProposalInvalid();
            return;
        }

        if (proposal.getCurrentStatus() != ProposalStatus.VALID) {
            interaction.printProposalCreatedNotValid(proposal.getId());
            return;
        }

        interaction.printProposalValid(proposal.getId());
        if (!interaction.askPublishProposal()) {
            interaction.printProposalDiscarded();
            return;
        }

        executeAndPrint(
            manager.publishProposal(proposal),
            interaction.proposalPublishSuccessMessage(),
            interaction.proposalPublishFailureMessage()
        );
    }

    /**
     * Allows the configurator to choose one valid proposal and publish it later.
     */
    private void publishValidProposal() {
        List<Proposal> validProposals = manager.getValidProposals();
        int index = interaction.chooseValidProposalToPublish(validProposals);
        if (index < 0) {
            return;
        }

        Proposal selected = validProposals.get(index);
        executeAndPrint(
            manager.publishProposal(selected),
            interaction.proposalPublishSuccessMessage(),
            interaction.proposalPublishFailureMessage()
        );
    }

    /**
     * Prompts the user for a new field and validates local/global name uniqueness.
     *
     * @param type                  The field type to create.
     * @param forceMandatory        Whether the field must be mandatory without user prompt.
     * @param contextCategory       The category context for specific field uniqueness checks.
     * @param localReservedNames    Optional local names reserved during batch creation.
     * @return A newly created field, or {@code null} if validation fails or operation is canceled.
     */
    private Field promptForNewField(
        FieldType type,
        boolean forceMandatory,
        Category contextCategory,
        Set<String> localReservedNames
    ) {
        String name = interaction.readFieldName();
        boolean takenGlobally = !manager.isFieldNameAvailableForCategory(name, contextCategory);
        boolean takenLocally = localReservedNames != null
            && localReservedNames.contains(name.toLowerCase());

        if (takenGlobally || takenLocally) {
            interaction.printFieldNameAlreadyUsed();
            return null;
        }

        String description = interaction.readFieldDescription();
        boolean mandatory = forceMandatory || interaction.askFieldMandatory();
        DataType dataType = interaction.chooseFieldDataType();

        if (dataType == null) {
            interaction.printOperationCancelled();
            return null;
        }

        return new Field(name, description, mandatory, type, dataType);
    }

    /**
     * Prints success or failure output for a completed operation.
     *
     * @param result            Operation outcome.
     * @param successMessage    Message for successful operations.
     * @param failMessage       Message for failed operations.
     */
    private void executeAndPrint(boolean result, String successMessage, String failMessage) {
        interaction.printOperationResult(result, successMessage, failMessage);
    }

    /**
     * Displays all categories with their combined shared fields.
     */
    private void showFullCategories() {
        List<Category> categories = manager.getCategories();
        if (categories.isEmpty()) {
            interaction.printNoCategoryAvailable();
            return;
        }

        for (Category category : categories) {
            interaction.showCategoryFields(category.getName(), manager.getSharedFieldsForCategory(category));
        }
    }

    /**
     * Displays the full archive, including status history and subscribers.
     */
    private void showArchive() {
        interaction.showArchive(manager.getArchivedProposals());
    }
}
