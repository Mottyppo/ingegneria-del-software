package it.unibs.ingesw.service.proposal;

import it.unibs.ingesw.model.Archive;
import it.unibs.ingesw.model.Proposal;
import it.unibs.ingesw.persistence.ArchiveRepository;

/**
 * Publishes valid proposals to the board.
 */
class ProposalPublicationService {
    private final Archive archive;
    private final ArchiveRepository archiveRepository;

    ProposalPublicationService(Archive archive, ArchiveRepository archiveRepository) {
        this.archive = archive;
        this.archiveRepository = archiveRepository;
    }

    boolean publishProposal(Proposal proposal) {
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
}
