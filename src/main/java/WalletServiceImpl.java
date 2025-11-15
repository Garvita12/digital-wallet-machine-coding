package com.fkwallet.service.impl;

import com.fkwallet.model.FixedDeposit;
import com.fkwallet.model.Transaction;
import com.fkwallet.model.TransactionType;
import com.fkwallet.model.Wallet;
import com.fkwallet.service.OfferService;
import com.fkwallet.service.WalletService;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class WalletServiceImpl implements WalletService {

    private final Map<String, Wallet> wallets = new LinkedHashMap<>();
    private OfferService offerService;
    private final AtomicLong orderSeq = new AtomicLong(0);

    public static final BigDecimal MIN_UNIT = new BigDecimal("0.0001");
    public static final BigDecimal OFFER1_REWARD = new BigDecimal("10");
    public static final BigDecimal OFFER2_FIRST = new BigDecimal("10");
    public static final BigDecimal OFFER2_SECOND = new BigDecimal("5");
    public static final BigDecimal OFFER2_THIRD = new BigDecimal("2");
    public static final BigDecimal FD_INTEREST = new BigDecimal("10");

    public WalletServiceImpl() {}

    public void setOfferService(OfferService offerService) {
        this.offerService = offerService;
    }

    @Override
    public synchronized void createWallet(String owner, BigDecimal initial) {
        requirePositiveOrZero(initial, "initial");
        if (initial.scale() > 4) initial = initial.setScale(4, BigDecimal.ROUND_HALF_UP);
        if (wallets.containsKey(owner)) throw new IllegalArgumentException("Wallet exists: " + owner);
        Wallet w = new Wallet(owner, initial, orderSeq.incrementAndGet());
        wallets.put(owner, w);
    }

    @Override
    public synchronized void transfer(String from, String to, BigDecimal amount) {
        requirePlayerExists(from);
        requirePlayerExists(to);
        requirePositive(amount, "amount");
        if (amount.compareTo(MIN_UNIT) < 0) throw new IllegalArgumentException("Minimum transfer is " + MIN_UNIT);

        Wallet wf = wallets.get(from);
        Wallet wt = wallets.get(to);

        if (wf.getBalance().compareTo(amount) < 0) throw new IllegalArgumentException("Insufficient funds");

        wf.debit(amount, String.format("%s -> %s", from, to));
        wt.credit(amount, String.format("%s <- %s", to, from), TransactionType.CREDIT);

        updateFixedDepositsOnGlobalTx();

        if (offerService != null) offerService.maybeTriggerOffer1(from, to);
    }

    private void updateFixedDepositsOnGlobalTx() {
        List<Wallet> list = new ArrayList<>(wallets.values());
        for (Wallet w : list) {
            FixedDeposit fd = w.getFixedDeposit();
            if (fd == null) continue;

            if (w.getBalance().compareTo(fd.getFdAmount()) < 0) {
                w.clearFixedDeposit();
            } else {
                fd.decrement();
                if (fd.isComplete()) {
                    w.credit(FD_INTEREST, "FD interest", TransactionType.FD_INTEREST);
                    w.clearFixedDeposit();
                }
            }
        }
    }

    @Override
    public synchronized String statement(String owner) {
        requirePlayerExists(owner);
        Wallet w = wallets.get(owner);
        StringBuilder sb = new StringBuilder();

        for (var t : w.getTransactions()) {
            sb.append(t.getNote()).append(" ")
              .append(t.getType() == TransactionType.DEBIT ? "debit" : "credit")
              .append(" ")
              .append(t.getAmount().stripTrailingZeros().toPlainString())
              .append("\n");
        }

        if (w.getFixedDeposit() != null) {
            sb.append(String.format(
                    "FD %s remTx %d",
                    w.getFixedDeposit().getFdAmount().stripTrailingZeros().toPlainString(),
                    w.getFixedDeposit().getRemainingTxs()
            ));
        }

        return sb.toString().trim();
    }

    @Override
    public synchronized Map<String, String> overview() {
        Map<String, String> out = new LinkedHashMap<>();
        for (var w : wallets.values()) {
            StringBuilder line = new StringBuilder();
            line.append(w.getBalance().stripTrailingZeros().toPlainString());
            if (w.getFixedDeposit() != null) {
                line.append(" FD=")
                        .append(w.getFixedDeposit().getFdAmount().stripTrailingZeros().toPlainString())
                        .append(" remTx=")
                        .append(w.getFixedDeposit().getRemainingTxs());
            }
            out.put(w.getOwner(), line.toString());
        }
        return out;
    }

    @Override
    public synchronized void offer2() {
        if (offerService != null) offerService.triggerOffer2();
    }

    @Override
    public synchronized void fixedDeposit(String owner, BigDecimal fdAmount) {
        requirePlayerExists(owner);
        if (fdAmount.scale() > 4)
            fdAmount = fdAmount.setScale(4, BigDecimal.ROUND_HALF_UP);

        Wallet w = wallets.get(owner);

        if (w.getFixedDeposit() != null) {
            throw new IllegalArgumentException("FD already exists for " + owner);
        }
        if (w.getBalance().compareTo(fdAmount) < 0) {
            throw new IllegalArgumentException("Insufficient balance to park FD");
        }

        w.setFixedDeposit(new FixedDeposit(fdAmount));

        // FIX: use addTransaction instead of unmodifiableList()
        w.addTransaction(new Transaction(TransactionType.FD_PARK, fdAmount, "FD parked"));
    }

    private void requirePlayerExists(String owner) {
        if (!wallets.containsKey(owner))
            throw new IllegalArgumentException("Unknown wallet " + owner);
    }

    private void requirePositive(BigDecimal v, String name) {
        if (v == null || v.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException(name + " must be > 0");
    }

    private void requirePositiveOrZero(BigDecimal v, String name) {
        if (v == null || v.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException(name + " must be >= 0");
    }

    @Override
    public synchronized Wallet getWallet(String owner) {
        return wallets.get(owner);
    }

    public synchronized List<Wallet> snapshotWallets() {
        return new ArrayList<>(wallets.values());
    }

    public synchronized void creditReward(String owner, BigDecimal amount, String note) {
        Wallet w = wallets.get(owner);
        if (w == null) return;
        w.credit(amount, note, TransactionType.OFFER_REWARD);
    }
}
