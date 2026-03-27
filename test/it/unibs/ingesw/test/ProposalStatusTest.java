package it.unibs.ingesw.test;

import it.unibs.ingesw.model.ProposalStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProposalStatusTest {

    @Test
    void readAllSupportedProposalStatuses() {
        assertEquals(7, ProposalStatus.values().length);
        assertEquals("Creata", ProposalStatus.CREATED.toString());
        assertEquals("Valida", ProposalStatus.VALID.toString());
        assertEquals("Aperta", ProposalStatus.OPEN.toString());
        assertEquals("Confermata", ProposalStatus.CONFIRMED.toString());
        assertEquals("Chiusa", ProposalStatus.CLOSE.toString());
        assertEquals("Ritirata", ProposalStatus.WITHDRAWED.toString());
        assertEquals("Annullata", ProposalStatus.CANCELED.toString());
    }
}
