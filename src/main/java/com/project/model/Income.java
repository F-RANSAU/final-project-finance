package com.project.model;

import java.time.LocalDate;

public class Income extends Transaction {
    private final String source;

    public Income(double amount, LocalDate date, String description, String source) {
        super(amount, date, description);
        this.source = (source == null || source.isBlank()) ? "Autre" : source;
    }

    public String getSource() {
        return source;
    }

    /**
     * Pas de "throws" ici : un dépôt ne peut pas échouer par manque de solde.
     * Une sous-classe a le droit de lever moins d'exceptions que sa classe parente.
     */
    @Override
    public void applyTo(Account account) {
        account.deposit(getAmount());
    }

    @Override
    public double getSignedAmount() {
        return getAmount();
    }

    @Override
    public String getLabel() {
        return "Revenu (" + source + ")";
    }
}