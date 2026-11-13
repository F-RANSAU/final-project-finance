package com.project.model;

import com.project.exception.InsufficientBalanceException;
import com.project.exception.InvalidAmountException;

import java.time.LocalDate;

public abstract class Transaction {
    private final double amount;
    private final LocalDate date;
    private final String description;

    protected Transaction(double amount, LocalDate date, String description) {
        if (amount <= 0) {
            throw new InvalidAmountException("Le montant d'une transaction doit être strictement positif.");
        }
        if (date == null) {
            throw new IllegalArgumentException("La date d'une transaction est obligatoire.");
        }
        this.amount = amount;
        this.date = date;
        this.description = description == null ? "" : description;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public abstract void applyTo(Account account) throws InsufficientBalanceException;

    // Positif pour une entrée d'argent, négatif pour une sortie.
    public abstract double getSignedAmount();

    public abstract String getLabel();
}