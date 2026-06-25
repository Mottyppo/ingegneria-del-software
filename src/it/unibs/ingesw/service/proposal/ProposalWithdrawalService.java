package it.unibs.ingesw.service.proposal;

import it.unibs.ingesw.model.Archive;
import it.unibs.ingesw.model.Participant;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.persistence.ArchiveRepository;
import it.unibs.ingesw.persistence.ParticipantRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles proposal withdrawal and subscriber notifications.
 */
class ProposalWithdrawalService {
    private final Archive archive;
    private final List<Participant> participants;
    private final ArchiveRepository archiveRepository;
    private final ParticipantRepository participantRepository;
    private final ProposalRuleValidator validator;
    private final NotificationService notificationService;

    ProposalWithdrawalService(
            Archive archive,
            List<Participant> participants,
            ArchiveRepository archiveRepository,
            ParticipantRepository participantRepository,
            ProposalRuleValidator validator,
            NotificationService notificationService
    ) {
        this.archive = archive;
        this.participants = participants;
        this.archiveRepository = archiveRepository;
        this.participantRepository = participantRepository;
        this.validator = validator;
        this.notificationService = notificationService;
    }

    List<Proposal> getWithdrawableProposals() {
        List<Proposal> withdrawable = new ArrayList<>();
        for (Proposal proposal : archive.getProposals()) {
            if (validator.canWithdrawProposal(proposal)) {
                withdrawable.add(proposal);
            }
        }
        return List.copyOf(withdrawable);
    }

    boolean withdrawProposal(Proposal proposal) {
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
}
