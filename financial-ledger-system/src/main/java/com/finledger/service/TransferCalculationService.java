package com.finledger.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class TransferCalculationService {
    private static final BigDecimal FEE_RATE = new BigDecimal("0.015");
    private static final BigDecimal GST_RATE = new BigDecimal("0.18");

    public Amounts calculate(BigDecimal amount) {
        if (amount == null || amount.scale() > 2 || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be positive and have at most two decimal places");
        BigDecimal fee = amount.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gst = fee.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        return new Amounts(amount.setScale(2, RoundingMode.UNNECESSARY), fee, gst,
                amount.add(fee).add(gst).setScale(2, RoundingMode.HALF_UP));
    }
    public static class Amounts {
        public final BigDecimal amount, fee, gst, totalDebit;
        public Amounts(BigDecimal amount, BigDecimal fee, BigDecimal gst, BigDecimal totalDebit) {
            this.amount=amount; this.fee=fee; this.gst=gst; this.totalDebit=totalDebit;
        }
    }
}
