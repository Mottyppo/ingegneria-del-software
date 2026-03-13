package it.unibs.ingesw.test;

import it.unibs.ingesw.model.Campo;
import it.unibs.ingesw.model.TipoCampo;
import it.unibs.ingesw.model.TipoDato;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CampoTest {

    @Test
    void toggleObbligatorioChangesState() {
        Campo campo = new Campo("Titolo", "Descrizione", true, TipoCampo.BASE, TipoDato.STRING);
        assertTrue(campo.isObbligatorio());

        campo.toggleObbligatorio();
        assertFalse(campo.isObbligatorio());

        campo.toggleObbligatorio();
        assertTrue(campo.isObbligatorio());
    }
}
