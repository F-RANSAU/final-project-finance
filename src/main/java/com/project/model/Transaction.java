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

    /**
     * Chaque type de transaction sait comment il modifie le solde d'un compte :
     * le service n'a donc pas besoin de tester le type concret.
     */
    public abstract void applyTo(Account account) throws InsufficientBalanceException;

    /** Effet sur le solde : positif quand l'argent entre, négatif quand il sort. */
    public abstract double getSignedAmount();

    /** Libellé court affiché sur le relevé de compte. */
    public abstract String getLabel();
}