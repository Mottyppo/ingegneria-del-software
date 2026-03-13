package it.unibs.ingesw.test;

import it.unibs.ingesw.model.Configuratore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfiguratoreTest {

    @Test
    void primoAccessoDiventaFalseDopoCambioCredenziali() {
        Configuratore configuratore = new Configuratore("config", "pass");
        assertTrue(configuratore.isPrimoAccesso());

        configuratore.setCredenziali("nuovo", "nuova");
        assertFalse(configuratore.isPrimoAccesso());
    }
}
