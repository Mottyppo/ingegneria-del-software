package it.unibs.ingesw.test;

import it.unibs.ingesw.model.Configurator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfiguratorTest {

    @Test
    void createAndReadConfiguratorProperties() {
        Configurator configurator = new Configurator("config", "pass");
        assertEquals("config", configurator.getUsername());
        assertEquals("pass", configurator.getPassword());
        assertTrue(configurator.isFirstAccess());
    }

    @Test
    void updateCredentialsChangesState() {
        Configurator configurator = new Configurator("config", "pass");

        configurator.setCredentials("nuovo", "nuova");
        assertEquals("nuovo", configurator.getUsername());
        assertEquals("nuova", configurator.getPassword());
        assertFalse(configurator.isFirstAccess());
    }
}
