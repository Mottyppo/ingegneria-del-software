package it.unibs.ingesw.test;

import it.unibs.ingesw.model.Campo;
import it.unibs.ingesw.model.SystemConfig;
import it.unibs.ingesw.model.TipoCampo;
import it.unibs.ingesw.model.TipoDato;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemConfigTest {

    @Test
    void impostaCampiBaseSoloUnaVolta() {
        SystemConfig config = new SystemConfig();
        List<Campo> base = List.of(
                new Campo("Titolo", "", true, TipoCampo.BASE, TipoDato.STRING)
        );

        assertTrue(config.impostaCampiBase(base));
        assertFalse(config.impostaCampiBase(base));
        assertEquals(1, config.getCampiBase().size());
    }

    @Test
    void campiBaseNonModificabili() {
        SystemConfig config = new SystemConfig();
        config.impostaCampiBase(List.of(
                new Campo("Titolo", "", true, TipoCampo.BASE, TipoDato.STRING)
        ));

        assertThrows(UnsupportedOperationException.class, () -> config.getCampiBase().add(
                new Campo("Altro", "", true, TipoCampo.BASE, TipoDato.STRING)
        ));
    }

    @Test
    void gestioneCampiComuni() {
        SystemConfig config = new SystemConfig();
        Campo comune = new Campo("Note", "", false, TipoCampo.COMUNE, TipoDato.STRING);

        config.aggiungiCampoComune(comune);
        assertEquals(1, config.getCampiComuni().size());

        config.modificaObbligatorioComune(0);
        assertTrue(config.getCampiComuni().get(0).isObbligatorio());

        config.rimuoviCampoComune(0);
        assertEquals(0, config.getCampiComuni().size());
    }
}
