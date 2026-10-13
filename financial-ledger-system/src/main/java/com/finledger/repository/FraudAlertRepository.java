package com.finledger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.finledger.entity.FraudAlert;

public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {}
