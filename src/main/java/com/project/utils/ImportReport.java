package com.project.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Résultat de l'import des transactions : combien ont été acceptées, et pourquoi les autres ont été refusées.
 */
public class ImportReport {
    private int importedCount;
    private final List<String> rejections = new ArrayList<>();

    public void addImported() {
        importedCount++;
    }

    public void addRejection(int lineNumber, String reason) {
        rejections.add("Ligne " + lineNumber + " : " + reason);
    }

    public int getImportedCount() {
        return importedCount;
    }

    public List<String> getRejections() {
        return Collections.unmodifiableList(rejections);
    }
}