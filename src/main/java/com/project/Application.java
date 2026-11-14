package com.project;

import com.project.exception.DataLoadException;
import com.project.model.Account;
import com.project.model.SavingsGoal;
import com.project.service.BudgetService;
import com.project.service.FinanceService;
import com.project.service.SpendingAnalyser;
import com.project.ui.ReportPrinter;
import com.project.utils.CsvDataLoader;
import com.project.utils.ImportReport;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class Application {
    private static final String DEFAULT_DATA_FILE = "src/main/resources/data.csv";

    public static void main(String[] args) {
        Path dataFile = Path.of(args.length > 0 ? args[0] : DEFAULT_DATA_FILE);
        CsvDataLoader loader = new CsvDataLoader(dataFile);

        try {
            // Toute la configuration est lue avant d'afficher quoi que ce soit : si elle est fausse, rien ne s'affiche.
            List<String> lines = loader.readLines();
            Account account = loader.parseAccount(lines);
            BudgetService budgetService = new BudgetService();
            loader.parseBudgets(lines).forEach(budgetService::setBudget);
            Optional<SavingsGoal> savingsGoal = loader.parseSavingsGoal(lines);

            SpendingAnalyser analyser = new SpendingAnalyser();
            FinanceService financeService = new FinanceService(account, budgetService, analyser);
            ImportReport report = loader.importTransactions(lines, financeService);

            ReportPrinter printer = new ReportPrinter(financeService, budgetService, analyser);
            printer.printWelcome();
            printer.printImportReport(report);
            printer.printStatement();
            printer.printMonthlySummaries();
            savingsGoal.ifPresent(printer::printSavingsGoal);
        } catch (DataLoadException e) {
            System.out.println("Impossible de démarrer : " + e.getMessage());
        }
    }
}