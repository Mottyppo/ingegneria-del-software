package it.unibs.ingesw.test;

import it.unibs.ingesw.model.Campo;
import it.unibs.ingesw.model.Categoria;
import it.unibs.ingesw.model.TipoCampo;
import it.unibs.ingesw.model.TipoDato;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CategoriaTest {

    @Test
    void aggiungiERimuoviCampoSpecifico() {
        Categoria categoria = new Categoria("Sport", new ArrayList<>());
        Campo campo = new Campo("Certificato medico", "", true, TipoCampo.SPECIFICO, TipoDato.BOOLEAN);

        categoria.aggiungiCampoSpecifico(campo);
        assertEquals(1, categoria.getCampiSpecifici().size());

        categoria.rimuoviCampoSpecifico(0);
        assertEquals(0, categoria.getCampiSpecifici().size());
    }
}
