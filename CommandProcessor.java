package com.fkwallet.cli;

import com.fkwallet.service.WalletService;

import java.math.BigDecimal;
import java.util.Map;

public class CommandProcessor {
    private final WalletService service;

    public CommandProcessor(WalletService service) {
        this.service = service;
    }

    public String exec(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) return "";
        try {
            switch (parts[0]) {
                case "CreateWallet":
                    validateArgs(parts, 3);
                    service.createWallet(parts[1], new BigDecimal(parts[2]));
                    return "";
                case "TransferMoney":
                    validateArgs(parts, 4);
                    service.transfer(parts[1], parts[2], new BigDecimal(parts[3]));
                    return "";
                case "Statement":
                    validateArgs(parts, 2);
                    return service.statement(parts[1]);
                case "Overview":
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, String> e : service.overview().entrySet()) {
                        sb.append(e.getKey()).append("\t").append(e.getValue()).append("\n");
                    }
                    return sb.toString().trim();
                case "Offer2":
                    service.offer2();
                    return "";
                case "FixedDeposit":
                    validateArgs(parts, 3);
                    service.fixedDeposit(parts[1], new BigDecimal(parts[2]));
                    return "";
                default:
                    return "Unknown command: " + parts[0];
            }
        } catch (Exception ex) {
            return "ERROR: " + ex.getMessage();
        }
    }

    private void validateArgs(String[] parts, int expected) {
        if (parts.length < expected) throw new IllegalArgumentException("Invalid arguments");
    }
}
