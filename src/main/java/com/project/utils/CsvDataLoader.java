package com.project.utils;

import com.project.exception.BudgetExceededException;
import com.project.exception.DataLoadException;
import com.project.exception.InsufficientBalanceException;
import com.project.exception.InvalidAmountException;
import com.project.model.Account;
import com.project.model.Budget;
import com.project.model.Expense;
import com.project.model.ExpenseCategory;
import com.project.model.Income;
import com.project.service.TransactionRecorder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lit le fichier CSV de démarrage et transforme ses lignes en objets du domaine.
 *
 * Format (séparateur ";", lignes vides et lignes commençant par "#" ignorées) :
 *   ACCOUNT;numéro;titulaire;solde initial
 *   BUDGET;catégorie;plafond mensuel
 *   INCOME;date;montant;description;source
 *   EXPENSE;date;montant;description;catégorie
 */
public class CsvDataLoader {
    // ";" plutôt que "," : les descriptions peuvent contenir des virgules.
    private static final String SEPARATOR = ";";

    private final Path path;

    public CsvDataLoader(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Le chemin du fichier est obligatoire.");
        }
        this.path = path;
    }

    public List<String> readLines() throws DataLoadException {
        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataLoadException("Impossible de lire le fichier " + path.toAbsolutePath(), e);
        }
    }

    /** Le compte est indispensable : sans ligne ACCOUNT valide, l'application ne peut pas démarrer. */
    public Account parseAccount(List<String> lines) throws DataLoadException {
        for (int i = 0; i < lines.size(); i++) {
            String[] fields = split(lines.get(i));
            if (fields.length > 0 && fields[0].equals("ACCOUNT")) {
                try {
                    requireColumns(fields, 4);
                    return new Account(fields[1], fields[2], parseAmount(fields[3]));
                } catch (DataLoadException | InvalidAmountException | IllegalArgumentException e) {
                    throw new DataLoadException("Ligne " + (i + 1) + " : compte invalide (" + e.getMessage() + ")", e);
                }
            }
        }
        throw new DataLoadException("Le fichier ne contient aucune ligne ACCOUNT.");
    }

    /** Une erreur dans un budget arrête le chargement : mieux vaut corriger la configuration que l'ignorer. */
    public List<Budget> parseBudgets(List<String> lines) throws DataLoadException {
        List<Budget> budgets = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String[] fields = split(lines.get(i));
            if (fields.length > 0 && fields[0].equals("BUDGET")) {
                try {
                    requireColumns(fields, 3);
                    budgets.add(new Budget(parseCategory(fields[1]), parseAmount(fields[2])));
                } catch (DataLoadException | InvalidAmountException e) {
                    throw new DataLoadException("Ligne " + (i + 1) + " : budget invalide (" + e.getMessage() + ")", e);
                }
            }
        }
        return budgets;
    }

    /**
     * Rejoue les transactions dans l'ordre du fichier.
     * Une transaction invalide ou refusée est notée dans le rapport, puis on passe à la suivante :
     * une seule ligne fausse ne doit pas empêcher de charger tout le reste.
     */
    public ImportReport importTransactions(List<String> lines, TransactionRecorder recorder) {
        ImportReport report = new ImportReport();
        for (int i = 0; i < lines.size(); i++) {
            int lineNumber = i + 1;
            String[] fields = split(lines.get(i));
            if (fields.length == 0) {
                continue;
            }
            try {
                switch (fields[0]) {
                    case "INCOME" -> {
                        requireColumns(fields, 5);
                        recorder.record(new Income(parseAmount(fields[2]), parseDate(fields[1]), fields[3], fields[4]));
                        report.addImported();
                    }
                    case "EXPENSE" -> {
                        requireColumns(fields, 5);
                        recorder.record(new Expense(parseAmount(fields[2]), parseDate(fields[1]), fields[3],
                                parseCategory(fields[4])));
                        report.addImported();
                    }
                    case "ACCOUNT", "BUDGET" -> {
                        // déjà traitées par parseAccount et parseBudgets
                    }
                    default -> report.addRejection(lineNumber, "type de ligne inconnu \"" + fields[0] + "\"");
                }
            } catch (BudgetExceededException | InsufficientBalanceException e) {
                report.addRejection(lineNumber, "refusée, " + e.getMessage());
            } catch (DataLoadException | InvalidAmountException e) {
                // Le fichier vient de l'extérieur : un montant refusé par le modèle est une donnée invalide,
                // pas un bug. On le signale à la frontière de l'application au lieu d'arrêter le chargement.
                report.addRejection(lineNumber, "invalide, " + e.getMessage());
            }
        }
        return report;
    }

    /** Découpe une ligne ; renvoie un tableau vide pour les lignes vides et les commentaires. */
    private String[] split(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return new String[0];
        }
        String[] fields = trimmed.split(SEPARATOR);
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        return fields;
    }

    private void requireColumns(String[] fields, int expected) throws DataLoadException {
        if (fields.length != expected) {
            throw new DataLoadException(expected + " colonnes attendues, " + fields.length + " trouvées");
        }
    }

    private double parseAmount(String text) throws DataLoadException {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new DataLoadException("montant illisible \"" + text + "\"", e);
        }
    }

    private ExpenseCategory parseCategory(String text) throws DataLoadException {
        try {
            return ExpenseCategory.valueOf(text);
        } catch (IllegalArgumentException e) {
            throw new DataLoadException("catégorie inconnue \"" + text + "\"", e);
        }
    }

    private LocalDate parseDate(String text) throws DataLoadException {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new DataLoadException("date illisible \"" + text + "\" (format attendu : AAAA-MM-JJ)", e);
        }
    }
}