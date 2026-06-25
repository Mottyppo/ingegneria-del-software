package it.unibs.ingesw.service.proposal;

import it.unibs.ingesw.model.Archive;
import it.unibs.ingesw.model.Participant;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;
import it.unibs.ingesw.persistence.ArchiveRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles participant subscriptions to open proposals.
 */
class ProposalSubscriptionService {
    private static final String PARTICIPANTS_FIELD_NAME = "Numero di partecipanti";

    private final Archive archive;
    private final ArchiveRepository archiveRepository;
    private final ProposalRuleValidator validator;

    ProposalSubscriptionService(
            Archive archive,
            ArchiveRepository archiveRepository,
            ProposalRuleValidator validator
    ) {
        this.archive = archive;
        this.archiveRepository = archiveRepository;
        this.validator = validator;
    }

    boolean subscribeParticipantToProposal(Participant participant, int proposalId) {
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

    List<Proposal> getSubscribedOpenProposals(Participant participant) {
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

    boolean unsubscribeParticipantFromProposal(Participant participant, int proposalId) {
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
