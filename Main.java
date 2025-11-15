package com.fkwallet;

import com.fkwallet.cli.CommandProcessor;
import com.fkwallet.service.impl.OfferServiceImpl;
import com.fkwallet.service.impl.WalletServiceImpl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) throws Exception {
        WalletServiceImpl walletService = new WalletServiceImpl();
        OfferServiceImpl offerService = new OfferServiceImpl(walletService);
        walletService.setOfferService(offerService);
        walletService.createWallet("Harry", new BigDecimal("100"));
        walletService.createWallet("Ron", new BigDecimal("95.7"));
        walletService.createWallet("Hermione", new BigDecimal("104"));
        walletService.createWallet("Albus", new BigDecimal("200"));
        walletService.createWallet("Draco", new BigDecimal("500"));

        CommandProcessor processor = new CommandProcessor(walletService);

        System.out.println("FkRupee Wallet CLI demo. Type commands or 'exit' to quit.");
        System.out.println("Sample: Overview");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().equalsIgnoreCase("exit")) break;
            if (line.trim().isEmpty()) continue;
            String out = processor.exec(line);
            if (!out.isEmpty()) System.out.println(out);
        }
    }
}
