package com.fkwallet.model;

import java.math.BigDecimal;

public class FixedDeposit {
    private final BigDecimal fdAmount;
    private int remainingTxs;

    public FixedDeposit(BigDecimal fdAmount) {
        this.fdAmount = fdAmount;
        this.remainingTxs = 5;
    }

    public BigDecimal getFdAmount() { return fdAmount; }
    public int getRemainingTxs() { return remainingTxs; }
    public void decrement() { remainingTxs--; }
    public boolean isComplete() { return remainingTxs <= 0; }
}
