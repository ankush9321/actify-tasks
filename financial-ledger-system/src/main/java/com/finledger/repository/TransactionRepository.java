package com.finledger.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.finledger.entity.FinancialTransaction;

public interface TransactionRepository extends JpaRepository<FinancialTransaction, Long> {
    Optional<FinancialTransaction> findByIdempotencyKey(String key);
    List<FinancialTransaction> findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(Long sourceId, Long destinationId);
}
