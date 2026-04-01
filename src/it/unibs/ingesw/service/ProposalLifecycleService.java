package it.unibs.ingesw.service;

import it.unibs.ingesw.model.Archive;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;
import it.unibs.ingesw.persistence.ArchiveRepository;
import it.unibs.ingesw.persistence.ParticipantRepository;

/**
 * Applies automatic proposal lifecycle transitions.
 *
 * <p>The service evaluates time-based and subscriber-based transitions and
 * persists the resulting archive and notification changes when needed.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Confirms or cancels open proposals after the subscription deadline.</li>
 *   <li>Closes confirmed proposals after the end date.</li>
 *   <li>Triggers automatic subscriber notifications.</li>
 * </ul>
 */
public class ProposalLifecycleService {
    private static final String PARTICIPANTS_FIELD_NAME = "Numero di partecipanti";

    private final Archive archive;
    private final ArchiveRepository archiveRepository;
    private final ParticipantRepository participantRepository;
    private final ProposalRuleValidator validator;
    private final NotificationService notificationService;

    /**
     * Creates a lifecycle service over the shared archive.
     *
     * @param archive               The shared archive.
     * @param archiveRepository     The archive repository used to store changes.
     * @param participantRepository The participant repository used to store participant updates.
     * @param validator             The rule validator used for date and numeric checks.
     * @param notificationService   The notification service used for automatic messages.
     */
    public ProposalLifecycleService(
            Archive archive,
            ArchiveRepository archiveRepository,
            ParticipantRepository participantRepository,
            ProposalRuleValidator validator,
            NotificationService notificationService
    ) {
        this.archive = archive;
        this.archiveRepository = archiveRepository;
        this.participantRepository = participantRepository;
        this.validator = validator;
        this.notificationService = notificationService;
    }

    /**
     * Applies all automatic lifecycle transitions to the loaded proposals.
     */
    public void refreshProposalLifecycle() {
        boolean archiveChanged = false;
        boolean participantsChanged = false;

        for (Proposal proposal : archive.getProposals()) {
            if (proposal == null) {
                continue;
            }

            if (proposal.getCurrentStatus() == ProposalStatus.OPEN && validator.isDeadlineExpired(proposal)) {
                Integer expectedParticipants = validator.parseInteger(
                        proposal.getFieldValues().get(PARTICIPANTS_FIELD_NAME)
                );
                int subscribedCount = proposal.getSubscribers().size();

                boolean confirmed = expectedParticipants != null
                        && expectedParticipants > 0
                        && subscribedCount == expectedParticipants
                        && proposal.markAsConfirmed();

                if (!confirmed) {
                    proposal.markAsCanceled();
                }

                archive.saveProposal(proposal);
                archiveChanged = true;

                boolean notified = confirmed
                        ? notificationService.notifyProposalConfirmed(proposal)
                        : notificationService.notifyProposalCanceled(proposal);
                participantsChanged = participantsChanged || notified;
            }

            if (proposal.getCurrentStatus() == ProposalStatus.CONFIRMED && validator.isAfterEndDate(proposal)) {
                if (proposal.markAsClose()) {
                    archive.saveProposal(proposal);
                    archiveChanged = true;
                }
            }
        }

        if (archiveChanged) {
            archiveRepository.write(archive);
        }
        if (participantsChanged) {
            participantRepository.writeAll(notificationService.getParticipants());
        }
    }
}
