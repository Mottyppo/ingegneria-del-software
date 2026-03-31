package it.unibs.ingesw.test;

import it.unibs.ingesw.model.Participant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParticipantTest {

    @Test
    void createAndReadParticipantProperties() {
        Participant participant = new Participant("Mario", "Rossi", "mrossi", "pwd");

        assertEquals("Mario", participant.getName());
        assertEquals("Rossi", participant.getSurname());
        assertEquals("mrossi", participant.getUsername());
        assertEquals("pwd", participant.getPassword());
        assertNotNull(participant.getPersonalSpace());
    }

    @Test
    void toStringContainsIdentityData() {
        Participant participant = new Participant("Luca", "Bianchi", "lb", "pass");
        assertTrue(participant.toString().contains("Luca"));
        assertTrue(participant.toString().contains("Bianchi"));
        assertTrue(participant.toString().contains("lb"));
    }
}
