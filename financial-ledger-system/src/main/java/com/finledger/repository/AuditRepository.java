package com.finledger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.finledger.entity.AuditLog;

public interface AuditRepository extends JpaRepository<AuditLog, Long> {}
