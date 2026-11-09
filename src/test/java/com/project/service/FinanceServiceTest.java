package com.project.service;

import com.project.exception.BudgetExceededException;
import com.project.exception.InsufficientBalanceException;
import com.project.model.Account;
import com.project.model.Budget;
import com.project.model.Expense;
import com.project.model.ExpenseCategory;
import com.project.model.Income;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FinanceServiceTest {
    private Account account;
    private FinanceService financeService;

    @BeforeEach
    void setUp() {
        account = new Account("FR76-0001", "Léa Martin", 1000);
        BudgetService budgetService = new BudgetService();
        budgetService.setBudget(new Budget(ExpenseCategory.SHOPPING, 100));
        financeService = new FinanceService(account, budgetService, new SpendingAnalyser());
    }

    @Test
    void expenseWithinBudgetIsRecorded() throws BudgetExceededException, InsufficientBalanceException {
        financeService.record(new Expense(60, LocalDate.of(2026, 11, 2), "Robe", ExpenseCategory.SHOPPING));

        assertEquals(940, account.getBalance(), 0.001);
        assertEquals(1, financeService.getHistory().size());
    }

    @Test
    void expenseReachingExactlyTheBudgetIsAllowed()
            throws BudgetExceededException, InsufficientBalanceException {
        financeService.record(new Expense(60, LocalDate.of(2026, 11, 2), "Robe", ExpenseCategory.SHOPPING));
        financeService.record(new Expense(40, LocalDate.of(2026, 11, 5), "Sac", ExpenseCategory.SHOPPING));

        assertEquals(900, account.getBalance(), 0.001);
    }

    @Test
    void expenseOverBudgetIsRefusedAndNothingChanges()
            throws BudgetExceededException, InsufficientBalanceException {
        financeService.record(new Expense(60, LocalDate.of(2026, 11, 2), "Robe", ExpenseCategory.SHOPPING));

        assertThrows(BudgetExceededException.class, () -> financeService.record(
                new Expense(50, LocalDate.of(2026, 11, 5), "Sac", ExpenseCategory.SHOPPING)));

        assertEquals(940, account.getBalance(), 0.001);
        assertEquals(1, financeService.getHistory().size());
        assertEquals(1, financeService.getExpenses().size());
    }

    @Test
    void budgetStartsAgainEachMonth() throws BudgetExceededException, InsufficientBalanceException {
        financeService.record(new Expense(100, LocalDate.of(2026, 10, 31), "Manteau", ExpenseCategory.SHOPPING));
        financeService.record(new Expense(100, LocalDate.of(2026, 11, 1), "Bottes", ExpenseCategory.SHOPPING));

        assertEquals(800, account.getBalance(), 0.001);
    }

    @Test
    void expenseAboveBalanceIsRefusedAndNothingChanges() {
        assertThrows(InsufficientBalanceException.class, () -> financeService.record(
                new Expense(2000, LocalDate.of(2026, 11, 2), "Travaux", ExpenseCategory.HOUSING_AND_BILLS)));

        assertEquals(1000, account.getBalance(), 0.001);
        assertTrue(financeService.getHistory().isEmpty());
    }

    @Test
    void incomeIncreasesBalanceAndIsKeptInHistory() {
        financeService.record(new Income(1500, LocalDate.of(2026, 11, 1), "Salaire", "Employeur"));

        assertEquals(2500, account.getBalance(), 0.001);
        assertEquals(1, financeService.getHistory().size());
        assertTrue(financeService.getExpenses().isEmpty());
    }

    @Test
    void historyCannotBeModifiedFromOutside() {
        assertThrows(UnsupportedOperationException.class, () -> financeService.getHistory().clear());
    }
}