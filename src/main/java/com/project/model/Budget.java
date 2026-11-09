package com.project.model;

import com.project.exception.InvalidAmountException;

public class Budget {
    private final ExpenseCategory category;
    private double monthlyLimit;

    public Budget(ExpenseCategory category, double monthlyLimit) {
        if (category == null) {
            throw new IllegalArgumentException("La catégorie d'un budget est obligatoire.");
        }
        this.category = category;
        setMonthlyLimit(monthlyLimit);
    }

    /** Montant encore disponible ce mois-ci, sachant ce qui a déjà été dépensé. */
    public double getRemaining(double alreadySpent) {
        return Math.max(0, monthlyLimit - alreadySpent);
    }

    /** Une dépense est autorisée tant qu'elle ne fait pas dépasser le plafond (atteindre pile le plafond est permis). */
    public boolean allows(double alreadySpent, double newAmount) {
        return alreadySpent + newAmount <= monthlyLimit;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        if (monthlyLimit < 0) {
            throw new InvalidAmountException("Le plafond d'un budget ne peut pas être négatif.");
        }
        this.monthlyLimit = monthlyLimit;
    }
}