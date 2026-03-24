package it.unibs.ingesw.test;

import it.unibs.ingesw.model.DataType;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.FieldType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FieldTest {

    @Test
    void createAndReadFieldProperties() {
        Field field = new Field("Titolo", "Descrizione", true, FieldType.BASE, DataType.STRING);
        assertEquals("Titolo", field.getName());
        assertEquals("Descrizione", field.getDescription());
        assertTrue(field.isMandatory());
        assertEquals(FieldType.BASE, field.getType());
        assertEquals(DataType.STRING, field.getDataType());
    }

    @Test
    void updateMandatorinessWithToggle() {
        Field field = new Field("Titolo", "Descrizione", true, FieldType.BASE, DataType.STRING);
        assertTrue(field.isMandatory());

        field.toggleMandatoriness();
        assertFalse(field.isMandatory());

        field.toggleMandatoriness();
        assertTrue(field.isMandatory());
    }
}
