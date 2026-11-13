package com.project.model;

import com.project.exception.InvalidAmountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SavingsGoalTest {
    private SavingsGoal goal;

    @BeforeEach
    void setUp() {
        goal = new SavingsGoal(5000);
    }

    @Test
    void progressIsTheShareOfTheTargetAlreadySaved() {
        assertEquals(50, goal.getProgressPercentage(2500), 0.001);
        assertEquals(2500, goal.getRemaining(2500), 0.001);
        assertFalse(goal.isReached(2500));
    }

    @Test
    void emptyAccountMeansNoProgress() {
        assertEquals(0, goal.getProgressPercentage(0), 0.001);
        assertEquals(5000, goal.getRemaining(0), 0.001);
    }

    @Test
    void goalIsReachedWhenBalanceEqualsTheTarget() {
        assertTrue(goal.isReached(5000));
        assertEquals(100, goal.getProgressPercentage(5000), 0.001);
        assertEquals(0, goal.getRemaining(5000), 0.001);
    }

    @Test
    void goalIsNotReachedOneCentBelowTheTarget() {
        assertFalse(goal.isReached(4999.99));
    }

    @Test
    void progressNeverGoesAboveOneHundredPercent() {
        assertEquals(100, goal.getProgressPercentage(6200), 0.001);
        assertEquals(0, goal.getRemaining(6200), 0.001);
    }

    @Test
    void zeroOrNegativeTargetIsRefused() {
        assertThrows(InvalidAmountException.class, () -> new SavingsGoal(0));
        assertThrows(InvalidAmountException.class, () -> new SavingsGoal(-100));
    }
}