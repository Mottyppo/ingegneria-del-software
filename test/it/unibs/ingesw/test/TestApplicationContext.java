package it.unibs.ingesw.test;

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
import it.unibs.ingesw.service.AuthenticationService;
import it.unibs.ingesw.service.BatchImportService;
import it.unibs.ingesw.service.ConfigurationService;
import it.unibs.ingesw.service.proposal.NotificationService;
import it.unibs.ingesw.service.proposal.ProposalLifecycleService;
import it.unibs.ingesw.service.proposal.ProposalRuleValidator;
import it.unibs.ingesw.service.proposal.ProposalService;
import it.unibs.ingesw.service.proposal.ProposalValueNormalizer;

import java.util.List;

/**
 * Test-only service graph used to keep ApplicationContext a strict singleton in production.
 */
class TestApplicationContext {
    private static final String DEFAULT_CONFIGURATOR_ONE_USERNAME = "crocerossaitaliana";
    private static final String DEFAULT_CONFIGURATOR_ONE_PASSWORD = "ginevra1864";
    private static final String DEFAULT_CONFIGURATOR_TWO_USERNAME = "alpinibrescia";
    private static final String DEFAULT_CONFIGURATOR_TWO_PASSWORD = "nikolajewka1943";

    private final AuthenticationService authenticationService;
    private final ConfigurationService configurationService;
    private final ProposalLifecycleService proposalLifecycleService;
    private final ProposalService proposalService;
    private final BatchImportService batchImportService;

    TestApplicationContext() {
        this(
                new JsonConfigRepository(),
                new JsonCategoryRepository(),
                new JsonConfiguratorRepository(),
                new JsonParticipantRepository(),
                new JsonArchiveRepository()
        );
    }

    TestApplicationContext(
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

    AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    ConfigurationService getConfigurationService() {
        return configurationService;
    }

    ProposalLifecycleService getProposalLifecycleService() {
        return proposalLifecycleService;
    }

    ProposalService getProposalService() {
        return proposalService;
    }

    BatchImportService getBatchImportService() {
        return batchImportService;
    }
}
