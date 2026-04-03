package it.unibs.ingesw.application;

import it.unibs.ingesw.model.Archive;
import it.unibs.ingesw.model.Category;
import it.unibs.ingesw.model.Configurator;
import it.unibs.ingesw.model.Participant;
import it.unibs.ingesw.model.SystemConfig;
import it.unibs.ingesw.persistence.ArchiveRepository;
import it.unibs.ingesw.persistence.CategoryRepository;
import it.unibs.ingesw.persistence.ConfigRepository;
import it.unibs.ingesw.persistence.ConfiguratorRepository;
import it.unibs.ingesw.persistence.JsonArchiveRepository;
import it.unibs.ingesw.persistence.JsonBatchImportReader;
import it.unibs.ingesw.persistence.JsonCategoryRepository;
import it.unibs.ingesw.persistence.JsonConfigRepository;
import it.unibs.ingesw.persistence.JsonConfiguratorRepository;
import it.unibs.ingesw.persistence.JsonParticipantRepository;
import it.unibs.ingesw.persistence.ParticipantRepository;
import it.unibs.ingesw.service.BatchImportService;
import it.unibs.ingesw.service.AuthenticationService;
import it.unibs.ingesw.service.ConfigurationService;
import it.unibs.ingesw.service.NotificationService;
import it.unibs.ingesw.service.ProposalLifecycleService;
import it.unibs.ingesw.service.ProposalRuleValidator;
import it.unibs.ingesw.service.ProposalService;
import it.unibs.ingesw.service.ProposalValueNormalizer;

import java.util.List;

/**
 * Composition root of the application.
 *
 * <p>The context loads the shared in-memory state from persistence and wires all
 * application services over that same state snapshot. It acts as the single place
 * where object graph construction happens.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Loads configuration, users, categories, and proposals from persistence.</li>
 *   <li>Builds all application services over the same shared collections.</li>
 *   <li>Initializes default configurators when the store is empty.</li>
 * </ul>
 */
public class ApplicationContext {
    private static final String DEFAULT_CONFIGURATOR_ONE_USERNAME = "crocerossaitaliana";
    private static final String DEFAULT_CONFIGURATOR_ONE_PASSWORD = "ginevra1864";
    private static final String DEFAULT_CONFIGURATOR_TWO_USERNAME = "alpinibrescia";
    private static final String DEFAULT_CONFIGURATOR_TWO_PASSWORD = "nikolajewka1943";

    private final AuthenticationService authenticationService;
    private final ConfigurationService configurationService;
    private final ProposalLifecycleService proposalLifecycleService;
    private final ProposalService proposalService;
    private final BatchImportService batchImportService;

    /**
     * Creates the application context using the default JSON repositories.
     */
    public ApplicationContext() {
        this(
                new JsonConfigRepository(),
                new JsonCategoryRepository(),
                new JsonConfiguratorRepository(),
                new JsonParticipantRepository(),
                new JsonArchiveRepository()
        );
    }

    /**
     * Creates the application context using the provided repositories.
     *
     * @param configRepository       The configuration repository.
     * @param categoryRepository     The category repository.
     * @param configuratorRepository The configurator repository.
     * @param participantRepository  The participant repository.
     * @param archiveRepository      The proposal archive repository.
     */
    public ApplicationContext(
            ConfigRepository configRepository,
            CategoryRepository categoryRepository,
            ConfiguratorRepository configuratorRepository,
            ParticipantRepository participantRepository,
            ArchiveRepository archiveRepository
    ) {
        SystemConfig config = configRepository.read();
        List<Category> categories = categoryRepository.readAll();
        List<Configurator> configurators = configuratorRepository.readAll();
        List<Participant> participants = participantRepository.readAll();
        Archive archive = archiveRepository.read();

        this.authenticationService = new AuthenticationService(
                configurators,
                participants,
                configuratorRepository,
                participantRepository
        );
        this.configurationService = new ConfigurationService(config, categories, configRepository, categoryRepository);

        ProposalRuleValidator proposalRuleValidator = new ProposalRuleValidator();
        NotificationService notificationService = new NotificationService(participants);
        this.proposalLifecycleService = new ProposalLifecycleService(
                archive,
                archiveRepository,
                participantRepository,
                proposalRuleValidator,
                notificationService
        );
        this.proposalService = new ProposalService(
                archive,
                participants,
                archiveRepository,
                participantRepository,
                configurationService,
                notificationService,
                new ProposalValueNormalizer(),
                proposalRuleValidator
        );
        this.batchImportService = new BatchImportService(
                configurationService,
                proposalService,
                new JsonBatchImportReader()
        );

        authenticationService.initializeDefaultConfiguratorsIfNeeded(
                DEFAULT_CONFIGURATOR_ONE_USERNAME,
                DEFAULT_CONFIGURATOR_ONE_PASSWORD,
                DEFAULT_CONFIGURATOR_TWO_USERNAME,
                DEFAULT_CONFIGURATOR_TWO_PASSWORD
        );
    }

    /**
     * Returns the authentication service.
     *
     * @return The authentication service bound to this context.
     */
    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    /**
     * Returns the configuration service.
     *
     * @return The configuration service bound to this context.
     */
    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    /**
     * Returns the proposal lifecycle service.
     *
     * @return The lifecycle service bound to this context.
     */
    public ProposalLifecycleService getProposalLifecycleService() {
        return proposalLifecycleService;
    }

    /**
     * Returns the proposal service.
     *
     * @return The proposal service bound to this context.
     */
    public ProposalService getProposalService() {
        return proposalService;
    }

    /**
     * Returns the batch import service.
     *
     * @return The batch import service bound to this context.
     */
    public BatchImportService getBatchImportService() {
        return batchImportService;
    }
}
