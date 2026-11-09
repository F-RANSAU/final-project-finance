package com.project.service;

import com.project.model.Expense;
import com.project.model.ExpenseCategory;
import com.project.model.Income;
import com.project.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SpendingAnalyserTest {
    private static final YearMonth NOVEMBER = YearMonth.of(2026, 11);

    private SpendingAnalyser analyser;
    private Expense rent;
    private Expense boots;
    private Expense dress;
    private Expense octoberCoat;
    private Income salary;

    @BeforeEach
    void setUp() {
        analyser = new SpendingAnalyser();
        rent = new Expense(650, LocalDate.of(2026, 11, 2), "Loyer", ExpenseCategory.HOUSING_AND_BILLS);
        boots = new Expense(120, LocalDate.of(2026, 11, 6), "Bottes", ExpenseCategory.SHOPPING);
        dress = new Expense(30, LocalDate.of(2026, 11, 30), "Robe", ExpenseCategory.SHOPPING);
        octoberCoat = new Expense(90, LocalDate.of(2026, 10, 31), "Manteau", ExpenseCategory.SHOPPING);
        salary = new Income(1850, LocalDate.of(2026, 11, 1), "Salaire", "Employeur");
    }

    @Test
    void totalExpensesOnlyCountsTheRequestedMonth() {
        double total = analyser.totalExpenses(List.of(rent, boots, dress, octoberCoat), NOVEMBER);

        assertEquals(800, total, 0.001);
    }

    @Test
    void spentInCategoryIncludesFirstAndLastDayOfMonth() {
        double spent = analyser.spentInCategory(List.of(boots, dress, octoberCoat), ExpenseCategory.SHOPPING, NOVEMBER);

        assertEquals(150, spent, 0.001);
    }

    @Test
    void spendingIsGroupedByCategory() {
        Map<ExpenseCategory, Double> spending = analyser.spendingByCategory(List.of(rent, boots, dress), NOVEMBER);

        assertEquals(2, spending.size());
        assertEquals(650, spending.get(ExpenseCategory.HOUSING_AND_BILLS), 0.001);
        assertEquals(150, spending.get(ExpenseCategory.SHOPPING), 0.001);
    }

    @Test
    void largestExpenseIsFound() {
        assertEquals(rent, analyser.largestExpense(List.of(boots, rent, dress), NOVEMBER).orElseThrow());
    }

    @Test
    void largestExpenseIsEmptyForAMonthWithoutExpenses() {
        assertTrue(analyser.largestExpense(List.of(octoberCoat), NOVEMBER).isEmpty());
    }

    @Test
    void netCashFlowAddsIncomeAndSubtractsExpenses() {
        List<Transaction> transactions = List.of(salary, rent, boots);

        assertEquals(1080, analyser.netCashFlow(transactions, NOVEMBER), 0.001);
    }

    @Test
    void monthsCoveredAreSortedWithoutDuplicates() {
        List<Transaction> transactions = List.of(salary, octoberCoat, rent);

        assertEquals(List.of(YearMonth.of(2026, 10), NOVEMBER), List.copyOf(analyser.monthsCovered(transactions)));
    }
}