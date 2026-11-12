package com.project.model;

import com.project.exception.InvalidAmountException;

public class Budget {
    private static final double WARNING_THRESHOLD = 80;

    private final ExpenseCategory category;
    private double monthlyLimit;

    public Budget(ExpenseCategory category, double monthlyLimit) {
        if (category == null) {
            throw new IllegalArgumentException("La catégorie d'un budget est obligatoire.");
        }
        this.category = category;
        setMonthlyLimit(monthlyLimit);
    }

    public double getRemaining(double alreadySpent) {
        return Math.max(0, monthlyLimit - alreadySpent);
    }

    public boolean allows(double alreadySpent, double newAmount) {
        // Atteindre exactement le plafond reste autorisé.
        return alreadySpent + newAmount <= monthlyLimit;
    }

    public double getUsagePercentage(double alreadySpent) {
        // Évite la division par zéro : un plafond nul est consommé dès la première dépense.*/
        if (monthlyLimit == 0) {
            return alreadySpent > 0 ? 100 : 0;
        }
        return alreadySpent * 100 / monthlyLimit;
    }

    public boolean isNearlyExhausted(double alreadySpent) {
        return getUsagePercentage(alreadySpent) >= WARNING_THRESHOLD;
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