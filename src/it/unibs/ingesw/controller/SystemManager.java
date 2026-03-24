package it.unibs.ingesw.controller;

import it.unibs.ingesw.io.IOManager;
import it.unibs.ingesw.model.Field;
import it.unibs.ingesw.model.Category;
import it.unibs.ingesw.model.Configurator;
import it.unibs.ingesw.model.SystemConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//TODO: Documentation

public class SystemManager {
    private final IOManager ioManager;
    private final List<Configurator> configurators;
    private final List<Category> categories;
    private final SystemConfig config;

    public SystemManager() {
        this.ioManager = new IOManager();
        this.config = ioManager.readConfig();
        this.categories = ioManager.readCategories();
        this.configurators = ioManager.readConfigurators();

        if (this.configurators.isEmpty()) {
            initializeDefaultConfigurators();
        }
    }

    private void initializeDefaultConfigurators() {
        Configurator c1 = new Configurator("crocerossaitaliana", "ginevra1864");
        Configurator c2 = new Configurator("alpinibrescia", "nikolajewka1943");
        this.configurators.add(c1);
        this.configurators.add(c2);
        ioManager.writeConfigurators(this.configurators);
    }

    public Configurator authenticateConfigurator(String username, String password) {
        for (Configurator configurator : configurators) {
            if (configurator.getUsername().equalsIgnoreCase(username)
                    && configurator.getPassword().equals(password)) {
                return configurator;
            }
        }
        return null;
    }

    public boolean isUsernameAvailable(String username, Configurator exclude) {
        for (Configurator configurator : configurators) {
            if (exclude != null && configurator == exclude) {
                continue;
            }
            if (configurator.getUsername().equalsIgnoreCase(username)) {
                return false;
            }
        }
        return true;
    }

    public boolean updateCredentials(Configurator configurator, String newUsername, String newPassword) {
        if (newUsername == null || newUsername.isBlank()) {
            return false;
        }
        String normalized = newUsername.trim();
        if (!isUsernameAvailable(normalized, configurator)) {
            return false;
        }
        configurator.setCredentials(normalized, newPassword);
        ioManager.writeConfigurators(this.configurators);
        return true;
    }

    public boolean areBaseFieldsSet() {
        return config.areBaseFieldsSet();
    }

    public boolean setBaseFields(List<Field> baseFields) {
        boolean success = config.setBaseFields(baseFields);
        if (success) {
            ioManager.writeConfig(config);
        }
        return success;
    }

    public List<Field> getBaseFields() {
        return config.getBaseFields();
    }

    public List<Field> getCommonFields() {
        return config.getCommonFields();
    }

    public boolean addCommonField(Field field) {
        if (!isFieldNameAvailable(field.getName(), null)) {
            return false;
        }
        config.addCommonField(field);
        ioManager.writeConfig(config);
        return true;
    }

    public boolean removeCommonField(int index) {
        if (isInvalidIndex(index, config.getCommonFields())) return false;
        config.removeCommonField(index);
        ioManager.writeConfig(config);
        return true;
    }

    public boolean toggleMandatorinessCommonField(int index) {
        if (isInvalidIndex(index, config.getCommonFields())) return false;
        config.toggleMandatorinessCommonField(index);
        ioManager.writeConfig(config);
        return true;
    }

    public List<Category> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    public boolean addCategory(String name, List<Field> specificFields) {
        if (!isCategoryNameAvailable(name)) {
            return false;
        }
        String normalized = name.trim();
        List<Field> validSpecifics = specificFields == null ? new ArrayList<>() : new ArrayList<>(specificFields);
        if (!checkSpecificFieldsNames(validSpecifics)) {
            return false;
        }
        Category category = new Category(normalized, validSpecifics);
        categories.add(category);
        ioManager.writeCategories(categories);
        return true;
    }

    public boolean removeCategory(int index) {
        if (isInvalidIndex(index, categories)) return false;
        categories.remove(index);
        ioManager.writeCategories(categories);
        return true;
    }

    public boolean addSpecificField(int categoryIndex, Field field) {
        if (isInvalidIndex(categoryIndex, categories)) return false;
        Category category = categories.get(categoryIndex);
        if (!isFieldNameAvailable(field.getName(), category)) {
            return false;
        }
        category.addSpecificField(field);
        ioManager.writeCategories(categories);
        return true;
    }

    public boolean removeSpecificField(int categoryIndex, int fieldIndex) {
        if (isInvalidIndex(categoryIndex, categories)) return false;
        Category category = categories.get(categoryIndex);
        if (isInvalidIndex(fieldIndex, category.getSpecificFields())) return false;
        category.removeSpecificField(fieldIndex);
        ioManager.writeCategories(categories);
        return true;
    }

    public boolean toggleMandatorinessSpecificField(int categoryIndex, int fieldIndex) {
        if (isInvalidIndex(categoryIndex, categories)) return false;
        Category category = categories.get(categoryIndex);
        if (isInvalidIndex(fieldIndex, category.getSpecificFields())) return false;
        category.toggleMandatoriness(fieldIndex);
        ioManager.writeCategories(categories);
        return true;
    }

    public boolean isCategoryNameAvailable(String name) {
        if (name == null || name.trim().isBlank()) {
            return false;
        }
        String normalized = name.trim();
        for (Category category : categories) {
            if (category.getName().equalsIgnoreCase(normalized)) {
                return false;
            }
        }
        return true;
    }

    private boolean isFieldNameAvailable(String fieldName, Category category) {
        if (fieldName == null) {
            return false;
        }
        String normalized = fieldName.trim();
        if (normalized.isBlank()) {
            return false;
        }

        if (fieldNameExists(config.getBaseFields(), normalized)) {
            return false;
        }
        if (fieldNameExists(config.getCommonFields(), normalized)) {
            return false;
        }
        if (category != null && fieldNameExists(category.getSpecificFields(), normalized)) {
            return false;
        }
        return true;
    }

    private boolean fieldNameExists(List<Field> fields, String fieldName) {
        for (Field field : fields) {
            if (field.getName() != null && field.getName().equalsIgnoreCase(fieldName)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkSpecificFieldsNames(List<Field> specificFields) {
        List<String> checkedNames = new ArrayList<>();
        for (Field field : specificFields) {
            if (field == null || field.getName() == null) {
                return false;
            }
            String name = field.getName().trim();
            if (name.isBlank()) {
                return false;
            }
            if (!isFieldNameAvailable(name, null)) {
                return false;
            }
            for (String checked : checkedNames) {
                if (checked.equalsIgnoreCase(name)) {
                    return false;
                }
            }
            checkedNames.add(name);
        }
        return true;
    }

    public boolean isFieldNameAvailableForCategory(String fieldName, Category category) {
        return isFieldNameAvailable(fieldName, category);
    }

    public List<Field> getSharedFieldsForCategory(Category category) {
        List<Field> fields = new ArrayList<>();
        fields.addAll(config.getBaseFields());
        fields.addAll(config.getCommonFields());
        if (category != null) {
            fields.addAll(category.getSpecificFields());
        }
        return fields;
    }

    private <T> boolean isInvalidIndex(int index, List<T> list) {
        return index < 0 || index >= list.size();
    }
}
