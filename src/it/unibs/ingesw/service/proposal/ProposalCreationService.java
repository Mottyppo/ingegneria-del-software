package it.unibs.ingesw.service.proposal;

import it.unibs.ingesw.model.Archive;
import it.unibs.ingesw.model.Category;
import it.unibs.ingesw.model.DataType;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.persistence.ArchiveRepository;
import it.unibs.ingesw.service.ConfigurationService;

import java.util.List;
import java.util.Map;

/**
 * Creates proposals and persists their initial state.
 */
class ProposalCreationService {
    private final Archive archive;
    private final ArchiveRepository archiveRepository;
    private final ConfigurationService configurationService;
    private final ProposalValueNormalizer normalizer;
    private final ProposalRuleValidator validator;

    ProposalCreationService(
            Archive archive,
            ArchiveRepository archiveRepository,
            ConfigurationService configurationService,
            ProposalValueNormalizer normalizer,
            ProposalRuleValidator validator
    ) {
        this.archive = archive;
        this.archiveRepository = archiveRepository;
        this.configurationService = configurationService;
        this.normalizer = normalizer;
        this.validator = validator;
    }

    Proposal createProposal(int categoryIndex, Map<String, String> rawValues) {
        List<Category> categories = configurationService.getCategories();
        if (isInvalidIndex(categoryIndex, categories) || rawValues == null) {
            return null;
        }

        return createProposal(categories.get(categoryIndex), rawValues);
    }

    Proposal createProposal(String categoryName, Map<String, String> rawValues) {
        if (rawValues == null) {
            return null;
        }

        Category category = configurationService.findCategoryByName(categoryName);
        if (category == null) {
            return null;
        }

        return createProposal(category, rawValues);
    }

    private Proposal createProposal(Category category, Map<String, String> rawValues) {
        List<Field> fields = configurationService.getSharedFieldsForCategory(category);
        Map<String, String> normalized = normalizer.normalizeAndValidateValues(fields, rawValues);
        if (normalized == null) {
            return null;
        }

        Map<String, DataType> fieldTypes = normalizer.extractFieldTypes(fields, normalized);
        Proposal proposal = new Proposal(archive.nextId(), category.getName(), normalized, fieldTypes);

        archive.saveProposal(proposal);
        archiveRepository.write(archive);

        if (validator.checkDomainRules(normalized) && proposal.markAsValid()) {
            archive.saveProposal(proposal);
            archiveRepository.write(archive);
        }
        return proposal;
    }

    private <T> boolean isInvalidIndex(int index, List<T> list) {
        return index < 0 || index >= list.size();
    }
}
