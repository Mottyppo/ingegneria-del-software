package it.unibs.ingesw.test;

import it.unibs.ingesw.model.FieldType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FieldTypeTest {

    @Test
    void readAllSupportedFieldTypes() {
        assertEquals(3, FieldType.values().length);
        assertEquals("Base", FieldType.BASE.toString());
        assertEquals("Comune", FieldType.COMMON.toString());
        assertEquals("Specifico", FieldType.SPECIFIC.toString());
    }
}
