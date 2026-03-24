package it.unibs.ingesw.test;

import it.unibs.ingesw.model.DataType;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.FieldType;
import it.unibs.ingesw.model.SystemConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SystemConfigTest {

    @Test
    void baseFieldsCrudCreateAndReadOnce() {
        SystemConfig config = new SystemConfig();
        List<Field> base = List.of(
                new Field("Titolo", "", true, FieldType.BASE, DataType.STRING)
        );

        // CREATE
        assertTrue(config.setBaseFields(base));
        // READ
        assertEquals(1, config.getBaseFields().size());
        assertEquals("Titolo", config.getBaseFields().getFirst().getName());
        // cannot create again
        assertFalse(config.setBaseFields(base));
    }

    @Test
    void baseFieldsImmutable() {
        SystemConfig config = new SystemConfig();
        config.setBaseFields(List.of(
                new Field("Titolo", "", true, FieldType.BASE, DataType.STRING)
        ));

        assertThrows(UnsupportedOperationException.class, () -> config.getBaseFields().add(
                new Field("Altro", "", true, FieldType.BASE, DataType.STRING)
        ));
    }

    @Test
    void commonFieldsCrudFlow() {
        SystemConfig config = new SystemConfig();
        Field comune = new Field("Note", "", false, FieldType.COMMON, DataType.STRING);

        // CREATE
        config.addCommonField(comune);

        // READ
        assertEquals(1, config.getCommonFields().size());
        assertFalse(config.getCommonFields().getFirst().isMandatory());

        // UPDATE
        config.toggleMandatorinessCommonField(0);
        assertTrue(config.getCommonFields().getFirst().isMandatory());

        // DELETE
        config.removeCommonField(0);
        assertEquals(0, config.getCommonFields().size());
    }
}
