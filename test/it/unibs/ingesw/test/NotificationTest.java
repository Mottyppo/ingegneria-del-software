package it.unibs.ingesw.test;

import it.unibs.ingesw.model.Notification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NotificationTest {

    @Test
    void createAndReadNotificationMessage() {
        Notification notification = new Notification("Evento confermato");
        assertEquals("Evento confermato", notification.getMessage());
        assertTrue(notification.toString().contains("Evento confermato"));
    }
}
