package com.fkwallet;

import com.fkwallet.service.impl.OfferServiceImpl;
import com.fkwallet.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WalletServiceTest {

    WalletServiceImpl walletService;

    @BeforeEach
    void setup() {
        walletService = new WalletServiceImpl();
        var offer = new OfferServiceImpl(walletService);
        walletService.setOfferService(offer);
    }

    @Test
    void testCreateAndOverview() {
        walletService.createWallet("Harry", new BigDecimal("100"));
        walletService.createWallet("Ron", new BigDecimal("95.7"));
        Map<String, String> ov = walletService.overview();
        assertEquals("100", ov.get("Harry"));
        assertEquals("95.7", ov.get("Ron"));
    }

    @Test
    void testTransferAndOffer1() {
        walletService.createWallet("A", new BigDecimal("50"));
        walletService.createWallet("B", new BigDecimal("40"));
        walletService.transfer("A","B", new BigDecimal("5")); // A=45 B=45 -> equal -> both +10
        assertEquals(new BigDecimal("55"), walletService.getWallet("A").getBalance());
        assertEquals(new BigDecimal("55"), walletService.getWallet("B").getBalance());
    }

    @Test
    void testOffer2TieBreakers() {
        walletService.createWallet("X", new BigDecimal("10"));
        walletService.createWallet("Y", new BigDecimal("20"));
        walletService.createWallet("Z", new BigDecimal("30"));
        walletService.transfer("Z","X", new BigDecimal("1")); // Z:1, X:1
        walletService.transfer("Z","X", new BigDecimal("1")); // Z:2, X:2
        walletService.transfer("X","Y", new BigDecimal("0.0001")); // X:3, Y:1
        walletService.transfer("Y","X", new BigDecimal("0.0001")); // Y:2
        walletService.transfer("Y","X", new BigDecimal("0.0001")); // Y:3
        walletService.offer2();
        assertTrue(walletService.getWallet("Y").getBalance().compareTo(new BigDecimal("20").add(WalletServiceImpl.OFFER2_FIRST)) >= 0);
    }

    @Test
    void testFDFlow() {
        walletService.createWallet("P", new BigDecimal("100"));
        walletService.fixedDeposit("P", new BigDecimal("50"));
        walletService.transfer("P","P", new BigDecimal("0.0001"));
        walletService.transfer("P","P", new BigDecimal("0.0001"));
        walletService.transfer("P","P", new BigDecimal("0.0001"));
        walletService.transfer("P","P", new BigDecimal("0.0001"));
        walletService.transfer("P","P", new BigDecimal("0.0001"));
        boolean found = walletService.getWallet("P").getTransactions().stream()
                .anyMatch(t -> t.getNote().equals("FD interest"));
        assertTrue(found);
    }
}
