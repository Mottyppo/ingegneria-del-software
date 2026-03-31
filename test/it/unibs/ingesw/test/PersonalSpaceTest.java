package it.unibs.ingesw.test;

import it.unibs.ingesw.model.Notification;
import it.unibs.ingesw.model.PersonalSpace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PersonalSpaceTest {

    @Test
    void addReadAndRemoveNotifications() {
        PersonalSpace personalSpace = new PersonalSpace();

        assertTrue(personalSpace.addNotification("Conferma proposta"));
        assertTrue(personalSpace.addNotification(new Notification("Annullamento proposta")));
        assertEquals(2, personalSpace.getNotifications().size());
        assertEquals("Conferma proposta", personalSpace.getNotifications().getFirst().getMessage());

        assertTrue(personalSpace.removeNotification(0));
        assertEquals(1, personalSpace.getNotifications().size());
        assertEquals("Annullamento proposta", personalSpace.getNotifications().getFirst().getMessage());
    }

    @Test
    void rejectInvalidNotificationsAndIndexes() {
        PersonalSpace personalSpace = new PersonalSpace();

        assertFalse(personalSpace.addNotification(" "));
        assertFalse(personalSpace.addNotification((String) null));
        assertFalse(personalSpace.removeNotification(0));
    }
}
