package com.fkwallet.service.impl;

import com.fkwallet.model.Wallet;
import com.fkwallet.service.OfferService;

import java.util.Comparator;
import java.util.List;

public class OfferServiceImpl implements OfferService {

    private final WalletServiceImpl walletService;

    public OfferServiceImpl(WalletServiceImpl walletService) {
        this.walletService = walletService;
    }

    @Override
    public void maybeTriggerOffer1(String from, String to) {
        var w1 = walletService.getWallet(from);
        var w2 = walletService.getWallet(to);

        if (w1.getBalance().compareTo(w2.getBalance()) == 0) {
            walletService.creditReward(from, WalletServiceImpl.OFFER1_REWARD, "Offer1");
            walletService.creditReward(to, WalletServiceImpl.OFFER1_REWARD, "Offer1");
        }
    }

    @Override
    public void triggerOffer2() {
        List<Wallet> list = walletService.snapshotWallets();

        list.sort(Comparator
                .comparing(Wallet::getTransactionCount).reversed()
                .thenComparing(Wallet::getBalance).reversed()
                .thenComparing(Wallet::getCreationOrder)  // earlier created first
        );

        if (list.size() >= 1) {
            var w = list.get(0);
            walletService.creditReward(w.getOwner(),
                    WalletServiceImpl.OFFER2_FIRST,
                    "Offer2 First");
        }
        if (list.size() >= 2) {
            var w = list.get(1);
            walletService.creditReward(w.getOwner(),
                    WalletServiceImpl.OFFER2_SECOND,
                    "Offer2 Second");
        }
        if (list.size() >= 3) {
            var w = list.get(2);
            walletService.creditReward(w.getOwner(),
                    WalletServiceImpl.OFFER2_THIRD,
                    "Offer2 Third");
        }
    }
}
