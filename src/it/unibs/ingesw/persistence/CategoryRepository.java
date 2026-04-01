package it.unibs.ingesw.persistence;

import it.unibs.ingesw.model.Category;

import java.util.List;

/**
 * Repository for configured proposal categories.
 *
 * <p>The repository stores and retrieves the complete category collection,
 * including category-specific fields.</p>
 */
public interface CategoryRepository {

    /**
     * Loads all configured categories.
     *
     * @return The persisted categories, or an empty list when unavailable.
     */
    List<Category> readAll();

    /**
     * Stores the full category collection.
     *
     * @param categories The categories to persist.
     */
    void writeAll(List<Category> categories);
}
