package com.project.utils;

import com.project.exception.DataLoadException;
import com.project.model.Account;
import com.project.model.Budget;
import com.project.model.ExpenseCategory;
import com.project.model.SavingsGoal;
import com.project.service.BudgetService;
import com.project.service.FinanceService;
import com.project.service.SpendingAnalyser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Les méthodes de parsing reçoivent des lignes déjà lues : pas besoin de créer de fichier pour les tester. */
class CsvDataLoaderTest {
    private CsvDataLoader loader;
    private Account account;
    private FinanceService financeService;

    @BeforeEach
    void setUp() {
        loader = new CsvDataLoader(Path.of("inutilise.csv"));
        account = new Account("FR76-0001", "Léa Martin", 100);
        BudgetService budgetService = new BudgetService();
        budgetService.setBudget(new Budget(ExpenseCategory.SHOPPING, 50));
        financeService = new FinanceService(account, budgetService, new SpendingAnalyser());
    }

    @Test
    void accountIsReadFromItsLine() throws DataLoadException {
        Account loaded = loader.parseAccount(List.of("# commentaire", "ACCOUNT;FR76-9;Léa Martin;850.50"));

        assertEquals("FR76-9", loaded.getAccountNumber());
        assertEquals("Léa Martin", loaded.getOwner());
        assertEquals(850.50, loaded.getBalance(), 0.001);
    }

    @Test
    void missingAccountLineStopsTheLoading() {
        assertThrows(DataLoadException.class, () -> loader.parseAccount(List.of("BUDGET;SHOPPING;100")));
    }

    @Test
    void accountWithMissingColumnStopsTheLoading() {
        assertThrows(DataLoadException.class, () -> loader.parseAccount(List.of("ACCOUNT;FR76-9;Léa Martin")));
    }

    @Test
    void budgetsAreRead() throws DataLoadException {
        List<Budget> budgets = loader.parseBudgets(List.of("BUDGET;SHOPPING;150", "", "BUDGET;LEISURE;80.5"));

        assertEquals(2, budgets.size());
        assertEquals(ExpenseCategory.LEISURE, budgets.get(1).getCategory());
        assertEquals(80.5, budgets.get(1).getMonthlyLimit(), 0.001);
    }

    @Test
    void budgetWithUnknownCategoryStopsTheLoading() {
        DataLoadException exception = assertThrows(DataLoadException.class,
                () -> loader.parseBudgets(List.of("BUDGET;VOYAGES;150")));

        assertTrue(exception.getMessage().startsWith("Ligne 1"));
    }

    @Test
    void savingsGoalIsReadFromItsLine() throws DataLoadException {
        Optional<SavingsGoal> goal = loader.parseSavingsGoal(List.of("BUDGET;SHOPPING;150", "GOAL;5000"));

        assertEquals(5000, goal.orElseThrow().getTargetAmount(), 0.001);
    }

    @Test
    void savingsGoalIsEmptyWhenTheFileHasNoGoalLine() throws DataLoadException {
        assertTrue(loader.parseSavingsGoal(List.of("BUDGET;SHOPPING;150")).isEmpty());
    }

    @Test
    void invalidSavingsGoalStopsTheLoading() {
        assertThrows(DataLoadException.class, () -> loader.parseSavingsGoal(List.of("GOAL;-50")));
        assertThrows(DataLoadException.class, () -> loader.parseSavingsGoal(List.of("GOAL;abc")));
    }

    @Test
    void validTransactionsAreImportedInOrder() {
        ImportReport report = loader.importTransactions(List.of(
                "INCOME;2026-11-01;200;Salaire;Employeur",
                "EXPENSE;2026-11-02;40;Robe;SHOPPING"), financeService);

        assertEquals(2, report.getImportedCount());
        assertTrue(report.getRejections().isEmpty());
        assertEquals(260, account.getBalance(), 0.001);
    }

    @Test
    void invalidLinesAreRejectedButTheOthersAreStillImported() {
        ImportReport report = loader.importTransactions(List.of(
                "EXPENSE;2026-11-02;abc;Montant faux;SHOPPING",
                "EXPENSE;02/11/2026;10;Date fausse;SHOPPING",
                "EXPENSE;2026-11-02;10;Catégorie fausse;SPORT",
                "EXPENSE;2026-11-02;-5;Montant négatif;SHOPPING",
                "EXPENSE;2026-11-02;10;Colonne manquante",
                "TRANSFER;2026-11-02;10;Type inconnu;X",
                "EXPENSE;2026-11-02;20;Valide;SHOPPING"), financeService);

        assertEquals(1, report.getImportedCount());
        assertEquals(6, report.getRejections().size());
        assertEquals(80, account.getBalance(), 0.001);
    }

    @Test
    void businessRefusalsAreReportedWithTheirLineNumber() {
        ImportReport report = loader.importTransactions(List.of(
                "EXPENSE;2026-11-02;60;Trop cher pour le budget;SHOPPING",
                "EXPENSE;2026-11-03;500;Trop cher pour le solde;HOUSING_AND_BILLS"), financeService);

        assertEquals(0, report.getImportedCount());
        assertTrue(report.getRejections().get(0).startsWith("Ligne 1 : refusée"));
        assertTrue(report.getRejections().get(1).startsWith("Ligne 2 : refusée"));
        assertEquals(100, account.getBalance(), 0.001);
    }

    @Test
    void missingFileIsReportedAsDataLoadException() {
        CsvDataLoader missingFileLoader = new CsvDataLoader(Path.of("fichier-qui-n-existe-pas.csv"));

        assertThrows(DataLoadException.class, missingFileLoader::readLines);
    }

    @Test
    void realDataFileCanBeLoaded() throws DataLoadException {
        CsvDataLoader realLoader = new CsvDataLoader(Path.of("src/main/resources/data.csv"));

        List<String> lines = realLoader.readLines();

        assertEquals("Léa Martin", realLoader.parseAccount(lines).getOwner());
        assertEquals(5, realLoader.parseBudgets(lines).size());
    }
}