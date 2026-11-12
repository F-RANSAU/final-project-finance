package com.project.service;

import com.project.exception.BudgetExceededException;
import com.project.exception.InsufficientBalanceException;
import com.project.model.Expense;
import com.project.model.Income;

/** Seule partie de FinanceService dont le lecteur CSV a besoin. */
public interface TransactionRecorder {
    void record(Expense expense) throws BudgetExceededException, InsufficientBalanceException;

    void record(Income income);
}