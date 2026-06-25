package it.unibs.ingesw.service.proposal;

import it.unibs.ingesw.model.Archive;
import it.unibs.ingesw.model.Notification;
import it.unibs.ingesw.model.Participant;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.persistence.ArchiveRepository;
import it.unibs.ingesw.persistence.ParticipantRepository;
import it.unibs.ingesw.service.ConfigurationService;

import java.util.List;
import java.util.Map;

/**
 * Facade for proposal-related application use cases.
 *
 * <p>The facade preserves a compact API for controllers and import flows while
 * delegating each responsibility to a focused service inside this package.</p>
 */
public class ProposalService {
    private final ProposalCreationService creationService;
    private final ProposalPublicationService publicationService;
    private final ProposalSubscriptionService subscriptionService;
    private final ProposalWithdrawalService withdrawalService;
    private final ProposalQueryService queryService;
    private final PersonalSpaceService personalSpaceService;

    /**
     * Creates a proposal facade over the shared application state.
     *
     * @param archive               The shared proposal archive.
     * @param participants          The shared participants.
     * @param archiveRepository     The archive repository used to store proposal changes.
     * @param participantRepository The participant repository used to store personal-space changes.
     * @param configurationService  The configuration service used to resolve fields and categories.
     * @param notificationService   The notification service used to notify participants.
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
        this.creationService = new ProposalCreationService(
                archive,
                archiveRepository,
                configurationService,
                normalizer,
                validator
        );
        this.publicationService = new ProposalPublicationService(archive, archiveRepository);
        this.subscriptionService = new ProposalSubscriptionService(archive, archiveRepository, validator);
        this.withdrawalService = new ProposalWithdrawalService(
                archive,
                participants,
                archiveRepository,
                participantRepository,
                validator,
                notificationService
        );
        this.queryService = new ProposalQueryService(archive);
        this.personalSpaceService = new PersonalSpaceService(participants, participantRepository);
    }

    /**
     * Creates and persists a proposal using the selected category index.
     *
     * @param categoryIndex The selected category index.
     * @param rawValues     The raw values provided by the user.
     * @return The created proposal, or {@code null} when validation fails.
     */
    public Proposal createProposal(int categoryIndex, Map<String, String> rawValues) {
        return creationService.createProposal(categoryIndex, rawValues);
    }

    /**
     * Creates and persists a proposal by resolving the category through its name.
     *
     * @param categoryName The selected category name.
     * @param rawValues    The raw values provided by the user.
     * @return The created proposal, or {@code null} when validation fails.
     */
    public Proposal createProposal(String categoryName, Map<String, String> rawValues) {
        return creationService.createProposal(categoryName, rawValues);
    }

    /**
     * Publishes a valid proposal to the board.
     *
     * @param proposal The proposal to publish.
     * @return {@code true} if publication succeeds, {@code false} otherwise.
     */
    public boolean publishProposal(Proposal proposal) {
        return publicationService.publishProposal(proposal);
    }

    /**
     * Returns all currently valid proposals.
     *
     * @return An immutable list of valid proposals.
     */
    public List<Proposal> getValidProposals() {
        return queryService.getValidProposals();
    }

    /**
     * Returns all currently open proposals.
     *
     * @return An immutable list of open proposals.
     */
    public List<Proposal> getOpenProposals() {
        return queryService.getOpenProposals();
    }

    /**
     * Returns the full archived proposal list.
     *
     * @return An immutable list of archived proposals.
     */
    public List<Proposal> getArchivedProposals() {
        return queryService.getArchivedProposals();
    }

    /**
     * Returns the current board grouped by category.
     *
     * @return The category-to-proposals board.
     */
    public Map<String, List<Proposal>> getBoardByCategory() {
        return queryService.getBoardByCategory();
    }

    /**
     * Subscribes a participant to an open proposal when all constraints are satisfied.
     *
     * @param participant The participant subscribing to the proposal.
     * @param proposalId  The target proposal id.
     * @return {@code true} if the subscription succeeds, {@code false} otherwise.
     */
    public boolean subscribeParticipantToProposal(Participant participant, int proposalId) {
        return subscriptionService.subscribeParticipantToProposal(participant, proposalId);
    }

    /**
     * Returns the open proposals to which the participant is currently subscribed.
     *
     * @param participant The participant whose subscriptions must be read.
     * @return An immutable list of subscribed open proposals.
     */
    public List<Proposal> getSubscribedOpenProposals(Participant participant) {
        return subscriptionService.getSubscribedOpenProposals(participant);
    }

    /**
     * Removes a participant subscription from an open proposal while the deadline is still valid.
     *
     * @param participant The participant canceling the subscription.
     * @param proposalId  The target proposal id.
     * @return {@code true} if the cancellation succeeds, {@code false} otherwise.
     */
    public boolean unsubscribeParticipantFromProposal(Participant participant, int proposalId) {
        return subscriptionService.unsubscribeParticipantFromProposal(participant, proposalId);
    }

    /**
     * Returns the proposals that can still be withdrawn by a configurator.
     *
     * @return An immutable list of withdrawable proposals.
     */
    public List<Proposal> getWithdrawableProposals() {
        return withdrawalService.getWithdrawableProposals();
    }

    /**
     * Withdraws an open or confirmed proposal and notifies its subscribers.
     *
     * @param proposal The proposal to withdraw.
     * @return {@code true} if the withdrawal succeeds, {@code false} otherwise.
     */
    public boolean withdrawProposal(Proposal proposal) {
        return withdrawalService.withdrawProposal(proposal);
    }

    /**
     * Returns the personal-space notifications of the given participant.
     *
     * @param participant The participant whose notifications must be read.
     * @return An immutable list of notifications.
     */
    public List<Notification> getParticipantNotifications(Participant participant) {
        return personalSpaceService.getParticipantNotifications(participant);
    }

    /**
     * Removes a notification from the participant personal space.
     *
     * @param participant The participant whose notification must be removed.
     * @param index       The notification index.
     * @return {@code true} if the notification was removed, {@code false} otherwise.
     */
    public boolean removeParticipantNotification(Participant participant, int index) {
        return personalSpaceService.removeParticipantNotification(participant, index);
    }
}
