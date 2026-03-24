package it.unibs.ingesw.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//TODO: Documentazione

public class SystemConfig {
    private List<Field> baseFields;
    private List<Field> commonFields;

    public SystemConfig() {
        this.baseFields = new ArrayList<>();
        this.commonFields = new ArrayList<>();
    }

    public boolean areBaseFieldsSet() {
        return baseFields != null && !baseFields.isEmpty();
    }

    public boolean setBaseFields(List<Field> baseFields) {
        if (areBaseFieldsSet()) {
            return false;
        }
        this.baseFields = new ArrayList<>(baseFields);
        return true;
    }

    public List<Field> getBaseFields() {
        return baseFields == null ? List.of() : Collections.unmodifiableList(baseFields);
    }

    public List<Field> getCommonFields() {
        return commonFields == null ? List.of() : Collections.unmodifiableList(commonFields);
    }

    public void addCommonField(Field field) {
        if (commonFields == null) {
            commonFields = new ArrayList<>();
        }
        commonFields.add(field);
    }

    public void removeCommonField(int index) {
        if (commonFields == null) {
            return;
        }
        commonFields.remove(index);
    }

    public void toggleMandatorinessCommonField(int index) {
        if (commonFields == null) {
            return;
        }
        commonFields.get(index).toggleMandatoriness();
    }

    @Override
    public String toString() {
        return "SystemConfig{" +
                "campi base=" + baseFields +
                ", campi comuni=" + commonFields +
                '}';
    }
}
