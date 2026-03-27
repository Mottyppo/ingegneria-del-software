package it.unibs.ingesw.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an initiative proposal produced by a configurator.
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Stores proposal id, category snapshot, and compiled field values.</li>
 *   <li>Tracks current status and complete status history.</li>
 *   <li>Stores publication date when the proposal reaches the open state.</li>
 * </ul>
 */
public class Proposal {
    private static final String TO_STRING_PREFIX =          "Proposta{";
    private static final String ID_LABEL =                  "id=";
    private static final String CATEGORY_LABEL =            ", categoria='";
    private static final String STATUS_LABEL =              ", stato=";
    private static final String FIELD_VALUES_LABEL =        ", campi=";
    private static final String TO_STRING_SUFFIX =          "}";

    private final int id;
    private final String categoryName;
    private Map<String, String> fieldValues;
    private ProposalStatus currentStatus;
    private List<StateLog> statusHistory;

    public Proposal(int id, String categoryName, Map<String, String> fieldValues) {
        this.id = id;
        this.categoryName = categoryName;
        this.fieldValues = fieldValues == null ? new LinkedHashMap<>() : new LinkedHashMap<>(fieldValues);
        this.currentStatus = ProposalStatus.CREATED;
        this.statusHistory = new ArrayList<>();
        appendState(ProposalStatus.CREATED);
    }

    public int getId() {
        return id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Map<String, String> getFieldValues() {
        ensureFieldValues();
        return Collections.unmodifiableMap(fieldValues);
    }

    public ProposalStatus getCurrentStatus() {
        return currentStatus;
    }

    public List<StateLog> getStatusHistory() {
        ensureHistory();
        return Collections.unmodifiableList(statusHistory);
    }

    //TODO: test here
    public String getPublicationDate() {
        String timestamp = null;
        for(StateLog state : statusHistory) {
            if (state.getStatus() == ProposalStatus.OPEN) {
                String currentData = state.getTimestamp();
                if (timestamp == null || currentData.compareTo(timestamp) > 0) {
                    timestamp = currentData;
                }
            }
        }
        return timestamp;
    }

    /**
     * Marks the proposal as valid.
     *
     * @return {@code true} if transition is accepted, {@code false} otherwise.
     */
    public boolean markAsValid() {
        if (currentStatus != ProposalStatus.CREATED) {
            return false;
        }
        appendState(ProposalStatus.VALID);
        return true;
    }

    /**
     * Marks the proposal as open.
     *
     * @return {@code true} if transition is accepted, {@code false} otherwise.
     */
    public boolean markAsOpen() {
        if (currentStatus != ProposalStatus.VALID) {
            return false;
        }
        appendState(ProposalStatus.OPEN);
        return true;
    }

    private void appendState(ProposalStatus nextStatus) {
        ensureHistory();
        this.currentStatus = nextStatus;
        this.statusHistory.add(new StateLog(nextStatus, LocalDateTime.now()));
    }

    private void ensureFieldValues() {
        if (fieldValues == null) {
            fieldValues = new LinkedHashMap<>();
        }
    }

    private void ensureHistory() {
        if (statusHistory == null) {
            statusHistory = new ArrayList<>();
        }
    }

    @Override
    public String toString() {
        return TO_STRING_PREFIX +
                ID_LABEL + id +
                CATEGORY_LABEL + categoryName + '\'' +
                STATUS_LABEL + currentStatus +
                FIELD_VALUES_LABEL + fieldValues +
                TO_STRING_SUFFIX;
    }
}
