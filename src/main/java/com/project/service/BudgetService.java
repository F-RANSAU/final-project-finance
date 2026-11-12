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


public class BudgetService {
    private final Map<ExpenseCategory, Budget> budgets = new HashMap<>();

    public void setBudget(Budget budget) {
        if (budget == null) {
            throw new IllegalArgumentException("Le budget ne peut pas être nul.");
        }
        budgets.put(budget.getCategory(), budget);
    }

    public Optional<Budget> findBudget(ExpenseCategory category) {
        return Optional.ofNullable(budgets.get(category));
    }

    public void checkAllowed(ExpenseCategory category, double alreadySpent, double amount)
            throws BudgetExceededException {
        // Une catégorie sans budget n'est pas limitée.
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