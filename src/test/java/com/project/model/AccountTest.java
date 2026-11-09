package com.project.model;

import com.project.exception.InsufficientBalanceException;
import com.project.exception.InvalidAmountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {
    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account("FR76-0001", "Léa Martin", 1000);
    }

    @Test
    void depositIncreasesBalance() {
        account.deposit(200);

        assertEquals(1200, account.getBalance(), 0.001);
    }

    @Test
    void withdrawalDecreasesBalance() throws InsufficientBalanceException {
        account.withdraw(300);

        assertEquals(700, account.getBalance(), 0.001);
    }

    @Test
    void withdrawingExactBalanceLeavesZero() throws InsufficientBalanceException {
        account.withdraw(1000);

        assertEquals(0, account.getBalance(), 0.001);
    }

    @Test
    void withdrawalAboveBalanceIsRefusedAndBalanceIsUnchanged() {
        assertThrows(InsufficientBalanceException.class, () -> account.withdraw(1000.01));

        assertEquals(1000, account.getBalance(), 0.001);
    }

    @Test
    void zeroOrNegativeDepositIsRefused() {
        assertThrows(InvalidAmountException.class, () -> account.deposit(0));
        assertThrows(InvalidAmountException.class, () -> account.deposit(-50));
        assertEquals(1000, account.getBalance(), 0.001);
    }

    @Test
    void negativeWithdrawalIsRefused() {
        assertThrows(InvalidAmountException.class, () -> account.withdraw(-20));
    }

    @Test
    void accountCannotBeCreatedWithNegativeBalance() {
        assertThrows(InvalidAmountException.class, () -> new Account("FR76-0002", "Léa Martin", -1));
    }

    @Test
    void accountCannotBeCreatedWithoutNumber() {
        assertThrows(IllegalArgumentException.class, () -> new Account(" ", "Léa Martin", 100));
    }

    @Test
    void accountCannotBeCreatedWithoutOwner() {
        assertThrows(IllegalArgumentException.class, () -> new Account("FR76-0002", null, 100));
    }
}