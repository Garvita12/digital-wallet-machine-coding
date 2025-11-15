package com.fkwallet.service.impl;

import com.fkwallet.model.Wallet;
import com.fkwallet.service.OfferService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OfferServiceImpl implements OfferService {

    private final WalletServiceImpl walletService;

    public OfferServiceImpl(WalletServiceImpl walletService) {
        this.walletService = walletService;
    }

    @Override
    public void maybeTriggerOffer1(String from, String to) {
        Wallet wf = walletService.getWallet(from);
        Wallet wt = walletService.getWallet(to);
        if (wf == null || wt == null) return;
        if (wf.getBalance().compareTo(wt.getBalance()) == 0) {
            walletService.creditReward(from, WalletServiceImpl.OFFER1_REWARD, "Offer1 reward");
            walletService.creditReward(to, WalletServiceImpl.OFFER1_REWARD, "Offer1 reward");
        }
    }

    @Override
    public void triggerOffer2() {
        List<Wallet> list = walletService.snapshotWallets();

        List<Wallet> sorted = list.stream()
                .sorted(
                        Comparator.comparingInt(Wallet::getTransactionCount).reversed()
                                .thenComparing(Wallet::getBalance, Comparator.reverseOrder())
                                .thenComparing(Wallet::getCreationOrder)
                )
                .collect(Collectors.toList());

        if (sorted.size() >= 1) walletService.creditReward(sorted.get(0).getOwner(), WalletServiceImpl.OFFER2_FIRST, "Offer2 reward 1");
        if (sorted.size() >= 2) walletService.creditReward(sorted.get(1).getOwner(), WalletServiceImpl.OFFER2_SECOND, "Offer2 reward 2");
        if (sorted.size() >= 3) walletService.creditReward(sorted.get(2).getOwner(), WalletServiceImpl.OFFER2_THIRD, "Offer2 reward 3");
    }
}
