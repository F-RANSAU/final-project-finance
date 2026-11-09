package com.project.service;

import com.project.exception.BudgetExceededException;
import com.project.model.Budget;
import com.project.model.ExpenseCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BudgetServiceTest {
    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        budgetService = new BudgetService();
        budgetService.setBudget(new Budget(ExpenseCategory.SHOPPING, 150));
    }

    @Test
    void expenseWithinBudgetPassesTheCheck() {
        assertDoesNotThrow(() -> budgetService.checkAllowed(ExpenseCategory.SHOPPING, 100, 50));
    }

    @Test
    void expenseOverBudgetIsRefusedWithAClearMessage() {
        BudgetExceededException exception = assertThrows(BudgetExceededException.class,
                () -> budgetService.checkAllowed(ExpenseCategory.SHOPPING, 120, 45));

        assertTrue(exception.getMessage().contains("Shopping"));
    }

    @Test
    void categoryWithoutBudgetIsNotLimited() {
        assertDoesNotThrow(() -> budgetService.checkAllowed(ExpenseCategory.HOUSING_AND_BILLS, 0, 5000));
    }

    @Test
    void settingABudgetTwiceReplacesTheFirstOne() {
        budgetService.setBudget(new Budget(ExpenseCategory.SHOPPING, 300));

        assertEquals(1, budgetService.getBudgets().size());
        assertEquals(300, budgetService.findBudget(ExpenseCategory.SHOPPING).orElseThrow().getMonthlyLimit(), 0.001);
    }

    @Test
    void findBudgetIsEmptyForUnknownCategory() {
        assertTrue(budgetService.findBudget(ExpenseCategory.TRANSPORT).isEmpty());
    }
}