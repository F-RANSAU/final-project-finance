package com.project.model;

import com.project.exception.InvalidAmountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BudgetTest {
    private Budget budget;

    @BeforeEach
    void setUp() {
        budget = new Budget(ExpenseCategory.SHOPPING, 150);
    }

    @Test
    void expenseWithinLimitIsAllowed() {
        assertTrue(budget.allows(100, 30));
    }

    @Test
    void expenseReachingExactlyTheLimitIsAllowed() {
        assertTrue(budget.allows(100, 50));
    }

    @Test
    void expenseGoingOverTheLimitIsNotAllowed() {
        assertFalse(budget.allows(100, 50.01));
    }

    @Test
    void remainingIsNeverNegative() {
        assertEquals(30, budget.getRemaining(120), 0.001);
        assertEquals(0, budget.getRemaining(200), 0.001);
    }

    @Test
    void warningStartsAtEightyPercent() {
        assertFalse(budget.isNearlyExhausted(119.99));
        assertTrue(budget.isNearlyExhausted(120));
    }

    @Test
    void zeroLimitIsFullyUsedByAnyExpense() {
        Budget noSpending = new Budget(ExpenseCategory.LEISURE, 0);

        assertEquals(0, noSpending.getUsagePercentage(0), 0.001);
        assertEquals(100, noSpending.getUsagePercentage(5), 0.001);
        assertFalse(noSpending.allows(0, 0.01));
    }

    @Test
    void negativeLimitIsRefused() {
        assertThrows(InvalidAmountException.class, () -> new Budget(ExpenseCategory.SHOPPING, -10));
        assertThrows(InvalidAmountException.class, () -> budget.setMonthlyLimit(-1));
        assertEquals(150, budget.getMonthlyLimit(), 0.001);
    }
}