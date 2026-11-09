package com.project;

import com.project.exception.DataLoadException;
import com.project.model.Account;
import com.project.service.BudgetService;
import com.project.service.FinanceService;
import com.project.service.SpendingAnalyser;
import com.project.ui.ReportPrinter;
import com.project.utils.CsvDataLoader;
import com.project.utils.ImportReport;

import java.nio.file.Path;
import java.util.List;

/**
 * Point d'entrée : assemble les objets puis déroule le scénario
 * chargement → règles métier → affichage. Aucun calcul n'est fait ici.
 */
public class Application {
    private static final String DEFAULT_DATA_FILE = "src/main/resources/data.csv";

    public static void main(String[] args) {
        Path dataFile = Path.of(args.length > 0 ? args[0] : DEFAULT_DATA_FILE);
        CsvDataLoader loader = new CsvDataLoader(dataFile);

        try {
            List<String> lines = loader.readLines();
            Account account = loader.parseAccount(lines);

            BudgetService budgetService = new BudgetService();
            loader.parseBudgets(lines).forEach(budgetService::setBudget);

            SpendingAnalyser analyser = new SpendingAnalyser();
            FinanceService financeService = new FinanceService(account, budgetService, analyser);
            ImportReport report = loader.importTransactions(lines, financeService);

            ReportPrinter printer = new ReportPrinter(financeService, budgetService, analyser);
            printer.printWelcome();
            printer.printImportReport(report);
            printer.printStatement();
            printer.printMonthlySummaries();
        } catch (DataLoadException e) {
            System.out.println("Impossible de démarrer : " + e.getMessage());
        }
    }
}