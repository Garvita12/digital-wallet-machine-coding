package com.fkwallet;

import com.fkwallet.cli.CommandProcessor;
import com.fkwallet.service.impl.OfferServiceImpl;
import com.fkwallet.service.impl.WalletServiceImpl;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws Exception {
        WalletServiceImpl walletService = new WalletServiceImpl(null);
        OfferServiceImpl offerService = new OfferServiceImpl(walletService);
        walletService = new WalletServiceImpl(offerService);
        offerService = new OfferServiceImpl(walletService);

        var processor = new CommandProcessor(walletService);

        System.out.println("FkRupee Wallet CLI. Enter commands. Type 'exit' to quit.");
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
