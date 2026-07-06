package it.unibs.ingesw.test;

import it.unibs.ingesw.model.Category;
import it.unibs.ingesw.model.DataType;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.FieldType;
import it.unibs.ingesw.service.ConfigurationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Black-box tests for the configurator setup use cases from version 1.
 */
public class ConfigurationServiceFlowTest {
    @TempDir
    Path tempDir;

    @BeforeEach
    void setDataDir() {
        System.setProperty("ingesw.data.dir", tempDir.toString());
    }

    @AfterEach
    void clearDataDir() {
        System.clearProperty("ingesw.data.dir");
    }

    @Test
    void configuratorDefinesFieldsAndCategoriesOnceAndCanViewThemAfterReload() {
        TestApplicationContext context = BlackBoxTestSupport.newContext();
        ConfigurationService configurationService = context.getConfigurationService();

        assertTrue(configurationService.setBaseFields(BlackBoxTestSupport.baseFields()));
        assertFalse(configurationService.setBaseFields(BlackBoxTestSupport.baseFields()));
        assertTrue(configurationService.addCommonField(BlackBoxTestSupport.noteField()));
        assertFalse(configurationService.addCommonField(
                new Field("titolo", "duplicato dei campi base", false, FieldType.COMMON, DataType.STRING)
        ));
        assertTrue(configurationService.addCategory(" Sport ", List.of(BlackBoxTestSupport.medicalCertificateField())));
        assertFalse(configurationService.addCategory("sport", List.of()));

        TestApplicationContext reloaded = BlackBoxTestSupport.newContext();
        ConfigurationService reloadedConfiguration = reloaded.getConfigurationService();
        List<Category> categories = reloadedConfiguration.getCategories();

        assertTrue(reloadedConfiguration.areBaseFieldsSet());
        assertEquals(8, reloadedConfiguration.getBaseFields().size());
        assertEquals(1, reloadedConfiguration.getCommonFields().size());
        assertEquals("Note", reloadedConfiguration.getCommonFields().getFirst().getName());
        assertEquals(1, categories.size());
        assertEquals("Sport", categories.getFirst().getName());
        assertEquals("Certificato medico", categories.getFirst().getSpecificFields().getFirst().getName());
    }

    @Test
    void configuratorUpdatesFieldSetsAndRemovesCategoriesUsingPublicOperations() {
        TestApplicationContext context = BlackBoxTestSupport.configuredSportContext();
        ConfigurationService configurationService = context.getConfigurationService();

        assertTrue(configurationService.toggleMandatorinessCommonField(0));
        assertFalse(configurationService.toggleMandatorinessCommonField(10));
        assertTrue(configurationService.addSpecificField(0, BlackBoxTestSupport.levelField()));
        assertTrue(configurationService.toggleMandatorinessSpecificField(0, 1));
        assertFalse(configurationService.removeSpecificField(0, 10));
        assertTrue(configurationService.removeSpecificField(0, 0));
        assertFalse(configurationService.removeCategory(-1));
        assertTrue(configurationService.removeCategory(0));

        TestApplicationContext reloaded = BlackBoxTestSupport.newContext();
        ConfigurationService reloadedConfiguration = reloaded.getConfigurationService();

        assertTrue(reloadedConfiguration.getCommonFields().getFirst().isMandatory());
        assertTrue(reloadedConfiguration.getCategories().isEmpty());
    }
}
