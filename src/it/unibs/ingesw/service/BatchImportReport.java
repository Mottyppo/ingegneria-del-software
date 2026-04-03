package it.unibs.ingesw.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Collects the outcome of a single batch-import execution.
 *
 * <p>The report stores aggregate counters together with human-readable notes
 * and discard reasons so that the UI can present a concise but informative
 * summary to the configurator.</p>
 */
public class BatchImportReport {
    private final String importName;
    private final String sourcePath;
    private int totalEntries;
    private int importedEntries;
    private boolean fileError;
    private final List<String> notes;
    private final List<String> issues;

    /**
     * Creates an empty report for the given import kind and source path.
     *
     * @param importName The logical import name shown in the UI.
     * @param sourcePath The source file path provided by the user.
     */
    public BatchImportReport(String importName, String sourcePath) {
        this.importName = importName;
        this.sourcePath = sourcePath;
        this.notes = new ArrayList<>();
        this.issues = new ArrayList<>();
    }

    public String getImportName() {
        return importName;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public int getTotalEntries() {
        return totalEntries;
    }

    public int getImportedEntries() {
        return importedEntries;
    }

    public int getDiscardedEntries() {
        return Math.max(0, totalEntries - importedEntries);
    }

    public boolean hasFileError() {
        return fileError;
    }

    public List<String> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    public List<String> getIssues() {
        return Collections.unmodifiableList(issues);
    }

    void setTotalEntries(int totalEntries) {
        this.totalEntries = Math.max(0, totalEntries);
    }

    void addImportedEntry() {
        addImportedEntries(1);
    }

    void addImportedEntries(int importedEntries) {
        this.importedEntries += Math.max(0, importedEntries);
    }

    void addNote(String note) {
        if (note != null && !note.isBlank()) {
            notes.add(note);
        }
    }

    void addIssue(String issue) {
        if (issue != null && !issue.isBlank()) {
            issues.add(issue);
        }
    }

    void markFileError(String issue) {
        fileError = true;
        addIssue(issue);
    }
}
