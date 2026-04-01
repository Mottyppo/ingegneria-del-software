package it.unibs.ingesw.controller;

import it.unibs.ingesw.application.ApplicationContext;
import it.unibs.ingesw.model.Category;
import it.unibs.ingesw.model.Configurator;
import it.unibs.ingesw.model.DataType;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.FieldType;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;
import it.unibs.ingesw.service.AuthenticationService;
import it.unibs.ingesw.service.ConfigurationService;
import it.unibs.ingesw.service.ProposalLifecycleService;
import it.unibs.ingesw.service.ProposalService;
import it.unibs.ingesw.ui.ConfiguratorInteraction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Coordinates CLI workflows dedicated to configurators.
 *
 * <p>The controller manages the configurator flow while delegating all terminal
 * I/O to {@link ConfiguratorInteraction}.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Handles configurator login and first-access credential updates.</li>
 *   <li>Drives configuration and category management flows.</li>
 *   <li>Handles proposal creation, publication, and archive visualization.</li>
 * </ul>
 */
public class ConfiguratorController {
    private final AuthenticationService authenticationService;
    private final ConfigurationService configurationService;
    private final ProposalService proposalService;
    private final ProposalLifecycleService proposalLifecycleService;
    private final ConfiguratorInteraction interaction;

    /**
     * Creates a configurator controller bound to the given application context.
     *
     * @param context The application context used to execute use cases.
     */
    public ConfiguratorController(ApplicationContext context) {
        this.authenticationService = context.getAuthenticationService();
        this.configurationService = context.getConfigurationService();
        this.proposalService = context.getProposalService();
        this.proposalLifecycleService = context.getProposalLifecycleService();
        this.interaction = new ConfiguratorInteraction();
    }

