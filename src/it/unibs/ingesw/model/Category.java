package it.unibs.ingesw.model;

import java.util.ArrayList;
import java.util.List;

//TODO: Documentazione

public class Category {
    private final String name;
    private List<Field> specificFields;

    public Category(String name, List<Field> specificFields) {
        this.name = name;
        this.specificFields = specificFields == null ? new ArrayList<>() : specificFields;
    }

    public String getName() {
        return name;
    }

    public List<Field> getSpecificFields() {
        if (specificFields == null) {
            specificFields = new ArrayList<>();
        }
        return specificFields;
    }

    public void addSpecificField(Field field) {
        specificFields.add(field);
    }

    public void removeSpecificField(int index) {
        specificFields.remove(index);
    }

    public void toggleMandatoriness(int index) {
        specificFields.get(index).toggleMandatoriness();
    }

    @Override
    public String toString() {
        return "Categoria{" +
                "nome='" + name + '\'' +
                ", campi specifici=" + specificFields +
                '}';
    }
}
