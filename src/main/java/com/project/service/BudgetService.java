package com.project.service;

import com.project.exception.BudgetExceededException;
import com.project.model.Budget;
import com.project.model.ExpenseCategory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Garde le budget mensuel de chaque catégorie et décide si une nouvelle dépense est autorisée.
 */
public class BudgetService {
    private final Map<ExpenseCategory, Budget> budgets = new HashMap<>();

    /** Ajoute un budget, ou remplace celui qui existe déjà pour la même catégorie. */
    public void setBudget(Budget budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Le budget ne peut pas être nul.");
        }
        budgets.put(budget.getCategory(), budget);
    }

    public Optional<Budget> findBudget(ExpenseCategory category) {
        return Optional.ofNullable(budgets.get(category));
    }

    /**
     * Refuse la dépense si elle fait dépasser le plafond mensuel de sa catégorie.
     * Une catégorie sans budget n'est pas limitée.
     */
    public void checkAllowed(ExpenseCategory category, double alreadySpent, double amount)
            throws BudgetExceededException {
        Optional<Budget> budget = findBudget(category);
        if (budget.isPresent() && !budget.get().allows(alreadySpent, amount)) {
            throw new BudgetExceededException(String.format(Locale.FRANCE,
                    "Budget %s dépassé : il reste %.2f € ce mois-ci, dépense de %.2f € refusée.",
                    category.getLabel(), budget.get().getRemaining(alreadySpent), amount));
        }
    }

    public Collection<Budget> getBudgets() {
        return Collections.unmodifiableCollection(budgets.values());
    }
}