    /**
     * Starts the complete interactive flow of the configurator backend.
     */
    public void start() {
        interaction.printBackEndTitle();

        Configurator configurator = login();
        if (configurator == null) {
            return;
        }

        if (configurator.isFirstAccess()) {
            manageFirstAccess(configurator);
        }

        if (!configurationService.areBaseFieldsSet()) {
            interaction.printFirstConfigurationNotice();
            setupBaseFields();
        }

        proposalLifecycleService.refreshProposalLifecycle();
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
            Configurator configurator = authenticationService.authenticateConfigurator(username, password);
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
            if (authenticationService.updateCredentials(configurator, newUsername, newPassword)) {
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

        for (ConfiguratorInteraction.BaseFieldTemplate baseField : interaction.baseFieldTemplates()) {
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
                configurationService.setBaseFields(fields),
                interaction.baseFieldsSetSuccessMessage(),
                interaction.baseFieldsSetFailureMessage()
        );
    }

    /**
     * Displays and handles the top-level configurator menu loop.
     */
    private void mainMenu() {
        boolean exit = false;

        while (!exit) {
            int choice = interaction.chooseMainMenu(configurationService.areBaseFieldsSet());
            switch (choice) {
                case 0 -> exit = true;
                case 1 -> {
                    if (configurationService.areBaseFieldsSet()) {
                        interaction.showFields(interaction.baseFieldsTitle(), configurationService.getBaseFields());
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
        if (!configurationService.areBaseFieldsSet()) {
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
                                configurationService.addCommonField(field),
                                interaction.commonFieldAddSuccessMessage(),
                                interaction.commonFieldAddFailureMessage()
                        );
                    }
                }
                case 2 -> {
                    int index = interaction.chooseIndex(
                            configurationService.getCommonFields(),
                            interaction.commonFieldToRemoveTitle(),
                            Field::getName
                    );
                    if (index >= 0) {
                        executeAndPrint(
                                configurationService.removeCommonField(index),
                                interaction.commonFieldRemoveSuccessMessage(),
                                interaction.commonFieldRemoveFailureMessage()
                        );
                    }
                }
                case 3 -> {
                    int index = interaction.chooseIndex(
                            configurationService.getCommonFields(),
                            interaction.commonFieldToEditTitle(),
                            Field::getName
                    );
                    if (index >= 0) {
                        executeAndPrint(
                                configurationService.toggleMandatorinessCommonField(index),
                                interaction.commonFieldToggleSuccessMessage(),
                                interaction.commonFieldToggleFailureMessage()
                        );
                    }
                }
                case 4 -> interaction.showFields(interaction.commonFieldsTitle(), configurationService.getCommonFields());
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
                            configurationService.getCategories(),
                            interaction.categoryToRemoveTitle(),
                            Category::getName
                    );
                    if (index >= 0) {
                        executeAndPrint(
                                configurationService.removeCategory(index),
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
        if (!configurationService.isCategoryNameAvailable(name)) {
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
                configurationService.addCategory(name, specificFields),
                interaction.categoryAddSuccessMessage(),
                interaction.categoryAddFailureMessage()
        );
    }

    /**
     * Displays and handles specific field management for a selected category.
     */
    private void manageSpecificFields() {
        int categoryIndex = interaction.chooseIndex(
                configurationService.getCategories(),
                interaction.categorySelectionTitle(),
                Category::getName
        );

        if (categoryIndex < 0) {
            return;
        }

        Category category = configurationService.getCategories().get(categoryIndex);
        boolean exit = false;

        while (!exit) {
            int choice = interaction.chooseSpecificFieldsMenu(category.getName());
            switch (choice) {
                case 0 -> exit = true;
                case 1 -> {
                    Field field = promptForNewField(FieldType.SPECIFIC, false, category, null);
                    if (field != null) {
                        executeAndPrint(
                                configurationService.addSpecificField(categoryIndex, field),
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
                                configurationService.removeSpecificField(categoryIndex, fieldIndex),
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
                                configurationService.toggleMandatorinessSpecificField(categoryIndex, fieldIndex),
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
                case 3 -> {
                    proposalLifecycleService.refreshProposalLifecycle();
                    interaction.showBoard(proposalService.getBoardByCategory());
                }
                default -> interaction.printInvalidChoice();
            }
        }
    }

    /**
     * Guides the user in creating a proposal and optionally publishing it.
     */
    private void createProposal() {
        List<Category> categories = configurationService.getCategories();
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
        List<Field> fields = configurationService.getSharedFieldsForCategory(category);
        Map<String, String> rawValues = new LinkedHashMap<>();

        for (Field field : fields) {
            if (!field.isMandatory() && !interaction.askFillOptionalField(field.getName())) {
                continue;
            }
            rawValues.put(field.getName(), interaction.readFieldValue(field));
        }

        Proposal proposal = proposalService.createProposal(categoryIndex, rawValues);
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
                proposalService.publishProposal(proposal),
                interaction.proposalPublishSuccessMessage(),
                interaction.proposalPublishFailureMessage()
        );
    }

    /**
     * Allows the configurator to publish one previously valid proposal.
     */
    private void publishValidProposal() {
        proposalLifecycleService.refreshProposalLifecycle();
        List<Proposal> validProposals = proposalService.getValidProposals();
        int index = interaction.chooseValidProposalToPublish(validProposals);
        if (index < 0) {
            return;
        }

        Proposal selected = validProposals.get(index);
        executeAndPrint(
                proposalService.publishProposal(selected),
                interaction.proposalPublishSuccessMessage(),
                interaction.proposalPublishFailureMessage()
        );
    }

    /**
     * Prompts the user for a new field and validates local/global uniqueness.
     *
     * @param type                  The field type to create.
     * @param forceMandatory        Whether the field must be mandatory without asking.
     * @param contextCategory       The category context for uniqueness checks.
     * @param localReservedNames    Optional names already reserved in the current batch.
     * @return A new field, or {@code null} if validation fails or the operation is canceled.
     */
    private Field promptForNewField(
            FieldType type,
            boolean forceMandatory,
            Category contextCategory,
            Set<String> localReservedNames
    ) {
        String name = interaction.readFieldName();
        boolean takenGlobally = !configurationService.isFieldNameAvailableForCategory(name, contextCategory);
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
     * Prints the result of an executed operation.
     *
     * @param result         The operation outcome.
     * @param successMessage The message to print on success.
     * @param failMessage    The message to print on failure.
     */
    private void executeAndPrint(boolean result, String successMessage, String failMessage) {
        interaction.printOperationResult(result, successMessage, failMessage);
    }

    /**
     * Displays all categories with their shared fields.
     */
    private void showFullCategories() {
        List<Category> categories = configurationService.getCategories();
        if (categories.isEmpty()) {
            interaction.printNoCategoryAvailable();
            return;
        }

        for (Category category : categories) {
            interaction.showCategoryFields(category.getName(), configurationService.getSharedFieldsForCategory(category));
        }
    }

    /**
     * Displays the full proposal archive.
     */
    private void showArchive() {
        proposalLifecycleService.refreshProposalLifecycle();
        interaction.showArchive(proposalService.getArchivedProposals());
    }
}
