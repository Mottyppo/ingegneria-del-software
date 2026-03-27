package it.unibs.ingesw.test;

import it.unibs.ingesw.model.ProposalStatus;
import it.unibs.ingesw.model.StateLog;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StateLogTest {

    @Test
    void createAndReadStateLogProperties() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 3, 24, 19, 0, 0);
        StateLog stateLog = new StateLog(ProposalStatus.VALID, timestamp);

        assertEquals(ProposalStatus.VALID, stateLog.getStatus());
        assertEquals("2026-03-24T19:00:00", stateLog.getTimestamp());
    }
}
