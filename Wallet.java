package com.fkwallet.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Wallet {
    private final String owner;
    private BigDecimal balance;
    private final Instant createdAt;
    private final List<Transaction> transactions = new ArrayList<>();
    private FixedDeposit fixedDeposit;
    private final long creationOrder;

    public Wallet(String owner, BigDecimal initial, long creationOrder) {
        this.owner = owner;
        this.balance = initial;
        this.createdAt = Instant.now();
        this.creationOrder = creationOrder;
    }

    public String getOwner() { return owner; }
    public BigDecimal getBalance() { return balance; }
    public Instant getCreatedAt() { return createdAt; }
    public List<Transaction> getTransactions() { return Collections.unmodifiableList(transactions); }
    public FixedDeposit getFixedDeposit() { return fixedDeposit; }
    public long getCreationOrder() { return creationOrder; }

    public void credit(BigDecimal amount, String note, TransactionType type) {
        balance = balance.add(amount);
        transactions.add(new Transaction(type, amount, note));
    }

    public void debit(BigDecimal amount, String note) {
        balance = balance.subtract(amount);
        transactions.add(new Transaction(TransactionType.DEBIT, amount, note));
    }

    public int getTransactionCount() {
        return transactions.size();
    }

    public void setFixedDeposit(FixedDeposit fd) { this.fixedDeposit = fd; }
    public void clearFixedDeposit() { this.fixedDeposit = null; }
}
