package it.unibs.ingesw.service.proposal;

import it.unibs.ingesw.model.Archive;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.model.ProposalStatus;

import java.util.List;
import java.util.Map;

/**
 * Exposes proposal read models for board and archive views.
 */
class ProposalQueryService {
    private final Archive archive;

    ProposalQueryService(Archive archive) {
        this.archive = archive;
    }

    List<Proposal> getValidProposals() {
        return archive.getByStatus(ProposalStatus.VALID);
    }

    List<Proposal> getOpenProposals() {
        return archive.getByStatus(ProposalStatus.OPEN);
    }

    List<Proposal> getArchivedProposals() {
        return archive.getProposals();
    }

    Map<String, List<Proposal>> getBoardByCategory() {
        return archive.getOpenByCategory();
    }
}
