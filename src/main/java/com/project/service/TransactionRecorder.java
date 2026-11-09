package com.project.service;

import com.project.exception.BudgetExceededException;
import com.project.exception.InsufficientBalanceException;
import com.project.model.Expense;
import com.project.model.Income;

/**
 * Ce dont une classe a besoin pour ajouter des transactions, et rien de plus.
 * Le lecteur CSV dépendra de cette interface, pas de FinanceService directement.
 */
public interface TransactionRecorder {
    void record(Expense expense) throws BudgetExceededException, InsufficientBalanceException;

    void record(Income income);
}