package com.project.model;

import com.project.exception.InvalidAmountException;

public class SavingsGoal {
    private final double targetAmount;

    public SavingsGoal(double targetAmount) {
        if (targetAmount <= 0) {
            throw new InvalidAmountException("Le montant d'un objectif d'épargne doit être strictement positif.");
        }
        this.targetAmount = targetAmount;
    }

    public boolean isReached(double currentBalance) {
        return currentBalance >= targetAmount;
    }

    public double getProgressPercentage(double currentBalance) {
        // Plafonné à 100 : un solde supérieur à l'objectif ne donne pas une progression de 120 %.
        return Math.min(100, currentBalance * 100 / targetAmount);
    }

    public double getRemaining(double currentBalance) {
        return Math.max(0, targetAmount - currentBalance);
    }

    public double getTargetAmount() {
        return targetAmount;
    }
}