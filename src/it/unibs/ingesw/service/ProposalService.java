package it.unibs.ingesw.service;

import it.unibs.ingesw.model.Archive;
import it.unibs.ingesw.model.Category;
import it.unibs.ingesw.model.DataType;
import it.unibs.ingesw.model.Notification;
import it.unibs.ingesw.model.Participant;
import it.unibs.ingesw.model.PersonalSpace;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;
import it.unibs.ingesw.persistence.ArchiveRepository;
import it.unibs.ingesw.persistence.ParticipantRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles proposal-related application use cases.
 *
 * <p>The service coordinates proposal creation, publication, subscriptions,
 * archive access, and personal-space interactions while delegating validation
 * and lifecycle concerns to dedicated collaborators.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Creates and publishes proposals.</li>
 *   <li>Provides board and archive queries without implicit lifecycle refresh.</li>
 *   <li>Handles participant subscriptions.</li>
 *   <li>Exposes personal-space notification use cases.</li>
 * </ul>
 */
public class ProposalService {
    private static final String PARTICIPANTS_FIELD_NAME = "Numero di partecipanti";

    private final Archive archive;
    private final List<Participant> participants;
    private final ArchiveRepository archiveRepository;
    private final ParticipantRepository participantRepository;
    private final ConfigurationService configurationService;
    private final NotificationService notificationService;
    private final ProposalValueNormalizer normalizer;
    private final ProposalRuleValidator validator;

    /**
     * Creates a proposal service over the shared application state.
     *
     * @param archive               The shared proposal archive.
     * @param participants          The shared participants.
     * @param archiveRepository     The archive repository used to store proposal changes.
     * @param participantRepository The participant repository used to store personal-space changes.
     * @param configurationService  The configuration service used to resolve fields and categories.
     * @param notificationService   The notification service used to notify participant of a withdrawal
     * @param normalizer            The proposal value normalizer.
     * @param validator             The proposal rule validator.
     */
    public ProposalService(
            Archive archive,
            List<Participant> participants,
            ArchiveRepository archiveRepository,
            ParticipantRepository participantRepository,
            ConfigurationService configurationService,
            NotificationService notificationService,
            ProposalValueNormalizer normalizer,
            ProposalRuleValidator validator
    ) {
        this.archive = archive;
        this.participants = participants;
        this.archiveRepository = archiveRepository;
        this.participantRepository = participantRepository;
        this.configurationService = configurationService;
        this.notificationService = notificationService;
        this.normalizer = normalizer;
        this.validator = validator;
    }

