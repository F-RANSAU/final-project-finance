package com.project.exception;

public class BudgetExceededException extends Exception {
    public BudgetExceededException(String message) {
        super(message);
    }
}