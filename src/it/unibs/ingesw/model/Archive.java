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
     * Saves a proposal in the archive. If another proposal with the same id exists,
     * it gets replaced.
     *
     * @param proposal The proposal to persist.
     * @return {@code true} if the proposal has been saved, {@code false} otherwise.
     */
    public boolean saveProposal(Proposal proposal) {
        if (proposal == null) {
            return false;
        }
        ensureProposals();
        for (int i = 0; i < proposals.size(); i++) {
            Proposal current = proposals.get(i);
            if (current != null && current.getId() == proposal.getId()) {
                proposals.set(i, proposal);
                return true;
            }
        }
        proposals.add(proposal);
        return true;
    }

    /**
     * Returns all proposals currently in the requested status.
     *
     * @param status The status to filter by.
     * @return An immutable list of filtered proposals.
     */
    public List<Proposal> getByStatus(ProposalStatus status) {
        ensureProposals();
        List<Proposal> filtered = new ArrayList<>();
        if (status == null) {
            return Collections.unmodifiableList(filtered);
        }
        for (Proposal proposal : proposals) {
            if (proposal != null && proposal.getCurrentStatus() == status) {
                filtered.add(proposal);
            }
        }
        return Collections.unmodifiableList(filtered);
    }

    /**
     * Builds the board by grouping open proposals by category name.
     *
     * @return Map category -> open proposals.
     */
    public Map<String, List<Proposal>> getOpenByCategory() {
        Map<String, List<Proposal>> grouped = new LinkedHashMap<>();
        for (Proposal proposal : getByStatus(ProposalStatus.OPEN)) {
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
