package it.unibs.ingesw.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistent archive of proposals.
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Stores all saved proposals.</li>
 *   <li>Provides auto-increment id generation support.</li>
 *   <li>Builds the board view grouped by category for open proposals.</li>
 * </ul>
 */
public class Archive {
    private static final String TO_STRING_PREFIX =  "Archivio{";
    private static final String PROPOSALS_LABEL =   "proposte=";
    private static final String TO_STRING_SUFFIX =  "}";

    private List<Proposal> proposals;

    public Archive() {
        this.proposals = new ArrayList<>();
    }

    public List<Proposal> getProposals() {
        ensureProposals();
        return Collections.unmodifiableList(proposals);
    }

    /**
     * Returns the next available proposal identifier.
     *
     * @return The next id.
     */
    public int nextId() {
        ensureProposals();
        int max = 0;
        for (Proposal proposal : proposals) {
            if (proposal != null) {
                max = Math.max(max, proposal.getId());
            }
        }
        return max + 1;
    }

    /**
     * Adds a proposal to the archive only if it is currently open.
     *
     * @param proposal The proposal to persist.
     * @return {@code true} if the proposal is saved, {@code false} otherwise.
     */
    public boolean addOpenProposal(Proposal proposal) {
        if (proposal == null || proposal.getCurrentStatus() != ProposalStatus.OPEN) {
            return false;
        }
        ensureProposals();
        for (Proposal current : proposals) {
            if (current != null && current.getId() == proposal.getId()) {
                return false;
            }
        }
        proposals.add(proposal);
        return true;
    }

    /**
     * Builds the board by grouping open proposals by category name.
     *
     * @return Map category -> open proposals.
     */
    public Map<String, List<Proposal>> getOpenByCategory() {
        ensureProposals();
        Map<String, List<Proposal>> grouped = new LinkedHashMap<>();
        for (Proposal proposal : proposals) {
            if (proposal == null || proposal.getCurrentStatus() != ProposalStatus.OPEN) {
                continue;
            }
            String categoryName = proposal.getCategoryName();
            if (categoryName == null || categoryName.isBlank()) {
                categoryName = "Senza categoria";
            }
            grouped.computeIfAbsent(categoryName, _ -> new ArrayList<>()).add(proposal);
        }

        Map<String, List<Proposal>> immutable = new LinkedHashMap<>();
        for (Map.Entry<String, List<Proposal>> entry : grouped.entrySet()) {
            immutable.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(immutable);
    }

    private void ensureProposals() {
        if (proposals == null) {
            proposals = new ArrayList<>();
        }
    }

    @Override
    public String toString() {
        return TO_STRING_PREFIX +
                PROPOSALS_LABEL + proposals +
                TO_STRING_SUFFIX;
    }
}
