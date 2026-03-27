package it.unibs.ingesw.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable log entry that tracks a proposal status transition.
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Stores the reached status.</li>
 *   <li>Stores the transition timestamp in ISO date-time format.</li>
 * </ul>
 */
public class StateLog {
    private static final String TO_STRING_PREFIX =  "LogStato{";
    private static final String STATUS_LABEL =      "stato=";
    private static final String TIMESTAMP_LABEL =   ", timestamp='";
    private static final String TO_STRING_SUFFIX =  "}";

    private final ProposalStatus status;
    private final String timestamp;

    public StateLog(ProposalStatus status, LocalDateTime timestamp) {
        this.status = status;
        this.timestamp = timestamp == null ? null : timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public ProposalStatus getStatus() {
        return status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return TO_STRING_PREFIX +
                STATUS_LABEL + status +
                TIMESTAMP_LABEL + timestamp + '\'' +
                TO_STRING_SUFFIX;
    }
}
