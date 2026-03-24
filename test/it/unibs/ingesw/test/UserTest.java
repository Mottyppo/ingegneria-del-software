package it.unibs.ingesw.test;

import it.unibs.ingesw.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserTest {

    private static final class DummyUser extends User {
        private DummyUser(String username, String password) {
            super(username, password);
        }
    }

    @Test
    void createAndReadUserProperties() {
        DummyUser user = new DummyUser("utente", "password");
        assertEquals("utente", user.getUsername());
        assertEquals("password", user.getPassword());
    }

    @Test
    void toStringIncludesUsername() {
        DummyUser user = new DummyUser("utente", "password");
        assertTrue(user.toString().contains("utente"));
    }
}
