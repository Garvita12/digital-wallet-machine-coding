package com.fkwallet.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Transaction {
    private final Instant time;
    private final TransactionType type;
    private final BigDecimal amount;
    private final String note;

    public Transaction(TransactionType type, BigDecimal amount, String note) {
        this.time = Instant.now();
        this.type = type;
        this.amount = amount;
        this.note = note;
    }

    public Instant getTime() { return time; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getNote() { return note; }

    @Override
    public String toString() {
        String direction = type == TransactionType.DEBIT ? "debit" : "credit";
        return String.format("%s %s %s", note, direction, amount.stripTrailingZeros().toPlainString());
    }
}
