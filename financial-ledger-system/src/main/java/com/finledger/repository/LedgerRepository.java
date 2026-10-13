package com.finledger.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.finledger.entity.LedgerEntry;

public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByTransactionIdOrderByIdAsc(Long transactionId);
}
