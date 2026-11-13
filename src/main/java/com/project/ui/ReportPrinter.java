package com.project.ui;

import com.project.model.Budget;
import com.project.model.Expense;
import com.project.model.ExpenseCategory;
import com.project.model.Transaction;
import com.project.service.BudgetService;
import com.project.service.FinanceService;
import com.project.service.SpendingAnalyser;
import com.project.utils.ImportReport;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;


public class ReportPrinter {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRANCE);

    private final FinanceService financeService;
    private final BudgetService budgetService;
    private final SpendingAnalyser analyser;

    public ReportPrinter(FinanceService financeService, BudgetService budgetService, SpendingAnalyser analyser) {
        this.financeService = financeService;
        this.budgetService = budgetService;
        this.analyser = analyser;
    }

    public void printWelcome() {
        System.out.println("==============================================");
        System.out.println("  Mes finances — compte de " + financeService.getAccount().getOwner());
        System.out.println("==============================================");
    }

    public void printImportReport(ImportReport report) {
        System.out.println();
        System.out.println("Chargement des données : " + report.getImportedCount() + " transactions importées, "
                + report.getRejections().size() + " lignes écartées.");
        report.getRejections().forEach(rejection -> System.out.println(rejection));
    }

    public void printStatement() {
        System.out.println();
        System.out.println("--- Relevé de compte ---");
        for (Transaction transaction : financeService.getHistory()) {
            System.out.println(format("%s  %-24s %-26s %+10.2f €",
                    transaction.getDate().format(DATE_FORMAT),
                    transaction.getLabel(),
                    transaction.getDescription(),
                    transaction.getSignedAmount()));
        }
        System.out.println(format("Solde actuel : %.2f €", financeService.getAccount().getBalance()));
    }

    public void printMonthlySummaries() {
        List<Transaction> history = financeService.getHistory();
        for (YearMonth month : analyser.monthsCovered(history)) {
            printMonthlySummary(month);
        }
    }

    private void printMonthlySummary(YearMonth month) {
        List<Expense> expenses = financeService.getExpenses();
        System.out.println();
        System.out.println("--- Bilan : " + month.format(MONTH_FORMAT) + " ---");
        System.out.println(format("Dépenses totales : %.2f €", analyser.totalExpenses(expenses, month)));
        System.out.println(format("Épargne du mois (entrées - sorties) : %+.2f €",
                analyser.netCashFlow(financeService.getHistory(), month)));

        Optional<Expense> largest = analyser.largestExpense(expenses, month);
        largest.ifPresent(expense -> System.out.println(format(
                "Plus grosse dépense : %s (%.2f €)", expense.getDescription(), expense.getAmount())));

        Map<ExpenseCategory, Double> spending = analyser.spendingByCategory(expenses, month);
        System.out.println("Budgets :");
        for (ExpenseCategory category : ExpenseCategory.values()) {
            double spent = spending.getOrDefault(category, 0.0);
            Optional<Budget> budget = budgetService.findBudget(category);
            if (budget.isPresent()) {
                printBudgetLine(budget.get(), spent);
            } else if (spent > 0) {
                System.out.println(format("  %-24s %8.2f €  (pas de budget)",
                        category.getLabel(), spent));
            }
        }
    }

    private void printBudgetLine(Budget budget, double spent) {
        double usage = budget.getUsagePercentage(spent);
        String status = budget.isNearlyExhausted(spent) ? "  ⚠ bientôt atteint" : "";
        System.out.println(format("  %-24s %8.2f / %.2f €  (%.0f %%, reste %.2f €)%s",
                budget.getCategory().getLabel(), spent, budget.getMonthlyLimit(), usage,
                budget.getRemaining(spent), status));
    }

    // Locale.FRANCE : virgule décimale, comme dans le reste de l'affichage.
    private String format(String pattern, Object... values) {
        return String.format(Locale.FRANCE, pattern, values);
    }

}