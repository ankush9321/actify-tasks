package com.finledger.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.finledger.entity.FraudAlert;
import com.finledger.repository.FraudAlertRepository;

@Service
public class FraudService {
    private final FraudAlertRepository alerts;
    public FraudService(FraudAlertRepository alerts) { this.alerts = alerts; }

    @Transactional(propagation = Propagation.MANDATORY)
    public void checkAmount(BigDecimal amount, String actor) {
        if (amount.compareTo(new BigDecimal("50000.00")) >= 0) {
            alerts.save(new FraudAlert("HIGH_VALUE_TRANSFER",
                    "Transaction amount reached configured review threshold: " + amount, actor));
        }
    }
}
