package it.unibs.ingesw.persistence;

import com.google.gson.reflect.TypeToken;
import it.unibs.ingesw.model.Category;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON-backed implementation of {@link CategoryRepository}.
 */
public class JsonCategoryRepository extends JsonRepositorySupport implements CategoryRepository {
    private static final String CATEGORIES_FILE = "categories.json";

    @Override
    public List<Category> readAll() {
        Type listType = new TypeToken<List<Category>>() {
        }.getType();
        List<Category> categories = readJson(resolve(CATEGORIES_FILE), listType, new ArrayList<>());
        return categories == null ? new ArrayList<>() : categories;
    }

    @Override
    public void writeAll(List<Category> categories) {
        writeJson(resolve(CATEGORIES_FILE), categories);
    }
}
