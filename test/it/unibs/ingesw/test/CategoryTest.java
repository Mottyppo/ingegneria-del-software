package it.unibs.ingesw.test;

import it.unibs.ingesw.model.Category;
import it.unibs.ingesw.model.DataType;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.FieldType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CategoryTest {

    @Test
    void createAndReadCategoryProperties() {
        Category category = new Category("Sport", new ArrayList<>());
        assertEquals("Sport", category.getName());
        assertTrue(category.getSpecificFields().isEmpty());
    }

    @Test
    void createCategoryWithNullSpecificFieldsBuildsEmptyList() {
        Category category = new Category("Musica", null);
        assertTrue(category.getSpecificFields().isEmpty());
    }

    @Test
    void specificFieldsCrudFlow() {
        Category category = new Category("Sport", new ArrayList<>());
        Field field = new Field("Certificato medico", "", true, FieldType.SPECIFIC, DataType.BOOLEAN);

        // CREATE
        category.addSpecificField(field);

        // READ
        assertEquals(1, category.getSpecificFields().size());
        assertEquals(field, category.getSpecificFields().getFirst());

        // UPDATE
        category.toggleMandatoriness(0);
        assertFalse(category.getSpecificFields().getFirst().isMandatory());

        // DELETE
        category.removeSpecificField(0);
        assertTrue(category.getSpecificFields().isEmpty());
    }
}
