package it.unibs.ingesw.test;

import it.unibs.ingesw.model.DataType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataTypeTest {

    @Test
    void readAllSupportedDataTypes() {
        assertEquals(6, DataType.values().length);
        assertEquals("Testo", DataType.STRING.toString());
        assertEquals("Intero", DataType.INTEGER.toString());
        assertEquals("Decimale", DataType.DECIMAL.toString());
        assertEquals("Data", DataType.DATE.toString());
        assertEquals("Ora", DataType.TIME.toString());
        assertEquals("Booleano", DataType.BOOLEAN.toString());
    }
}
