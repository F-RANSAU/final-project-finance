package com.project.model;

import com.project.exception.InsufficientBalanceException;
import com.project.exception.InvalidAmountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {
    private static final LocalDate DATE = LocalDate.of(2026, 11, 2);
    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account("FR76-0001", "Léa Martin", 500);
    }

    @Test
    void eachTransactionTypeAppliesItsOwnEffectOnTheAccount() throws InsufficientBalanceException {
        List<Transaction> transactions = List.of(
                new Income(1000, DATE, "Salaire", "Employeur"),
                new Expense(200, DATE, "Courses", ExpenseCategory.FOOD_AND_DINING));

        for (Transaction transaction : transactions) {
            transaction.applyTo(account);
        }

        assertEquals(1300, account.getBalance(), 0.001);
    }

    @Test
    void signedAmountIsPositiveForIncomeAndNegativeForExpense() {
        Transaction income = new Income(80, DATE, "Remboursement", "Amie");
        Transaction expense = new Expense(30, DATE, "Cinéma", ExpenseCategory.LEISURE);

        assertEquals(80, income.getSignedAmount(), 0.001);
        assertEquals(-30, expense.getSignedAmount(), 0.001);
    }

    @Test
    void expenseAboveBalanceIsRefused() {
        Transaction expense = new Expense(600, DATE, "Canapé", ExpenseCategory.HOUSING_AND_BILLS);

        assertThrows(InsufficientBalanceException.class, () -> expense.applyTo(account));
        assertEquals(500, account.getBalance(), 0.001);
    }

    @Test
    void transactionWithZeroAmountIsRefused() {
        assertThrows(InvalidAmountException.class, () -> new Income(0, DATE, "Rien", "Personne"));
    }

    @Test
    void transactionWithoutDateIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> new Expense(10, null, "Café", ExpenseCategory.FOOD_AND_DINING));
    }

    @Test
    void expenseWithoutCategoryIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new Expense(10, DATE, "Café", null));
    }

    @Test
    void incomeWithoutSourceGetsADefaultSource() {
        Income income = new Income(50, DATE, "Cadeau", " ");

        assertEquals("Autre", income.getSource());
    }
}