package com.project.model;

import com.project.exception.InsufficientBalanceException;
import com.project.exception.InvalidAmountException;

import java.util.Locale;

public class Account {
    private final String accountNumber;
    private String owner;
    private double balance;

    public Account(String accountNumber, String owner, double initialBalance) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Le numéro de compte ne peut pas être vide.");
        }
        if (initialBalance < 0) {
            throw new InvalidAmountException("Le solde initial ne peut pas être négatif.");
        }
        this.accountNumber = accountNumber;
        setOwner(owner);
        this.balance = initialBalance;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException("Le montant du dépôt doit être strictement positif.");
        }
        balance += amount;
    }

    public void withdraw(double amount) throws InsufficientBalanceException {
        if (amount <= 0) {
            throw new InvalidAmountException("Le montant du retrait doit être strictement positif.");
        }
        if (amount > balance) {
            throw new InsufficientBalanceException(String.format(Locale.FRANCE,
                    "Solde insuffisant pour retirer %.2f €. Solde actuel : %.2f €.", amount, balance));
        }
        balance -= amount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("Le titulaire du compte ne peut pas être vide.");
        }
        this.owner = owner;
    }

    public double getBalance() {
        return balance;
    }
}