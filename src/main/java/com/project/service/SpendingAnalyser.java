package com.project.service;

import com.project.model.Expense;
import com.project.model.ExpenseCategory;
import com.project.model.Transaction;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Calculs mensuels. Cette classe ne garde aucun état : elle lit seulement les listes qu'on lui donne.
 */
public class SpendingAnalyser {

    public double totalExpenses(List<Expense> expenses, YearMonth month) {
        return expenses.stream()
                .filter(e -> isIn(e.getDate(), month))
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public double spentInCategory(List<Expense> expenses, ExpenseCategory category, YearMonth month) {
        return expenses.stream()
                .filter(e -> isIn(e.getDate(), month))
                .filter(e -> e.getCategory() == category)
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    public Map<ExpenseCategory, Double> spendingByCategory(List<Expense> expenses, YearMonth month) {
        return expenses.stream()
                .filter(e -> isIn(e.getDate(), month))
                .collect(Collectors.groupingBy(Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)));
    }

    public Optional<Expense> largestExpense(List<Expense> expenses, YearMonth month) {
        return expenses.stream()
                .filter(e -> isIn(e.getDate(), month))
                .max(Comparator.comparingDouble(Expense::getAmount));
    }

    /** Entrées moins sorties sur le mois : marche pour tout type de transaction grâce à getSignedAmount(). */
    public double netCashFlow(List<Transaction> transactions, YearMonth month) {
        return transactions.stream()
                .filter(t -> isIn(t.getDate(), month))
                .mapToDouble(Transaction::getSignedAmount)
                .sum();
    }

    private boolean isIn(LocalDate date, YearMonth month) {
        return YearMonth.from(date).equals(month);
    }
}