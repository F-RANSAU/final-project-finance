package com.project.model;

import com.project.exception.InsufficientBalanceException;

import java.time.LocalDate;

public class Expense extends Transaction implements Categorisable {
    private final ExpenseCategory category;

    public Expense(double amount, LocalDate date, String description, ExpenseCategory category) {
        super(amount, date, description);
        if (category == null) {
            throw new IllegalArgumentException("Une dépense doit avoir une catégorie.");
        }
        this.category = category;
    }

    @Override
    public ExpenseCategory getCategory() {
        return category;
    }

    @Override
    public void applyTo(Account account) throws InsufficientBalanceException {
        account.withdraw(getAmount());
    }

    @Override
    public double getSignedAmount() {
        return -getAmount();
    }

    @Override
    public String getLabel() {
        return category.getLabel();
    }
}