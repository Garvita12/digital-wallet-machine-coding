package com.fkwallet.service;

import com.fkwallet.model.Wallet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface WalletService {
    void createWallet(String owner, BigDecimal initial);
    void transfer(String from, String to, BigDecimal amount) throws IllegalArgumentException;
    String statement(String owner);
    Map<String, String> overview();
    void offer2();
    void fixedDeposit(String owner, BigDecimal fdAmount);
    Wallet getWallet(String owner);
}