    /**
     * Creates and persists a proposal using the raw values coming from the UI.
     *
     * @param categoryIndex The selected category index.
     * @param rawValues     The raw values provided by the user.
     * @return The created proposal, or {@code null} when structural validation fails.
     */
    public Proposal createProposal(int categoryIndex, Map<String, String> rawValues) {
        List<Category> categories = configurationService.getCategories();
        if (isInvalidIndex(categoryIndex, categories) || rawValues == null) {
            return null;
        }

        Category category = categories.get(categoryIndex);
        List<it.unibs.ingesw.model.Field> fields = configurationService.getSharedFieldsForCategory(category);
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

    /**
     * Publishes a valid proposal to the board.
     *
     * @param proposal The proposal to publish.
     * @return {@code true} if publication succeeds, {@code false} otherwise.
     */
    public boolean publishProposal(Proposal proposal) {
        if (proposal == null) {
            return false;
        }

        Proposal persisted = archive.findById(proposal.getId());
        if (persisted == null) {
            return false;
        }
        if (!persisted.markAsOpen()) {
            return false;
        }

        archive.saveProposal(persisted);
        archiveRepository.write(archive);
        return true;
    }

    /**
     * Returns all currently valid proposals.
     *
     * @return An immutable list of valid proposals.
     */
    public List<Proposal> getValidProposals() {
        return archive.getByStatus(ProposalStatus.VALID);
    }

    /**
     * Returns all currently open proposals.
     *
     * @return An immutable list of open proposals.
     */
    public List<Proposal> getOpenProposals() {
        return archive.getByStatus(ProposalStatus.OPEN);
    }

    /**
     * Returns the full archived proposal list.
     *
     * @return An immutable list of archived proposals.
     */
    public List<Proposal> getArchivedProposals() {
        return archive.getProposals();
    }

    /**
     * Returns the current board grouped by category.
     *
     * @return The category-to-proposals board.
     */
    public Map<String, List<Proposal>> getBoardByCategory() {
        return archive.getOpenByCategory();
    }

    /**
     * Subscribes a participant to an open proposal when all constraints are satisfied.
     *
     * @param participant The participant subscribing to the proposal.
     * @param proposalId  The target proposal id.
     * @return {@code true} if the subscription succeeds, {@code false} otherwise.
     */
    public boolean subscribeParticipantToProposal(Participant participant, int proposalId) {
        if (participant == null) {
            return false;
        }

        Proposal proposal = archive.findById(proposalId);
        if (proposal == null || proposal.getCurrentStatus() != ProposalStatus.OPEN) {
            return false;
        }
        if (!validator.isSubscriptionWindowOpen(proposal)) {
            return false;
        }

        Integer participantsCount = validator.parseInteger(proposal.getFieldValues().get(PARTICIPANTS_FIELD_NAME));
        if (participantsCount == null || participantsCount <= 0) {
            return false;
        }

        boolean subscribed = proposal.addSubscriber(participant.getUsername(), participantsCount);
        if (!subscribed) {
            return false;
        }

        archive.saveProposal(proposal);
        archiveRepository.write(archive);
        return true;
    }

    /**
     * Returns the open proposals to which the participant is currently subscribed.
     *
     * @param participant The participant whose subscriptions must be read.
     * @return An immutable list of subscribed open proposals.
     */
    public List<Proposal> getSubscribedOpenProposals(Participant participant) {
        if (participant == null) {
            return List.of();
        }

        List<Proposal> subscribedProposals = new ArrayList<>();
        for (Proposal proposal : archive.getByStatus(ProposalStatus.OPEN)) {
            if (proposal != null
                    && validator.isSubscriptionWindowOpen(proposal)
                    && containsSubscriber(proposal, participant.getUsername())) {
                subscribedProposals.add(proposal);
            }
        }
        return List.copyOf(subscribedProposals);
    }

    /**
     * Removes a participant subscription from an open proposal while the deadline is still valid.
     *
     * @param participant The participant canceling the subscription.
     * @param proposalId  The target proposal id.
     * @return {@code true} if the cancellation succeeds, {@code false} otherwise.
     */
    public boolean unsubscribeParticipantFromProposal(Participant participant, int proposalId) {
        if (participant == null) {
            return false;
        }

        Proposal proposal = archive.findById(proposalId);
        if (proposal == null || proposal.getCurrentStatus() != ProposalStatus.OPEN) {
            return false;
        }
        if (!validator.isSubscriptionWindowOpen(proposal)) {
            return false;
        }
        if (!proposal.removeSubscriber(participant.getUsername())) {
            return false;
        }

        archive.saveProposal(proposal);
        archiveRepository.write(archive);
        return true;
    }

    /**
     * Returns the proposals that can still be withdrawn by a configurator.
     *
     * @return An immutable list of withdrawable proposals.
     */
    public List<Proposal> getWithdrawableProposals() {
        List<Proposal> withdrawable = new ArrayList<>();
        for (Proposal proposal : archive.getProposals()) {
            if (validator.canWithdrawProposal(proposal)) {
                withdrawable.add(proposal);
            }
        }
        return List.copyOf(withdrawable);
    }

    /**
     * Withdraws an open or confirmed proposal and notifies its subscribers.
     *
     * @param proposal The proposal to withdraw.
     * @return {@code true} if the withdrawal succeeds, {@code false} otherwise.
     */
    public boolean withdrawProposal(Proposal proposal) {
        if (proposal == null) {
            return false;
        }

        Proposal persisted = archive.findById(proposal.getId());
        if (!validator.canWithdrawProposal(persisted)) {
            return false;
        }
        if (!persisted.markAsWithdrawed()) {
            return false;
        }

        archive.saveProposal(persisted);
        archiveRepository.write(archive);

        boolean participantsChanged = notificationService.notifyProposalWithdrawed(persisted);
        if (participantsChanged) {
            participantRepository.writeAll(participants);
        }
        return true;
    }

    /**
     * Returns the personal-space notifications of the given participant.
     *
     * @param participant The participant whose notifications must be read.
     * @return An immutable list of notifications.
     */
    public List<Notification> getParticipantNotifications(Participant participant) {
        if (participant == null) {
            return List.of();
        }
        PersonalSpace personalSpace = participant.getPersonalSpace();
        return personalSpace.getNotifications();
    }

    /**
     * Removes a notification from the participant personal space.
     *
     * @param participant   The participant whose notification must be removed.
     * @param index         The notification index.
     * @return {@code true} if the notification was removed, {@code false} otherwise.
     */
    public boolean removeParticipantNotification(Participant participant, int index) {
        if (participant == null) {
            return false;
        }

        boolean removed = participant.getPersonalSpace().removeNotification(index);
        if (removed) {
            participantRepository.writeAll(participants);
        }
        return removed;
    }

    /**
     * Checks whether an index is invalid for the given list.
     *
     * @param index The index to validate.
     * @param list  The list to validate against.
     * @param <T>   The list item type.
     * @return {@code true} if the index is invalid, {@code false} otherwise.
     */
    private <T> boolean isInvalidIndex(int index, List<T> list) {
        return index < 0 || index >= list.size();
    }

    /**
     * Checks whether the given proposal already contains the provided subscriber.
     *
     * @param proposal  The proposal to inspect.
     * @param username  The username to search for.
     * @return {@code true} if the proposal already contains the subscriber, {@code false} otherwise.
     */
    private boolean containsSubscriber(Proposal proposal, String username) {
        if (proposal == null || username == null) {
            return false;
        }

        for (String subscriber : proposal.getSubscribers()) {
            if (subscriber != null && subscriber.equalsIgnoreCase(username.trim())) {
                return true;
            }
        }
        return false;
    }
}
