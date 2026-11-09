package com.project.service;

import com.project.exception.BudgetExceededException;
import com.project.exception.InsufficientBalanceException;
import com.project.model.Account;
import com.project.model.Expense;
import com.project.model.Income;
import com.project.model.Transaction;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Enregistre les transactions sur un compte en faisant respecter les règles métier
 * (budgets mensuels et solde suffisant).
 */
public class FinanceService implements TransactionRecorder {
    private final Account account;
    private final BudgetService budgetService;
    private final SpendingAnalyser analyser;
    private final List<Transaction> history = new ArrayList<>();
    private final List<Expense> expenses = new ArrayList<>();

    public FinanceService(Account account, BudgetService budgetService, SpendingAnalyser analyser) {
        if (account == null || budgetService == null || analyser == null) {
            throw new IllegalArgumentException("Le compte et les services sont obligatoires.");
        }
        this.account = account;
        this.budgetService = budgetService;
        this.analyser = analyser;
    }

    /**
     * Le budget est vérifié avant de toucher au compte : si la dépense est refusée,
     * ni le solde ni l'historique ne changent.
     */
    @Override
    public void record(Expense expense) throws BudgetExceededException, InsufficientBalanceException {
        if (expense == null) {
            throw new IllegalArgumentException("La dépense ne peut pas être nulle.");
        }
        YearMonth month = YearMonth.from(expense.getDate());
        double alreadySpent = analyser.spentInCategory(expenses, expense.getCategory(), month);
        budgetService.checkAllowed(expense.getCategory(), alreadySpent, expense.getAmount());

        expense.applyTo(account);
        history.add(expense);
        expenses.add(expense);
    }

    @Override
    public void record(Income income) {
        if (income == null) {
            throw new IllegalArgumentException("Le revenu ne peut pas être nul.");
        }
        income.applyTo(account);
        history.add(income);
    }

    public Account getAccount() {
        return account;
    }

    /** Toutes les transactions, dans l'ordre où elles ont été enregistrées. */
    public List<Transaction> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public List<Expense> getExpenses() {
        return Collections.unmodifiableList(expenses);
    }
}