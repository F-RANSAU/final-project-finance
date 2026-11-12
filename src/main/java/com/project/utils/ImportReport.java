package com.project.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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