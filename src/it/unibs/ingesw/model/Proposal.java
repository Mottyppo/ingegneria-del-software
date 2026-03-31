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
 *   <li>Stores subscribed fruitori while the proposal is open.</li>
 *   <li>Stores publication date when the proposal reaches the open state.</li>
 * </ul>
 */
public class Proposal {
    private static final String TO_STRING_PREFIX =          "Proposta{";
    private static final String ID_LABEL =                  "id=";
    private static final String CATEGORY_LABEL =            ", categoria='";
    private static final String STATUS_LABEL =              ", stato=";
    private static final String FIELD_VALUES_LABEL =        ", campi=";
    private static final String FIELD_TYPES_LABEL =         ", tipi campi=";
    private static final String SUBSCRIBERS_LABEL =         ", iscritti=";
    private static final String TO_STRING_SUFFIX =          "}";

    private final int id;
    private final String categoryName;
    private Map<String, String> fieldValues;
    private Map<String, DataType> fieldTypes;
    private ProposalStatus currentStatus;
    private List<StateLog> statusHistory;
    private List<String> subscribers;

    public Proposal(int id, String categoryName, Map<String, String> fieldValues) {
        this(id, categoryName, fieldValues, null);
    }

    public Proposal(
            int id,
            String categoryName,
            Map<String, String> fieldValues,
            Map<String, DataType> fieldTypes
    ) {
        this.id = id;
        this.categoryName = categoryName;
        this.fieldValues = fieldValues == null ? new LinkedHashMap<>() : new LinkedHashMap<>(fieldValues);
        this.fieldTypes = fieldTypes == null ? new LinkedHashMap<>() : new LinkedHashMap<>(fieldTypes);
        this.currentStatus = ProposalStatus.CREATED;
        this.statusHistory = new ArrayList<>();
        this.subscribers = new ArrayList<>();
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

    public DataType getFieldType(String fieldName) {
        ensureFieldTypes();
        return fieldTypes.get(fieldName);
    }

    public ProposalStatus getCurrentStatus() {
        return currentStatus;
    }

    public List<StateLog> getStatusHistory() {
        ensureHistory();
        return Collections.unmodifiableList(statusHistory);
    }

    public List<String> getSubscribers() {
        ensureSubscribers();
        return Collections.unmodifiableList(subscribers);
    }

    public boolean addSubscriber(String username, int maxParticipants) {
        ensureSubscribers();
        String normalized = username == null ? null : username.trim();
        if (normalized == null || normalized.isBlank()) {
            return false;
        }
        if (currentStatus != ProposalStatus.OPEN) {
            return false;
        }
        if (maxParticipants <= 0 || subscribers.size() >= maxParticipants) {
            return false;
        }
        for (String subscribed : subscribers) {
            if (subscribed != null && subscribed.equalsIgnoreCase(normalized)) {
                return false;
            }
        }
        subscribers.add(normalized);
        return true;
    }

    /**
     * Retrieves the publication date of the proposal.
     * <p>
     * The publication date is determined by finding the most recent timestamp
     * in the status history where the proposal reached the {@link ProposalStatus#OPEN} state.
     * </p>
     *
     * @return The timestamp of the publication as a {@code String}, or {@code null} if the proposal has never been opened.
     */
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

    /**
     * Marks the proposal as confirmed.
     *
     * @return {@code true} if transition is accepted, {@code false} otherwise.
     */
    public boolean markAsConfirmed() {
        if (currentStatus != ProposalStatus.OPEN) {
            return false;
        }
        appendState(ProposalStatus.CONFIRMED);
        return true;
    }

    /**
     * Marks the proposal as canceled.
     *
     * @return {@code true} if transition is accepted, {@code false} otherwise.
     */
    public boolean markAsCanceled() {
        if (currentStatus != ProposalStatus.OPEN) {
            return false;
        }
        appendState(ProposalStatus.CANCELED);
        return true;
    }

    /**
     * Marks the proposal as closed.
     *
     * @return {@code true} if transition is accepted, {@code false} otherwise.
     */
    public boolean markAsClose() {
        if (currentStatus != ProposalStatus.CONFIRMED) {
            return false;
        }
        appendState(ProposalStatus.CLOSE);
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

    private void ensureFieldTypes() {
        if (fieldTypes == null) {
            fieldTypes = new LinkedHashMap<>();
        }
    }

    private void ensureHistory() {
        if (statusHistory == null) {
            statusHistory = new ArrayList<>();
        }
    }

    private void ensureSubscribers() {
        if (subscribers == null) {
            subscribers = new ArrayList<>();
        }
    }

    @Override
    public String toString() {
        return TO_STRING_PREFIX +
                ID_LABEL + id +
                CATEGORY_LABEL + categoryName + '\'' +
                STATUS_LABEL + currentStatus +
                FIELD_VALUES_LABEL + fieldValues +
                FIELD_TYPES_LABEL + fieldTypes +
                SUBSCRIBERS_LABEL + subscribers +
                TO_STRING_SUFFIX;
    }
}
